package cn.liz.lizrpc.core.api;

import java.util.List;

public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer Default = p -> (p == null || p.isEmpty()) ? null : p.get(0);
}
