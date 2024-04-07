package cn.liz.lizrpc.core.provider;

import cn.liz.lizrpc.core.annotation.LizProvider;
import cn.liz.lizrpc.core.api.RegistryCenter;
import cn.liz.lizrpc.core.config.AppConfigProperties;
import cn.liz.lizrpc.core.config.ProviderConfigProperties;
import cn.liz.lizrpc.core.meta.InstanceMeta;
import cn.liz.lizrpc.core.meta.ProviderMeta;
import cn.liz.lizrpc.core.meta.ServiceMeta;
import cn.liz.lizrpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

/**
 * 服务提供者的启动类
 */
@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;
    RegistryCenter rc;
    String port;
    private AppConfigProperties appConfigProperties;
    private ProviderConfigProperties providerConfigProperties;
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
    private InstanceMeta instance;

    public ProviderBootstrap(String port, AppConfigProperties appConfigProperties,
                             ProviderConfigProperties providerConfigProperties) {
        this.port = port;
        this.appConfigProperties = appConfigProperties;
        this.providerConfigProperties = providerConfigProperties;
    }

//    @Value("${server.port}")
//    private String port;
//
//    @Value("${app.id}")
//    private String app;
//
//    @Value("${app.namespace}")
//    private String namespace;
//
//    @Value("${app.env}")
//    private String env;
//
//    @Value("#{${app.metas}}")//SpEL
//    Map<String, String> metas;

    @PostConstruct // init-method
    public void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(LizProvider.class);
        providers.forEach((x, y) -> log.info("beanName : " + x));
        providers.values().forEach(this::genInterface);
        rc = applicationContext.getBean(RegistryCenter.class);
    }

    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        instance = InstanceMeta.http(ip, Integer.valueOf(port)).addParams(providerConfigProperties.getMetas());
        rc.start();
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        log.info(" ===> unregister all services");
        skeleton.keySet().forEach(this::unregisterService);
        rc.stop();
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appConfigProperties.getId()).namespace(appConfigProperties.getNamespace())
                .env(appConfigProperties.getEnv()).name(service).build();
        rc.register(serviceMeta, instance);
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appConfigProperties.getId()).namespace(appConfigProperties.getNamespace())
                .env(appConfigProperties.getEnv()).name(service).build();
        rc.unregister(serviceMeta, instance);
    }

    private void genInterface(Object impl) {
        Arrays.stream(impl.getClass().getInterfaces()).forEach(
                service -> {
//                    Method[] methods = service.getMethods();
//                    for (Method method : methods) {
//                        if (MethodUtils.checkLocalMethod(method)) {
//                            continue;
//                        }
//                        createProvider(service, impl, method);
//                    }
                    Arrays.stream(service.getMethods())
                            .filter(method -> !MethodUtils.checkLocalMethod(method))
                            .forEach(method -> createProvider(service, impl, method));
                }
        );
    }

    private void createProvider(Class<?> service, Object impl, Method method) {
        ProviderMeta providerMeta = ProviderMeta.builder()
                .serviceImpl(impl)
                .method(method)
                .methodSign(MethodUtils.methodSign(method))
                .build();
        log.info("created a provider : " + providerMeta);
        skeleton.add(service.getCanonicalName(), providerMeta);
    }

}
