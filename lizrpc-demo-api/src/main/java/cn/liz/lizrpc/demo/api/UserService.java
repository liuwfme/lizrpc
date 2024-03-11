package cn.liz.lizrpc.demo.api;

public interface UserService {
    User findById(int id);

    int getId(int id);

    String getName();
}
