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

    @Override
    public User findById(int id, String name) {
        return new User(id, "Liz-" + name + "_" + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return user.getId().longValue();
    }

    @Override
    public long getId(Float id) {
        return id.longValue();
    }

    @Override
    public String getName() {
        return "testName";
    }

    @Override
    public String getName(int id) {
        return "lwf-" + id;
    }

    @Override
    public int[] getIds() {
        return new int[]{1, 2, 3};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{1, 2, 3, 4};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }
}