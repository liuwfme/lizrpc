package cn.liz.lizrpc.core.api;

import cn.liz.lizrpc.core.config.ConsumerConfigProperties;
import cn.liz.lizrpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RpcContext {
    List<Filter> filters;

    Router<InstanceMeta> router;

    LoadBalancer<InstanceMeta> loadBalancer;

    private Map<String, String> parameters = new HashMap<>();

    private ConsumerConfigProperties consumerConfigProperties;

    public static ThreadLocal<Map<String, String>> contextParameters = new ThreadLocal<>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<>();
        }
    };

    public static void setContextParameter(String key, String value) {
        contextParameters.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return contextParameters.get().get(key);
    }

    public static void removeContextParameter(String key) {
        contextParameters.get().remove(key);
    }
}
