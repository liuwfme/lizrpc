package cn.liz.lizrpc.core.provider;

import cn.liz.lizrpc.core.api.RegistryCenter;
import cn.liz.lizrpc.core.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ProviderConfig {
    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrap_runner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            System.out.println("providerBootstrap starting...");
            providerBootstrap.start();
            System.out.println("providerBootstrap started...");
        };
    }

    @Bean//(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter provider_rc() {
        return new ZkRegistryCenter();
    }
}
