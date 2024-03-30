package cn.liz.lizrpc.demo.provider;

import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;
import cn.liz.lizrpc.core.provider.ProviderConfig;
import cn.liz.lizrpc.core.provider.ProviderInvoker;
import cn.liz.lizrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    ProviderInvoker providerInvoker;

    @Autowired
    UserService userService;

    @RequestMapping("/")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

    @RequestMapping("/setTimeoutPorts")
    public RpcResponse<String> setTimeoutPorts(@RequestParam("timeoutPorts") String timeoutPorts) {
        userService.setTimeoutPorts(timeoutPorts);
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("ok, timeoutPorts=" + timeoutPorts);
        return response;
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("cn.liz.lizrpc.demo.api.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100});
            RpcResponse<Object> response = invoke(request);
            System.out.println("return : " + response.getData());

            RpcRequest request1 = new RpcRequest();
            request1.setService("cn.liz.lizrpc.demo.api.UserService");
            request1.setMethodSign("findById@2_int_java.lang.String");
            request1.setArgs(new Object[]{100, "lwf"});
            RpcResponse<Object> response1 = invoke(request1);
            System.out.println("return : " + response1.getData());

        };
    }
}
