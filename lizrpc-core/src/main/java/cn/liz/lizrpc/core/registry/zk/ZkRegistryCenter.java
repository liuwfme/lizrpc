package cn.liz.lizrpc.core.registry.zk;

import cn.liz.lizrpc.core.api.RegistryCenter;
import cn.liz.lizrpc.core.api.RpcException;
import cn.liz.lizrpc.core.meta.InstanceMeta;
import cn.liz.lizrpc.core.meta.ServiceMeta;
import cn.liz.lizrpc.core.registry.ChangedListener;
import cn.liz.lizrpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    @Value("${lizrpc.zk.server:localhost:2181}")
    private String servers;

    @Value("${lizrpc.zk.root:lizrpc}")
    private String zkRoot;

    private CuratorFramework client = null;
    private List<TreeCache> caches = new ArrayList<>();

    private boolean running = false;

    @Override
    public void start() {
        if (running) {
            log.info(" ===> zk client has started to server[" + servers + "/" + zkRoot + "], ignored.");
            return;
        }
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(zkRoot)
                .retryPolicy(retryPolicy)
                .build();
        log.info(" ===> zk client starting,, server[" + servers + "/" + zkRoot + "]");
        client.start();
        running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            log.info(" ===> zk client isn't running to server[" + servers + "/" + zkRoot + "], ignored.");
            return;
        }
        log.info(" ===> zk tree cache closed.");
        caches.forEach(TreeCache::close);
        log.info(" ===> zk client stopping...");
        client.close();
        running = false;
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.toMetas().getBytes());
            }
            // 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info("===> register to zk : " + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.toMetas().getBytes());
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 判断服务是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            // 删除实例节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info("===> unregister from zk : " + instancePath);
//            client.delete().forPath(instancePath);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info(" ===> fetchAll from zk : " + servicePath);
            nodes.forEach(System.out::println);
            return mapInstances(nodes, servicePath);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    private List<InstanceMeta> mapInstances(List<String> nodes, String servicePath) {
        return nodes.stream().map(node -> {
            String[] strings = node.split("_");
            InstanceMeta instance = InstanceMeta.http(strings[0], Integer.valueOf(strings[1]));
            log.info("mapInstances instance : {}", instance.toUrl());
            String nodePath = servicePath + "/" + node;
            byte[] bytes;
            try {
                bytes = client.getData().forPath(nodePath);
            } catch (Exception e) {
                log.warn("client.getData().forPath exception, e: ", e);
                throw new RuntimeException(e);
            }
//            HashMap params = JSON.parseObject(new String(bytes), HashMap.class);
            Map<String, Object> params = JSON.parseObject(new String(bytes));
            Map<String, String> stringParams = new HashMap<>();
            params.forEach((k, v) -> {
                log.info("mapInstances params k:{},v:{}", k, v);
//                stringParams.put(k, v == null ? null : v.toString());
                instance.getParameters().put(k, v == null ? null : v.toString());
            });
//            instance.setParameters(stringParams);
            return instance;
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true)
                .setMaxDepth(2)
                .build();
        cache.getListenable().addListener(
                (curator, event) -> {
                    synchronized (ZkRegistryCenter.class) {
                        if (running) {
                            // 有任何节点变动，这里会执行
                            log.info("zk subscribe event : " + event);
                            List<InstanceMeta> nodes = fetchAll(service);
                            listener.fire(new Event(nodes));
                        }
                    }
                }
        );
        cache.start();
        caches.add(cache);
    }
}
