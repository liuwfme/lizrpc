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

            User user = userService.findById(1);
            System.out.println("PRC result userService.findById = " + user);

            User user1 = userService.findById(1, "lwf");
            System.out.println("PRC result userService.findById = " + user1);

            System.out.println("--> userService.getName : " + userService.getName());
            System.out.println("--> userService.getName(123) : " + userService.getName(123));

            userService.getId(11);
            userService.getName();

            System.out.println("--> userService.toString : " + userService.toString());

            Order order = orderService.findById(2);
            System.out.println("PRC result orderService.findById(2) = " + order);

            demo2.test();

//            Order order404 = orderService.findById(404);
//            System.out.println("PRC result orderService.findById = " + order404);

            System.out.println("--> userService.getId(33) : " + userService.getId(33));
            System.out.println("--> userService.getId(new User(123, \"lwf\")) : " + userService.getId(new User(123, "lwf")));
            System.out.println("--> userService.getId(12f) : " + userService.getId(12f));
            System.out.println("--> userService.userService.getIds() : " + Arrays.toString(userService.getIds()));

            System.out.println("--> userService.getLongIds() : " + userService.getLongIds());
            for (long id : userService.getLongIds()) {
                System.out.println(id);
            }

            System.out.println("--> userService.getIds(int[] ids) : " + userService.getIds(new int[]{5, 6, 7}));
            for (int id : userService.getIds(new int[]{4, 5, 6})) {
                System.out.println(id);
            }

        };
    }
}