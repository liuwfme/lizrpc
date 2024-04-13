package cn.liz.lizrpc.core.consumer;

import cn.liz.lizrpc.core.annotation.LizConsumer;
import cn.liz.lizrpc.core.api.RegistryCenter;
import cn.liz.lizrpc.core.api.RpcContext;
import cn.liz.lizrpc.core.meta.InstanceMeta;
import cn.liz.lizrpc.core.meta.ServiceMeta;
import cn.liz.lizrpc.core.util.MethodUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费者启动类
 */
@Data
@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;

    Environment environment;

//    @Value("${app.id}")
//    private String app;
//
//    @Value("${app.namespace}")
//    private String namespace;
//
//    @Value("${app.env}")
//    private String env;
//
//    @Value("${app.retries}")
//    private int retries;
//
//    @Value("${app.timeout}")
//    private int timeout;

    private Map<String, Object> stub = new HashMap<>();

    public void start() {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
//        Router<InstanceMeta> router = applicationContext.getBean(Router.class);
//        LoadBalancer<InstanceMeta> loadBalancer = applicationContext.getBean(LoadBalancer.class);
//        List<Filter> filters = applicationContext.getBeansOfType(Filter.class).values().stream().toList();
//        RpcContext context = new RpcContext();
//        context.setRouter(router);
//        context.setLoadBalancer(loadBalancer);
//        context.setFilters(filters);
//        context.getParameters().put("app.retries", String.valueOf(retries));
//        context.getParameters().put("app.timeout", String.valueOf(timeout));
        RpcContext context = applicationContext.getBean(RpcContext.class);

        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);
//            if (!name.contains("lizrpcDemoConsumerApplication")) continue;
            List<Field> fields = MethodUtils.findAnnotatedField(bean.getClass(), LizConsumer.class);
            fields.stream().forEach(f -> {
                log.info("===> LizConsumer annotation -> " + f.getName());
                try {
                    Class<?> service = f.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        consumer = createFromARegistry(service, context, rc);
                        stub.put(serviceName, consumer);
                    }
                    f.setAccessible(true);
                    f.set(bean, consumer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private Object createFromARegistry(Class<?> service, RpcContext context, RegistryCenter rc) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(context.getParameters().get("app.id"))
                .namespace(context.getParameters().get("app.namespace"))
                .env(context.getParameters().get("app.env")).name(service.getCanonicalName()).build();
        List<InstanceMeta> providers = rc.fetchAll(serviceMeta);
        log.info("===> map to providers : ");
        providers.forEach(System.out::println);

        rc.subscribe(serviceMeta, event -> {
            providers.clear();
            providers.addAll(event.getData());
        });
        return createConsumer(service, context, providers);
    }

    private Object createConsumer(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(),
                new Class[]{service}, new LizInvocationHandler(service, context, providers));
    }


}
