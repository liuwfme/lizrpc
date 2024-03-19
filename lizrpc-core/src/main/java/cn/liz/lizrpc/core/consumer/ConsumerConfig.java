package cn.liz.lizrpc.core.consumer;

import cn.liz.lizrpc.core.api.LoadBalancer;
import cn.liz.lizrpc.core.api.Router;
import cn.liz.lizrpc.core.cluster.RandomLoadBalancer;
import cn.liz.lizrpc.core.cluster.RoundRibonLoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrap_runner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            System.out.println("consumerBootstrap starting ...");
            consumerBootstrap.start();
            System.out.println("consumerBootstrap started ...");
        };
    }

    @Bean
    public LoadBalancer loadBalancer() {
//        return LoadBalancer.Default;
//        return new RandomLoadBalancer();
        return new RoundRibonLoadBalancer();
    }

    @Bean
    public Router router() {
        return Router.Default;
    }
}
