package cn.liz.lizrpc.core.config;

import cn.liz.lizrpc.core.api.*;
import cn.liz.lizrpc.core.cluster.GrayRouter;
import cn.liz.lizrpc.core.cluster.RoundRibonLoadBalancer;
import cn.liz.lizrpc.core.consumer.ConsumerBootstrap;
import cn.liz.lizrpc.core.filter.ContextParameterFilter;
import cn.liz.lizrpc.core.meta.InstanceMeta;
import cn.liz.lizrpc.core.registry.RegistryCenter;
import cn.liz.lizrpc.core.registry.liz.LizRegistryCenter;
import cn.liz.lizrpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
@Slf4j
@Import({AppConfigProperties.class, ConsumerConfigProperties.class})
public class ConsumerConfig {

    @Autowired
    AppConfigProperties appConfigProperties;

    @Autowired
    ConsumerConfigProperties consumerConfigProperties;

//    @Value("${lizrpc.providers}")
//    String servers;
//
//    @Value("${app.grayRatio}")
//    private int grayRatio;

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE + 1)
    public ApplicationRunner consumerBootstrap_runner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            log.info("consumerBootstrap starting ...");
            consumerBootstrap.start();
            log.info("consumerBootstrap started !");
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
//        return LoadBalancer.Default;
        return new RoundRibonLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> router() {
//        return Router.Default;
        return new GrayRouter(consumerConfigProperties.getGrayRatio());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumer_rc() {
//        return new RegistryCenter.StaticRegistryCenter(List.of(servers.split(",")));
//        return new ZkRegistryCenter();
        return new LizRegistryCenter();
    }

    @Bean
    public Filter defaultFilter() {
//        return Filter.Default;
        return new ContextParameterFilter();
    }

//    @Bean
//    public Filter filter1() {
//        return new CacheFilter();
//    }

//    @Bean
//    public Filter filter2() {
//        return new MockFilter();
//    }

    @Bean
    @RefreshScope
    public RpcContext createContext(@Autowired Router router,
                                    @Autowired LoadBalancer loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);

        context.getParameters().put("app.id", appConfigProperties.getId());
        context.getParameters().put("app.namespace", appConfigProperties.getNamespace());
        context.getParameters().put("app.env", appConfigProperties.getEnv());

//        context.getParameters().put("consumer.retries", String.valueOf(consumerConfigProperties.getRetries()));
//        context.getParameters().put("consumer.timeout", String.valueOf(consumerConfigProperties.getTimeout()));
//        context.getParameters().put("consumer.faultLimit", String.valueOf(consumerConfigProperties.getFaultLimit()));
//        context.getParameters().put("consumer.halfOpenInitialDelay", String.valueOf(consumerConfigProperties.getHalfOpenInitialDelay()));
//        context.getParameters().put("consumer.halfOpenDelay", String.valueOf(consumerConfigProperties.getHalfOpenDelay()));
        context.setConsumerConfigProperties(consumerConfigProperties);
        return context;
    }

    @Bean
    @ConditionalOnMissingBean
    public ApolloChangedListener apolloChangedListener() {
        return new ApolloChangedListener();
    }
}
