package cn.liz.lizrpc.demo.provider;

import cn.liz.lizrpc.core.test.TestZKServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LizrpcDemoProviderApplicationTests {

    static TestZKServer zkServer = new TestZKServer();

    @BeforeAll
    public static void init() {
        System.out.println(" ========LizrpcDemoProviderApplicationTests.init.start=========== ");
        zkServer.start();
        System.out.println(" ========LizrpcDemoProviderApplicationTests.init.end=========== ");
    }

    @Test
    void contextLoads() {
        System.out.println(" ========LizrpcDemoProviderApplicationTests.contextLoads=========== ");
    }

    @AfterAll
    public static void destroy() {
        System.out.println(" ========LizrpcDemoProviderApplicationTests.init.start=========== ");
        zkServer.stop();
        System.out.println(" ========LizrpcDemoProviderApplicationTests.init.end=========== ");
    }

}
