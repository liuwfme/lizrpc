package cn.liz.lizrpc.core.filter;

import cn.liz.lizrpc.core.api.Filter;
import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheFilter implements Filter {

    static Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object preFilter(RpcRequest request) {
        return cache.get(request.toString());
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        cache.putIfAbsent(request.toString(), result);
        return result;
    }
}
