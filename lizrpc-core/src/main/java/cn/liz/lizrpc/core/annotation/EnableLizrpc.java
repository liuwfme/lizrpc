package cn.liz.lizrpc.core.annotation;

import cn.liz.lizrpc.core.config.ConsumerConfig;
import cn.liz.lizrpc.core.config.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启动类组合一个入口
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableLizrpc {
}
