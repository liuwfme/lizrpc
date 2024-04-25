package cn.liz.lizrpc.core.consumer;

import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;
import cn.liz.lizrpc.core.consumer.http.OkHttpInvoker;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker DEFAULT = new OkHttpInvoker(500);

    RpcResponse<?> post(RpcRequest rpcRequest, String url);

    String post(String requestStr, String url);

    String get(String url);

    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug(" ====== httpGet: " + url);
        String respJson = DEFAULT.get(url);
        log.debug(" ====== httpGet resp : {} " + respJson);
        return JSON.parseObject(respJson, clazz);
    }

    static <T> T httpGet(String url, TypeReference<T> typeReference) {
        log.debug(" ====== httpGet: " + url);
        String respJson = DEFAULT.get(url);
        log.debug(" ====== httpGet resp : {} " + respJson);
        return JSON.parseObject(respJson, typeReference);
    }

    static <T> T httpPost(String requestStr, String url, Class<T> clazz) {
        log.debug(" ====== httpPost: " + url);
        String respJson = DEFAULT.post(requestStr, url);
        log.debug(" ====== httpPost resp : {} " + respJson);
        return JSON.parseObject(respJson, clazz);
    }

}
