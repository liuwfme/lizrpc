package cn.liz.lizrpc.demo.provider;

import cn.liz.lizrpc.core.annotation.LizProvider;
import cn.liz.lizrpc.demo.api.User;
import cn.liz.lizrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

@Component
@LizProvider
public class UserServiceImpl implements UserService {

    @Override
    public User findById(int id) {
        return new User(id, "Liz-" + System.currentTimeMillis());
    }
}
