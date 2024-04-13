package cn.liz.lizrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
//@Configuration
@ConfigurationProperties(prefix = "lizrpc.provider")
public class ProviderConfigProperties {
    Map<String, String> metas = new HashMap<>();
}
