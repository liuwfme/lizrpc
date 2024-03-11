package cn.liz.lizrpc.core.util;

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
}
