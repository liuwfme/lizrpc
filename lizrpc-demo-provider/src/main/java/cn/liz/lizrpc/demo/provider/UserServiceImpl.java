package cn.liz.lizrpc.demo.provider;

import cn.liz.lizrpc.core.annotation.LizProvider;
import cn.liz.lizrpc.core.api.RpcContext;
import cn.liz.lizrpc.demo.api.User;
import cn.liz.lizrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@LizProvider
public class UserServiceImpl implements UserService {

    @Autowired
    Environment environment;

    @Override
    public User findById(int id) {
        return new User(id, "Liz-v1-" + environment.getProperty("server.port") + "_" + System.currentTimeMillis());
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

    @Override
    public User[] getUsers(User[] users) {
        return users;
    }

    @Override
    public List<User> getList(List<User> userList) {
        System.out.println("userList.getClass : " + userList.getClass());
//        userList.forEach(s -> System.out.println("userList.foreach.getClass : " + s.getClass()));
//        System.out.println("userList.get(0).getClass : " + userList.get(0).getClass());
//        User[] users = userList.toArray(new User[userList.size()]);
        User[] users = userList.toArray(new User[0]);
        System.out.println("getList() userList.toArray : ");
        Arrays.stream(users).forEach(System.out::println);
        userList.add(new User(303, "liz303"));
        return userList;
    }

    @Override
    public Map<String, User> getMap(Map<String, User> userMap) {
        System.out.println("getMap() userMap.values().class : ");
        userMap.values().forEach(v -> System.out.println(v.getClass()));
//        User[] users = userMap.values().toArray(new User[userMap.size()]);
        User[] users = userMap.values().toArray(new User[0]);
        System.out.println("getMap() userMap.values.toArray : ");
        Arrays.stream(users).forEach(System.out::println);
        userMap.put("p333", new User(333, "liz333"));
        return userMap;
    }

    @Override
    public Boolean getFlag(boolean flag) {
        return !flag;
    }

    @Override
    public User findById(long id) {
        return new User(Long.valueOf(id).intValue(), "Liz");
    }

    @Override
    public User ex(boolean flag) {
        if (flag) throw new RuntimeException("just throw an exception");
        return new User(100, "Liz100");
    }

    @Value("${timeoutPorts}")
    private String timeoutPorts;

    @Override
    public User findTimeout(int timeout) {
        String port = environment.getProperty("server.port");
//        if ("8081".equals(port)) {
        if (Arrays.asList(timeoutPorts.split(",")).contains(port)) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new User(111, "liz-" + port);
    }

    @Override
    public void setTimeoutPorts(String ports) {
        this.timeoutPorts = timeoutPorts;
    }

    @Override
    public String echoParameter(String key) {
        System.out.println(" ====>> RpcContext.contextParameters: ");
        RpcContext.contextParameters.get().forEach((k, v) -> System.out.println(k + " -> " + v));
        return RpcContext.getContextParameter(key);
    }
}
