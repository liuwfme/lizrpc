package cn.liz.lizrpc.core.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
//@Configuration
@ToString
@ConfigurationProperties(prefix = "lizrpc.consumer")
public class ConsumerConfigProperties {
    private int retries = 1;
    private int timeout = 1000;
    private int faultLimit = 10;
    private int halfOpenInitialDelay = 10000;
    private int halfOpenDelay = 60000;
    private int grayRatio = 0;
}
