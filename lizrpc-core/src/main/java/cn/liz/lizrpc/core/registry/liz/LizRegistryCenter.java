package cn.liz.lizrpc.core.registry.liz;

import cn.liz.lizrpc.core.consumer.HttpInvoker;
import cn.liz.lizrpc.core.meta.InstanceMeta;
import cn.liz.lizrpc.core.meta.ServiceMeta;
import cn.liz.lizrpc.core.registry.ChangedListener;
import cn.liz.lizrpc.core.registry.Event;
import cn.liz.lizrpc.core.registry.RegistryCenter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LizRegistryCenter implements RegistryCenter {

    @Value("${lizregistry.servers}")
    private String servers;

    Map<String, Long> VERSIONS = new HashMap<>();
    ScheduledExecutorService consumerExecutor = null;

    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    ScheduledExecutorService providerExecutor = null;

    @Override
    public void start() {
        log.info(" ====== [LizRegistry] : start with servers: {}", servers);
        consumerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor.scheduleAtFixedRate(() -> {
//            RENEWS.forEach((k, v) -> {
//                StringBuilder sb = new StringBuilder();
//                for (ServiceMeta service : v) {
//                    sb.append(service.toPath()).append(",");
//                }
//                String services = sb.toString();
//                if (services.endsWith(",")) services = services.substring(0, services.length() - 1);
//                HttpInvoker.httpPost(JSON.toJSONString(k), servers + "/renews?services=" + services, InstanceMeta.class);
//            });

            RENEWS.keySet().parallelStream().forEach(instance -> {
                StringBuilder sb = new StringBuilder();
                for (ServiceMeta service : RENEWS.get(instance)) {
                    sb.append(service.toPath()).append(",");
                }
//                String.join(",", RENEWS.get(instance).stream().map(ServiceMeta::toPath).toArray());
                String services = sb.toString();
                if (services.endsWith(",")) services = services.substring(0, services.length() - 1);
                log.info(" ====== [LizRegistry] : renews instance:{}, services:{}", instance, services);
                Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance),
                        servers + "/renews?services=" + services, Long.class);
                log.info(" ====== [LizRegistry] : renews instance:{}, services:{}, timestamp:{}", instance, services, timestamp);
            });
        }, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        log.info(" ====== [LizRegistry] : stop...");
        gracefulShutdownExecutor(consumerExecutor);
        gracefulShutdownExecutor(providerExecutor);
        log.info(" ====== [LizRegistry] : stopped !");
    }

    private void gracefulShutdownExecutor(ScheduledExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====== [LizRegistry] : register service:{}, instance:{}", service, instance);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/register?service=" + service.toPath(), Void.class);
        RENEWS.add(instance, service);
        log.info(" ====== [LizRegistry] : register success! service:{}, instance:{}", service, instance);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====== [LizRegistry] : unregister service:{}, instance:{}", service, instance);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/unregister?service=" + service.toPath(), Void.class);
        RENEWS.remove(instance, service);
        log.info(" ====== [LizRegistry] : unregister success! service:{}, instance:{}", service, instance);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ====== [LizRegistry] : fetchAll service:{}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(servers + "/findAll?service=" + service.toPath(), new TypeReference<List<InstanceMeta>>() {
        });
        log.info(" ====== [LizRegistry] : fetchAll service:{}, instances:{}", service, instances);
        return instances;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        consumerExecutor.scheduleWithFixedDelay(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long newVersion = HttpInvoker.httpGet(servers + "/version?service=" + service.toPath(), Long.class);
            log.debug(" ====== [LizRegistry] : subscribe version:{}, newVersion:{}", version, newVersion);
            if (newVersion > version) {
                log.info(" ====== [LizRegistry] : subscribe version:{}, newVersion:{}", version, newVersion);
                List<InstanceMeta> instances = fetchAll(service);
                listener.fire(new Event(instances));
                VERSIONS.put(service.toPath(), newVersion);
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }
}
