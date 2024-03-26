package cn.liz.lizrpc.core.test;

import lombok.SneakyThrows;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.utils.CloseableUtils;

public class TestZKServer {

    TestingCluster cluster;

    @SneakyThrows
    public void start() {
        InstanceSpec instanceSpec = new InstanceSpec(null, 2182, -1, -1,
                true, -1, -1, -1);
        cluster = new TestingCluster(instanceSpec);
        System.out.println("-----------TestZKServer start...");
        cluster.start();
        cluster.getServers().forEach(s -> System.out.println("======zk cluster instance : " + s.getInstanceSpec()));
        System.out.println("-----------TestZKServer started...");
    }

    @SneakyThrows
    public void stop() {
        System.out.println("-----------TestZKServer stop...");
        cluster.stop();
        CloseableUtils.closeQuietly(cluster);
        System.out.println("-----------TestZKServer stopped...");
    }
}
