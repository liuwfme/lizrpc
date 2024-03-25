package cn.liz.lizrpc.core.util;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

public class MockUtils {
    public static Object mock(Class type) {
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return 1;
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return 10L;
        }
        if (Number.class.isAssignableFrom(type)) {
            return 1;
        }
        if (type.equals(String.class)) {
            return "mock_string";
        }

        return mockPojo(type);
    }

    @SneakyThrows
    private static Object mockPojo(Class type) {
        Object result = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            Object fieldValue = mock(fieldType);
            field.set(result, fieldValue);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(mock(UserDto.class));
    }

    public static class UserDto {
        private int age;
        private String name;

        @Override
        public String toString() {
            return "UserDto{" +
                    "age=" + age +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
