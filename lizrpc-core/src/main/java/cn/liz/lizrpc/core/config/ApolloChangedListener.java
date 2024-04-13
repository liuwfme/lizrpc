package cn.liz.lizrpc.core.config;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@Data
public class ApolloChangedListener implements ApplicationContextAware {

    ApplicationContext applicationContext;

    @ApolloConfigChangeListener({"lizrpc-provider", "lizrpc-consumer", "application"})
    private void changeHandler(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            log.info("====== apollo change : {}", change);
        }

        applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
    }
}
