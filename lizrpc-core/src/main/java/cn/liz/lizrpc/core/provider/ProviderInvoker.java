package cn.liz.lizrpc.core.provider;

import cn.liz.lizrpc.core.api.RpcContext;
import cn.liz.lizrpc.core.api.RpcException;
import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;
import cn.liz.lizrpc.core.meta.ProviderMeta;
import cn.liz.lizrpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        log.info("======ProviderInvoker.invoke,request:{}", request);
        if (!request.getParams().isEmpty()) {
            request.getParams().forEach((k, v) -> {
                RpcContext.setContextParameter(k, v);
            });
        }
        RpcResponse<Object> rpcResponse = new RpcResponse<>();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {
            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
            Method method = meta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
            Object result = method.invoke(meta.getServiceImpl(), args);

            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
            log.warn("ProviderInvoker#invoke(), InvocationTargetException e: ", e);
            rpcResponse.setEx(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException | IllegalArgumentException e) {
            log.warn("ProviderInvoker#invoke(), IllegalException e: ", e);
            rpcResponse.setEx(new RpcException(e.getMessage()));
        } catch (Exception e) {
            log.warn("ProviderInvoker#invoke(), Exception e: ", e);
            rpcResponse.setEx(new RpcException(RpcException.ErrCodeEnum.Unknown.getMessage()));
        } finally {
            RpcContext.contextParameters.get().clear();
        }
        log.debug(" ====== ProviderInvoker.invoke response : {}", rpcResponse);
        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes, Type[] genericParameterTypes) {
        if (args == null || args.length == 0) return args;
        Object[] actualArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actualArgs[i] = TypeUtils.castGeneric(args[i], parameterTypes[i], genericParameterTypes[i]);
        }
        return actualArgs;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }
}
