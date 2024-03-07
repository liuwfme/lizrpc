package cn.liz.lizrpc.demo.provider;

import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;
import cn.liz.lizrpc.core.provider.ProviderBootstrap;
import cn.liz.lizrpc.core.provider.ProviderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class LizrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LizrpcDemoProviderApplication.class, args);
    }

    //使用HTTP+JSON来实现序列化和通信

    @Autowired
    ProviderBootstrap providerBootstrap;

    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
//        return invokeRequest(request);
        return providerBootstrap.invoke(request);
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("cn.liz.lizrpc.demo.api.UserService");
            request.setMethod("findById");
            request.setArgs(new Object[]{100});

            RpcResponse response = invoke(request);
            System.out.println("return : " + response.getData());
        };
    }
}
