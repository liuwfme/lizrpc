package cn.liz.lizrpc.core.consumer;

import cn.liz.lizrpc.core.api.RpcContext;
import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;
import cn.liz.lizrpc.core.consumer.http.OkHttpInvoker;
import cn.liz.lizrpc.core.util.MethodUtils;
import cn.liz.lizrpc.core.util.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 消费端的动态代理处理类
 */
public class LizInvocationHandler implements InvocationHandler {

    Class<?> service;
    RpcContext context;
    List<String> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();

    public LizInvocationHandler(Class<?> clazz, RpcContext context, List<String> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        List<String> urls = context.getRouter().route(this.providers);
        String url = (String) context.getLoadBalancer().choose(urls);
        System.out.println("loadBalancer.choose(urls) ---> " + url);
        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, url);
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            Exception ex = rpcResponse.getEx();
            throw new RuntimeException(ex);
        }
    }

}
