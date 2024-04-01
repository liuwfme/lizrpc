package cn.liz.lizrpc.core.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public class TypeUtils {
    public static Object cast(Object origin, Class<?> type) {
        log.debug("origin type : {}, target type : {}", origin, type);
        if (origin == null) return null;
        Class<?> aClass = origin.getClass();
        if (type.isAssignableFrom(aClass)) {
            return origin;
        }

        if (type.isArray()) {
            if ((origin instanceof List list)) {
                origin = list.toArray();
            }
            int length = Array.getLength(origin);
            Class<?> componentType = type.getComponentType();//返回元素的类型
            Object resultArray = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
                    Array.set(resultArray, i, Array.get(origin, i));
                } else {
                    Object obj = cast(Array.get(origin, i), componentType);
                    Array.set(resultArray, i, obj);
                }
            }
            return resultArray;
        }

        if (origin instanceof HashMap map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(type);
        }

        if (origin instanceof JSONObject jsonObject) {
            return jsonObject.toJavaObject(type);
        }

        if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return Long.valueOf(origin.toString());
        }
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return Integer.valueOf(origin.toString());
        }
        if (type.equals(Float.class) || type.equals(Float.TYPE)) {
            return Float.valueOf(origin.toString());
        }
        if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return Double.valueOf(origin.toString());
        }
        if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
            return Byte.valueOf(origin.toString());
        }
        if (type.equals(Short.class) || type.equals(Short.TYPE)) {
            return Short.valueOf(origin.toString());
        }
        if (type.equals(Character.class) || type.equals(Character.TYPE)) {
            return Character.valueOf(origin.toString().charAt(0));
        }
        if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return Boolean.valueOf(origin.toString());
        }
        return null;
    }

    public static Object castMethodResult(Method method, Object data) {
        log.debug("castMethodResult method:{}, data:{}", method, data);
        Class<?> returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        return castGeneric(data, returnType, genericReturnType);
    }

    public static Object castGeneric(Object data, Class<?> returnType, Type genericReturnType) {
        log.debug("castGeneric data:{}, returnType:{}, genericReturnType:{}", data, returnType, genericReturnType);
//        if (data instanceof JSONObject jsonResult) {
        if (data instanceof Map map) {
            if (Map.class.isAssignableFrom(returnType)) {
                log.debug("castGeneric map convert to map");
                Map returnMap = new HashMap();
                log.debug("genericReturnType : " + genericReturnType);
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                    log.debug("keyType : " + keyType);
                    log.debug("valueType : " + valueType);
                    map.forEach((k, v) -> {
                        Object key = TypeUtils.cast(k, keyType);
                        Object value = TypeUtils.cast(v, valueType);
                        returnMap.put(key, value);
                    });
                }
                return returnMap;
            }
            if (data instanceof JSONObject jsonObject) {
                log.debug("castGeneric JSONObject convert to JavaObject");
                return jsonObject.toJavaObject(returnType);
            } else if (!Map.class.isAssignableFrom(returnType)) {
                log.debug("castGeneric map convert to JavaObject");
                return new JSONObject(map).toJavaObject(returnType);
            } else {
                log.debug("castGeneric map convert to unknown");
                return data;
            }
        } else if (data instanceof List list) {
            Object[] array = list.toArray();
            if (returnType.isArray()) {
                log.debug("castGeneric list convert to array");
                Class<?> componentType = returnType.getComponentType();
                Object returnArray = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
                        Array.set(returnArray, i, array[i]);
                    } else {
                        Object obj = TypeUtils.cast(array[i], componentType);
                        Array.set(returnArray, i, obj);
                    }
                }
                return returnArray;
            } else if (List.class.isAssignableFrom(returnType)) {
                log.debug("castGeneric list convert to list");
                List<Object> resultList = new ArrayList<>(array.length);
                log.debug("genericReturnType : " + genericReturnType);
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type actualType = parameterizedType.getActualTypeArguments()[0];
                    log.debug("actualType : " + actualType);
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
        } else {
            return TypeUtils.cast(data, returnType);
        }
    }

}
