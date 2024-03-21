package cn.liz.lizrpc.core.meta;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 描述provider的映射关系
 */
@Data
@Builder
public class ProviderMeta {

    Method method;
    String methodSign;
    Object serviceImpl;

}
