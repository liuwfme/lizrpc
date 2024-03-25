package cn.liz.lizrpc.demo.consumer;

import cn.liz.lizrpc.demo.provider.LizrpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class LizrpcDemoConsumerApplicationTests {

    static ApplicationContext context;

    @BeforeAll
    static void init() {
        System.out.println(" =============LizrpcDemoConsumerApplicationTests.init.start========================= ");
        context = SpringApplication.run(LizrpcDemoProviderApplication.class, "--server.port=8884"
                , "--logging.level.cn.liz.lizrpc=debug"
        );
        System.out.println(" =============LizrpcDemoConsumerApplicationTests.init.end========================= ");
    }

    @Test
    void contextLoads() {
    }

    @AfterAll
    static void destroy() {
        System.out.println(" =============LizrpcDemoConsumerApplicationTests.destroy.start========================= ");
        SpringApplication.exit(context, () -> 1);
        System.out.println(" =============LizrpcDemoConsumerApplicationTests.destroy.end========================= ");
    }

}
