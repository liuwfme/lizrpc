package cn.liz.lizrpc.core.api;

import cn.liz.lizrpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.List;

@Data
public class RpcContext {
    List<Filter> filters;

    Router<InstanceMeta> router;

    LoadBalancer<InstanceMeta> loadBalancer;
}
