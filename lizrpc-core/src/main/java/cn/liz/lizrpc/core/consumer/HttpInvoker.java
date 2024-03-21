package cn.liz.lizrpc.core.consumer;

import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;

public interface HttpInvoker {
    RpcResponse<?> post(RpcRequest rpcRequest, String url);
}
