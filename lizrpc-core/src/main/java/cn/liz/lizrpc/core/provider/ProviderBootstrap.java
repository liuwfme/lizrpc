package cn.liz.lizrpc.core.provider;

import cn.liz.lizrpc.core.annotation.LizProvider;
import cn.liz.lizrpc.core.api.RegistryCenter;
import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;
import cn.liz.lizrpc.core.meta.ProviderMeta;
import cn.liz.lizrpc.core.util.MethodUtils;
import cn.liz.lizrpc.core.util.TypeUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 服务提供者的启动类
 */
@Data
public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;

    RegistryCenter rc;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    private String instance;

    @Value("${server.port}")
    private String port;


    @PostConstruct // init-method
    public void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(LizProvider.class);
        rc = applicationContext.getBean(RegistryCenter.class);
        providers.forEach((x, y) -> System.out.println("beanName : " + x));
        providers.values().forEach(x -> genInterface(x));
    }

    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        instance = ip + "_" + port;
        rc.start();
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        System.out.println(" ===> unregister all services");
        skeleton.keySet().forEach(this::unregisterService);
        rc.stop();
    }

    private void registerService(String service) {
        rc.register(service, instance);
    }

    private void unregisterService(String service) {
        rc.unregister(service, instance);
    }

    private void genInterface(Object x) {
        Arrays.stream(x.getClass().getInterfaces()).forEach(
                itface -> {
                    Method[] methods = itface.getMethods();
                    for (Method method : methods) {
                        if (MethodUtils.checkLocalMethod(method)) {
                            continue;
                        }
                        createProvider(itface, x, method);
                    }
                }
        );
//        skeleton.put(interfacee.getCanonicalName(), x);
    }

    private void createProvider(Class<?> interfacee, Object x, Method method) {
        ProviderMeta meta = new ProviderMeta();
        meta.setMethod(method);
        meta.setServiceImpl(x);
        meta.setMethodSign(MethodUtils.methodSign(method));
        System.out.println("created a provider : " + meta);
        skeleton.add(interfacee.getCanonicalName(), meta);
    }

    public RpcResponse invoke(RpcRequest request) {
//        if (MethodUtils.checkLocalMethod(methodSign)) {
//            return null;
//        }
        RpcResponse rpcResponse = new RpcResponse();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {
//            Method method = bean.getClass().getMethod(request.getMethod());
//            Method method = findMethod(bean.getClass(), request.getMethodSign());
            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
            Method method = meta.getMethod();
//            Object result = method.invoke(meta.getServiceImpl(), request.getArgs());
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
            Object result = method.invoke(meta.getServiceImpl(), args);

            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
        if (args == null || args.length == 0) return args;
        Object[] actualArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actualArgs[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }
        return actualArgs;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }

    private Method findMethod(Class<?> aClass, String methodName) {
        for (Method method : aClass.getMethods()) {
            if (methodName.equals(method.getName())) {// 因为可能有多个重名方法
                return method;
            }
        }
        return null;
    }
}
