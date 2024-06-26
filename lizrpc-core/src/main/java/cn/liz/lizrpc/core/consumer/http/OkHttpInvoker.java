package cn.liz.lizrpc.core.consumer.http;

import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;
import cn.liz.lizrpc.core.consumer.HttpInvoker;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;

    public OkHttpInvoker(int timeout) {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Override
    public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        log.debug("===> OkHttpInvoker#post reqJson:{}", reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSON_TYPE))
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug("===> OkHttpInvoker#post respJson:{} ", respJson);
            RpcResponse<Object> rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
            return rpcResponse;
        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RpcException(e, RpcException.ErrCodeEnum.SocketTimeout.getCode());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String post(String requestStr, String url) {
        log.debug("===> OkHttpInvoker#post requestStr:{},url:{}", requestStr, url);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestStr, JSON_TYPE))
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug("===> OkHttpInvoker#post respJson:{}", requestStr);
            return respJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String url) {
        log.debug("===> OkHttpInvoker#get url:{}", url);
        Request request = new Request.Builder().url(url).get().build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug("===> OkHttpInvoker#get respJson:{}", respJson);
            return respJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
