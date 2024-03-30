package cn.liz.lizrpc.core.consumer;

import cn.liz.lizrpc.core.api.*;
import cn.liz.lizrpc.core.consumer.http.OkHttpInvoker;
import cn.liz.lizrpc.core.governance.SlidingTimeWindow;
import cn.liz.lizrpc.core.meta.InstanceMeta;
import cn.liz.lizrpc.core.util.MethodUtils;
import cn.liz.lizrpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消费端的动态代理处理类
 */
@Slf4j
public class LizInvocationHandler implements InvocationHandler {

    Class<?> service;
    RpcContext context;
    final List<InstanceMeta> providers;

    final List<InstanceMeta> isolatedProviders = new ArrayList<>();

    final List<InstanceMeta> halfOpenProviders = new ArrayList<>();

    final Map<String, SlidingTimeWindow> windowMap = new HashMap<>();

    ScheduledExecutorService executorService;

    HttpInvoker httpInvoker;

    public LizInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;
        this.httpInvoker = new OkHttpInvoker(Integer.parseInt(context.getParameters()
                .getOrDefault("app.timeout", "1000")));
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(this::halfOpen, 10, 60, TimeUnit.SECONDS);
    }

    private void halfOpen() {
        log.debug(" ===> halfOpen, isolatedProviders : {}", isolatedProviders);
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolatedProviders);
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

        int retries = Integer.parseInt(context.getParameters().getOrDefault("app.retries", "1"));
        while (retries-- > 0) {
            log.info("retries : " + retries);
            try {
                return rpcExecute(method, rpcRequest);
            } catch (Exception e) {
                if (!(e.getCause() instanceof SocketTimeoutException)) {
                    throw e;
                }
            }
        }
        return null;
    }

    private Object rpcExecute(Method method, RpcRequest rpcRequest) {
        for (Filter filter : this.context.getFilters()) {
            Object preResult = filter.preFilter(rpcRequest);
            if (preResult != null) {
                log.debug(filter.getClass().getName() + " ===> preFilter : " + preResult);
                return preResult;
            }
        }

        InstanceMeta instanceMeta;
        synchronized (halfOpenProviders) {
            if (halfOpenProviders.isEmpty()) {
                List<InstanceMeta> instanceMetas = context.getRouter().route(this.providers);
                instanceMeta = context.getLoadBalancer().choose(instanceMetas);
                log.debug("loadBalancer.choose(instanceMetas) ---> " + instanceMeta);
            } else {
                instanceMeta = halfOpenProviders.remove(0);
                log.debug("check instance alive, instanceMeta : {}", instanceMeta);
            }
        }

        String url = instanceMeta.toUrl();
        RpcResponse<?> rpcResponse;
        Object result;
        try {
            rpcResponse = httpInvoker.post(rpcRequest, url);
            result = castReturnResult(method, rpcResponse);
        } catch (Exception e) {
            // 统计和隔离故障
            // 每一次异常记录一次，统计30s的异常数。滑动时间窗口。
            synchronized (windowMap) {
                SlidingTimeWindow window = windowMap.get(url);
                if (window == null) {
                    window = new SlidingTimeWindow();
                    windowMap.put(url, window);
                }
                window.record(System.currentTimeMillis());
                log.debug("url : {} in window with : {}", url, window.getSum());
                if (window.getSum() >= 10) {
                    // 隔离故障
                    isolate(instanceMeta);
                }
            }
            throw e;
        }

        // 故障恢复
        recover(instanceMeta);

        for (Filter filter : this.context.getFilters()) {
            Object filterResult = filter.postFilter(rpcRequest, rpcResponse, result);
            if (filterResult != null) {
                return filterResult;
            }
        }

        return result;
    }

    private void recover(InstanceMeta instanceMeta) {
        synchronized (providers) {
            if (!providers.contains(instanceMeta)) {
                isolatedProviders.remove(instanceMeta);
                providers.add(instanceMeta);
                log.debug("===> recovered instanceMeta:{}, providers:{}, isolatedProviders:{}",
                        instanceMeta, providers, isolatedProviders);
            }
        }
    }

    private void isolate(InstanceMeta instanceMeta) {
        log.debug("==> isolate instanceMeta:{}", instanceMeta);

        providers.remove(instanceMeta);
        log.debug("===> isolate providers:{}", providers);

        isolatedProviders.add(instanceMeta);
        log.debug("===> isolate isolatedProviders:{}", isolatedProviders);
    }

    private Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            return TypeUtils.castMethodResult(method, rpcResponse.getData());
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
