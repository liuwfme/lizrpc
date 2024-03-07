package cn.liz.lizrpc.core.provider;

import cn.liz.lizrpc.core.annotation.LizProvider;
import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

    @PostConstruct // init-method
    // PreDestroy
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(LizProvider.class);
        providers.forEach((x, y) -> System.out.println("beanName : " + x));
//        skeleton.putAll(providers);
        providers.values().forEach(
                x -> genInterface(x)
        );
    }

    private void genInterface(Object x) {
        Class<?> interfacee = x.getClass().getInterfaces()[0];
        skeleton.put(interfacee.getCanonicalName(), x);
    }

    public RpcResponse invoke(RpcRequest request) {
        Object bean = skeleton.get(request.getService());
        try {
//            Method method = bean.getClass().getMethod(request.getMethod());
            Method method = findMethod(bean.getClass(), request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse(true, result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
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
