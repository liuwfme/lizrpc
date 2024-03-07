package cn.liz.lizrpc.core.api;

import lombok.Data;

@Data
public class RpcRequest {
    private String service;// 接口：cn.liz.lizrpc.demo.api.UserService
    private String method;// 方法：findById
    private Object[] args;// 参数：100
}
