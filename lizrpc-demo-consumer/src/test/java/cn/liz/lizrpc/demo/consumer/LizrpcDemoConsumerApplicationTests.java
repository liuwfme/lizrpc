package cn.liz.lizrpc.demo.consumer;

import cn.liz.lizrpc.core.test.TestZKServer;
import cn.liz.lizrpc.demo.provider.LizrpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(classes = {LizrpcDemoConsumerApplication.class})
class LizrpcDemoConsumerApplicationTests {

    static ApplicationContext context;

    static TestZKServer zkServer = new TestZKServer();

    @BeforeAll
    static void init() {
        System.out.println(" =============LizrpcDemoConsumerApplicationTests.init.start========================= ");
        zkServer.start();
        context = SpringApplication.run(LizrpcDemoProviderApplication.class
                , "--server.port=8094"
                , "--lizrpc.zkServer=localhost:2182"
                , "--logging.level.cn.liz.lizrpc=debug"
        );
        System.out.println(" =============LizrpcDemoConsumerApplicationTests.init.end========================= ");
    }

    @Test
    void contextLoads() {
        System.out.println("======LizrpcDemoConsumerApplicationTests.contextLoads======");
    }

    @AfterAll
    static void destroy() {
        System.out.println(" =============LizrpcDemoConsumerApplicationTests.destroy.start========================= ");
        SpringApplication.exit(context, () -> 1);
        zkServer.stop();
        System.out.println(" =============LizrpcDemoConsumerApplicationTests.destroy.end========================= ");
    }

}
