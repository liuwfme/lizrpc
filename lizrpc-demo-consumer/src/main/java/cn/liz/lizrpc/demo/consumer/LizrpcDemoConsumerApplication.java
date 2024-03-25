package cn.liz.lizrpc.demo.consumer;

import cn.liz.lizrpc.core.annotation.LizConsumer;
import cn.liz.lizrpc.core.consumer.ConsumerConfig;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@Import({ConsumerConfig.class})
@RestController
public class LizrpcDemoConsumerApplication {

    @Autowired
    ApplicationContext applicationContext;

    @LizConsumer
    UserService userService;

    @LizConsumer
    OrderService orderService;

    @Autowired
    Demo2 demo2;

    @RequestMapping("/")
    public User findById(@RequestParam("id") int id) {
        return userService.findById(id);
    }

    public static void main(String[] args) {
        SpringApplication.run(LizrpcDemoConsumerApplication.class, args);
    }

    @Bean
    public ApplicationRunner consumer_runner() {
        return x -> {
//            testAllCase();
        };
    }

    private void testAllCase() {
        System.out.println("------------------------------------------------------------");
        // 常规int类型，返回User对象
        System.out.println("Case 1. >>===[常规int类型，返回User对象]===");
        User user = userService.findById(1);
        System.out.println("PRC result userService.findById = " + user);
        System.out.println();

//            Order order = orderService.findById(2);
//            System.out.println("PRC result orderService.findById(2) = " + order);
//            demo2.test();
//            Order order404 = orderService.findById(404);
//            System.out.println("PRC result orderService.findById = " + order404);

        // 方法重载，同名不同参
        System.out.println("Case 2. >>===[测试方法重载，同名方法，参数不同===");
        User user1 = userService.findById(1, "lwf");
        System.out.println("PRC result userService.findById = " + user1);
        System.out.println();

        // 返回字符串
        System.out.println("Case 3. >>===[测试返回字符串]===");
        System.out.println("--> 返回字符串 userService.getName : " + userService.getName());
        System.out.println();

        // 重载方法返回字符串
        System.out.println("Case 4. >>===[测试重载方法返回字符串]===");
        System.out.println("--> 重载方法返回字符串 userService.getName(123) : " + userService.getName(123));
        System.out.println();

        // toString方法
        System.out.println("Case 5. >>===[测试local toString方法]===");
        System.out.println("--> userService.toString : " + userService.toString());
        System.out.println();

        // 参数是long类型
        System.out.println("Case 6. >>===[常规int类型，返回User对象]===");
        System.out.println("--> 参数是long类型 userService.getId(33) : " + userService.getId(33));
        System.out.println();

        // 参数是float，返回是long
        System.out.println("Case 7. >>===[测试long+float类型]===");
        System.out.println("--> 参数是float，返回是long userService.getId(12f) : " + userService.getId(12f));
        System.out.println();

        // 参数是User对象
        System.out.println("Case 8. >>===[测试参数是User类型]===");
        System.out.println("--> 参数是User对象 userService.getId(new User(123, \"lwf\")) : "
                + userService.getId(new User(123, "lwf")));
        System.out.println();

        System.out.println("--> 返回int数组 userService.userService.getIds() : " + Arrays.toString(userService.getIds()));
        System.out.println();

        System.out.println("Case 9. >>===[测试返回long[]]===");
        System.out.println("--> 返回long数组 userService.getLongIds() : ");
        for (long id : userService.getLongIds()) {
            System.out.println(id);
        }
        System.out.println();

        System.out.println("Case 10. >>===[测试参数和返回值都是 int[]]===");
        System.out.println("--> 参数是int数组，返回int数组 userService.getIds(int[] ids) : ");
        for (int id : userService.getIds(new int[]{4, 5, 6})) {
            System.out.println(id);
        }
        System.out.println();

        // 参数和返回值都是List类型
        System.out.println("Case 11. >>===[测试参数和返回值都是List类型]===");
        List<User> list = userService.getList(List.of(new User(11, "name11"), new User(22, "name22")));
        System.out.println("--> 参数和返回值都是List类型 : ");
        list.forEach(System.out::println);
        System.out.println();

        // 参数和返回值都是Map类型
        System.out.println("Case 12. >>===[测试参数和返回值都是Map类型]===");
        Map<String, User> map = new HashMap<>();
        map.put("m111", new User(111, "name111"));
        map.put("m222", new User(222, "name222"));
        Map<String, User> userMap = userService.getMap(map);
        System.out.println("--> 参数和返回值都是Map类型 : ");
        userMap.forEach((k, v) -> System.out.println("k:" + k + ", v:" + v));
        System.out.println();

        System.out.println("Case 13. >>===[测试参数和返回值都是Boolean/boolean类型]===");
        System.out.println("userService.getFlag(false) = " + userService.getFlag(false));
        System.out.println();

        System.out.println("case 14. >>===[测试参数和返回值都是User[]类型]===");
        User[] users = new User[]{
                new User(11, "liz11"),
                new User(22, "liz22")
        };
        Arrays.stream(userService.getUsers(users)).forEach(System.out::println);
        System.out.println();

        System.out.println("Case 15. >>===[测试参数为long，返回值是User类型]===");
        User userLong = userService.findById(10000L);
        System.out.println(userLong);
        System.out.println();

        System.out.println("Case 16. >>===[测试参数为boolean，返回值都是User类型]===");
        User user100 = userService.ex(false);
        System.out.println(user100);
        System.out.println();

        System.out.println("Case 17. >>===[测试服务端抛出一个RuntimeException异常]===");
        try {
            User userEx = userService.ex(true);
            System.out.println(userEx);
        } catch (RuntimeException e) {
            System.out.println(" ===> exception: " + e.getMessage());
        }
        System.out.println();
    }
}
