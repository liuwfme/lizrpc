package cn.liz.lizrpc.core.util;

import cn.liz.lizrpc.core.annotation.LizConsumer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static List<Field> findAnnotatedField(Class<?> aClass, Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<>();
        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(annotationClass)) {
                    result.add(f);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return result;
    }

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(
                m -> System.out.println(methodSign(m))
        );
    }
}
