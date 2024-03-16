package cn.liz.lizrpc.core.consumer;

import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;
import cn.liz.lizrpc.core.util.MethodUtils;
import cn.liz.lizrpc.core.util.TypeUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LizInvocationHandler implements InvocationHandler {

    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    Class<?> service;

    public LizInvocationHandler(Class<?> clazz) {
        this.service = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);
        RpcResponse rpcResponse = post(rpcRequest);
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            Class<?> type = method.getReturnType();
            System.out.println("method.returnType : " + type);
            if (data instanceof JSONObject jsonResult) {
                if (Map.class.isAssignableFrom(type)) {
                    Map returnMap = new HashMap();
                    Type genericReturnType = method.getGenericReturnType();
                    System.out.println("genericReturnType : " + genericReturnType);
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                        System.out.println("keyType : " + keyType);
                        System.out.println("valueType : " + valueType);
                        jsonResult.entrySet().stream().forEach(e -> {
                            Object key = TypeUtils.cast(e.getKey(), keyType);
                            Object value = TypeUtils.cast(e.getValue(), valueType);
                            returnMap.put(key, value);
                        });
                    }
                    return returnMap;
                }
                return jsonResult.toJavaObject(type);
            } else if (data instanceof JSONArray jsonArray) {
                Object[] array = jsonArray.toArray();
                if (type.isArray()) {
                    Class<?> componentType = type.getComponentType();
                    Object returnArray = Array.newInstance(componentType, array.length);
                    for (int i = 0; i < array.length; i++) {
                        Array.set(returnArray, i, array[i]);
                    }
                    return returnArray;
                } else if (List.class.isAssignableFrom(type)) {
                    List<Object> resultList = new ArrayList<>(array.length);
                    Type genericReturnType = method.getGenericReturnType();
                    System.out.println("genericReturnType : " + genericReturnType);
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Type actualType = parameterizedType.getActualTypeArguments()[0];
                        System.out.println("actualType : " + actualType);
                        for (Object obj : array) {
                            resultList.add(TypeUtils.cast(obj, (Class<?>) actualType));
                        }
                    } else {
                        resultList.addAll(Arrays.asList(array));
                    }
                    return resultList;
                } else {
                    return null;
                }
//                Class<?> componentType = method.getReturnType().getComponentType();//返回元素的类型
////                componentType = method.getReturnType().arrayType();//返回数组的类型
//                System.out.println(componentType);
//                Object resultArray = Array.newInstance(componentType, array.length);
//                for (int i = 0; i < array.length; i++) {
//                    Array.set(resultArray, i, array[i]);
//                }
//                return resultArray;
            } else {
                return TypeUtils.cast(data, type);
            }
        } else {
            Exception ex = rpcResponse.getEx();
//            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest rpcRequest) {
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println("===> reqJson = " + reqJson);
        Request request = new Request.Builder()
                .url("http://localhost:8080/")
                .post(RequestBody.create(reqJson, JSON_TYPE))
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            System.out.println("===> respJson = " + respJson);
            RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
            return rpcResponse;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
