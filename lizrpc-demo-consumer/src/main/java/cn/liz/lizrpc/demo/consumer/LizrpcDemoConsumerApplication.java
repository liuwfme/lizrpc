package cn.liz.lizrpc.demo.consumer;

import cn.liz.lizrpc.core.annotation.LizConsumer;
import cn.liz.lizrpc.core.consumer.ConsumerConfig;
import cn.liz.lizrpc.demo.api.Order;
import cn.liz.lizrpc.demo.api.OrderService;
import cn.liz.lizrpc.demo.api.User;
import cn.liz.lizrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

@SpringBootApplication
@Import({ConsumerConfig.class})
public class LizrpcDemoConsumerApplication {

    @Autowired
    ApplicationContext applicationContext;

    @LizConsumer
    UserService userService;

    @LizConsumer
    OrderService orderService;

    @Autowired
    Demo2 demo2;

    public static void main(String[] args) {
        SpringApplication.run(LizrpcDemoConsumerApplication.class, args);
    }

    @Bean
    public ApplicationRunner consumer_runner() {
        return x -> {
//            User user = userService.findById(1);
//            System.out.println("PRC result userService.findById = " + user);

            userService.getId(11);
            userService.getName();

            System.out.println(userService.toString());

//            Order order = orderService.findById(2);
//            System.out.println("PRC result orderService.findById = " + order);

            //demo2.test();

//            Order order404 = orderService.findById(404);
//            System.out.println("PRC result orderService.findById = " + order404);

        };
    }
}
