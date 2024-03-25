package cn.liz.lizrpc.demo.consumer;

import cn.liz.lizrpc.core.annotation.LizConsumer;
import cn.liz.lizrpc.demo.api.User;
import cn.liz.lizrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Demo2 {
    @LizConsumer
    UserService userService2;

    public void test() {
        User user = userService2.findById(100);
        log.info("===> user2 : " + user);
    }
}
