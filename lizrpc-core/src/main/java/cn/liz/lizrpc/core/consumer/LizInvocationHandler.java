package cn.liz.lizrpc.core.consumer;

import cn.liz.lizrpc.core.api.*;
import cn.liz.lizrpc.core.consumer.http.OkHttpInvoker;
import cn.liz.lizrpc.core.meta.InstanceMeta;
import cn.liz.lizrpc.core.util.MethodUtils;
import cn.liz.lizrpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 消费端的动态代理处理类
 */
@Slf4j
public class LizInvocationHandler implements InvocationHandler {

    Class<?> service;
    RpcContext context;
    List<InstanceMeta> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();

    public LizInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
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

        for (Filter filter : this.context.getFilters()) {
            Object preResult = filter.preFilter(rpcRequest);
            if (preResult != null) {
                log.debug(filter.getClass().getName() + " ===> preFilter : " + preResult);
                return preResult;
            }
        }

        List<InstanceMeta> urls = context.getRouter().route(this.providers);
        InstanceMeta instanceMeta = context.getLoadBalancer().choose(urls);
        log.debug("loadBalancer.choose(urls) ---> " + instanceMeta);
        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instanceMeta.toUrl());

        Object result = castReturnResult(method, rpcResponse);

        for (Filter filter : this.context.getFilters()) {
            Object filterResult = filter.postFilter(rpcRequest, rpcResponse, result);
            if (filterResult != null) {
                return filterResult;
            }
        }

        return result;
    }

    private Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            Exception ex = rpcResponse.getEx();
            if (ex instanceof RpcException e) {
                throw e;
            } else {
                throw new RpcException(ex, RpcException.ErrCodeEnum.Unknown.getCode());
            }
        }
    }

}
