package cn.liz.lizrpc.core.util;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodUtils {
    public static boolean checkLocalMethod(final String method) {
        // 本地方法不代理
        return "toString".equals(method)
                || "hashCode".equals(method)
                || "equals".equals(method)
                || "getClass".equals(method)
                || "wait".equals(method)
                || "notify".equals(method)
                || "notifyAll".equals(method);
    }

    public static boolean checkLocalMethod(final Method method) {
        // 本地方法不代理
        return method.getDeclaringClass().equals(Object.class);
    }

    public static String methodSign(Method method) {
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                c -> sb.append("_").append(c.getCanonicalName())
        );
        return sb.toString();
    }

    public static String methodSign(Method method, Class clazz) {
        return null;
    }

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(
                m -> System.out.println(methodSign(m))
        );
    }
}
