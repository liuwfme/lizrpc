package cn.liz.lizrpc.core.filter;

import cn.liz.lizrpc.core.api.Filter;
import cn.liz.lizrpc.core.api.RpcContext;
import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;

import java.util.Map;

public class ContextParameterFilter implements Filter {

    @Override
    public Object preFilter(RpcRequest request) {
        Map<String, String> params = RpcContext.contextParameters.get();
        if (!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        RpcContext.contextParameters.get().clear();
        return null;
    }
}
