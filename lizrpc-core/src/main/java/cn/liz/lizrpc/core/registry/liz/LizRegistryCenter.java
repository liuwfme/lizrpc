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

@Slf4j
public class LizRegistryCenter implements RegistryCenter {

    private static final String REGISTER_PATH = "/register";
    private static final String UNREGISTER_PATH = "/unregister";
    private static final String FIND_ALL_PATH = "/findAll";
    private static final String VERSION_PATH = "/version";
    private static final String RENEWS_PATH = "/renews";

    @Value("${lizregistry.servers}")
    private String servers;

    Map<String, Long> VERSIONS = new HashMap<>();

    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();

    LizHealthChecker healthChecker = new LizHealthChecker();

    @Override
    public void start() {
        log.info(" ====== [LizRegistry] : start with servers: {}", servers);
        healthChecker.start();
        providerCheck();
        log.info(" ====== [LizRegistry] : started !");
    }

    @Override
    public void stop() {
        log.info(" ====== [LizRegistry] : stop...");
        healthChecker.stop();
        log.info(" ====== [LizRegistry] : stopped !");
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====== [LizRegistry] : register service:{}, instance:{}", service, instance);
        HttpInvoker.httpPost(JSON.toJSONString(instance), registerPath(service), Void.class);
        RENEWS.add(instance, service);
        log.info(" ====== [LizRegistry] : register success! service:{}, instance:{}", service, instance);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====== [LizRegistry] : unregister service:{}, instance:{}", service, instance);
        HttpInvoker.httpPost(JSON.toJSONString(instance), unregisterPath(service), Void.class);
        RENEWS.remove(instance, service);
        log.info(" ====== [LizRegistry] : unregister success! service:{}, instance:{}", service, instance);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ====== [LizRegistry] : fetchAll service:{}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(findAllPath(service), new TypeReference<List<InstanceMeta>>() {
        });
        log.info(" ====== [LizRegistry] : fetchAll service:{}, instances:{}", service, instances);
        return instances;
    }

    public void providerCheck() {
//        healthChecker.providerChecker(new LizHealthChecker.Callback() {
//            @Override
//            public void call() throws Exception {
//                RENEWS.keySet().parallelStream().forEach(instance -> {
//                    List<ServiceMeta> services = RENEWS.get(instance);
//                    log.info(" ====== [LizRegistry] : renews instance:{}, services:{}", instance, services);
//                    Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance), renewPath(services), Long.class);
//                    log.info(" ====== [LizRegistry] : renews instance:{}, services:{}, timestamp:{}", instance, services, timestamp);
//                });
//            }
//        });
        healthChecker.providerChecker(() -> {
            RENEWS.keySet().parallelStream().forEach(instance -> {
                List<ServiceMeta> services = RENEWS.get(instance);
                log.info(" ====== [LizRegistry] : renews instance:{}, services:{}", instance, services);
                Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance), renewPath(services), Long.class);
                log.info(" ====== [LizRegistry] : renews instance:{}, services:{}, timestamp:{}", instance, services, timestamp);
            });
        });
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        healthChecker.consumerChecker(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long newVersion = HttpInvoker.httpGet(versionPath(service), Long.class);
            log.debug(" ====== [LizRegistry] : subscribe version:{}, newVersion:{}", version, newVersion);
            if (newVersion > version) {
                log.info(" ====== [LizRegistry] : subscribe version:{}, newVersion:{}", version, newVersion);
                List<InstanceMeta> instances = fetchAll(service);
                listener.fire(new Event(instances));
                VERSIONS.put(service.toPath(), newVersion);
            }
        });
    }

    private String registerPath(ServiceMeta service) {
        return path(REGISTER_PATH, service);
    }

    private String unregisterPath(ServiceMeta service) {
        return path(UNREGISTER_PATH, service);
    }

    private String findAllPath(ServiceMeta service) {
        return path(FIND_ALL_PATH, service);
    }

    private String versionPath(ServiceMeta service) {
        return path(VERSION_PATH, service);
    }

    private String renewPath(List<ServiceMeta> serviceList) {
        return path(RENEWS_PATH, serviceList);
    }

    private String path(String context, ServiceMeta service) {
        return servers + context + "?service=" + service.toPath();
    }

    private String path(String context, List<ServiceMeta> serviceList) {
        StringBuilder sb = new StringBuilder();
        for (ServiceMeta service : serviceList) {
            sb.append(service.toPath()).append(",");
        }
        String services = sb.toString();
        if (services.endsWith(",")) services = services.substring(0, services.length() - 1);
        return servers + context + "?services=" + services;
    }

}
