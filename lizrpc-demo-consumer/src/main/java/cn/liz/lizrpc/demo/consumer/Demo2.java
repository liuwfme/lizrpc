package cn.liz.lizrpc.demo.consumer;

import cn.liz.lizrpc.core.annotation.LizConsumer;
import cn.liz.lizrpc.demo.api.User;
import cn.liz.lizrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

@Component
public class Demo2 {
    @LizConsumer
    UserService userService2;

    public void test() {
        User user = userService2.findById(100);
        System.out.println("===> user2 : " + user);
    }
}
