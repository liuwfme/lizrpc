package cn.liz.lizrpc.demo.provider;

import cn.liz.lizconfig.client.annotation.EnableLizConfig;
import cn.liz.lizrpc.core.api.RpcRequest;
import cn.liz.lizrpc.core.api.RpcResponse;
import cn.liz.lizrpc.core.config.ProviderConfig;
import cn.liz.lizrpc.core.config.ProviderConfigProperties;
import cn.liz.lizrpc.core.transport.SpringBootTransport;
import cn.liz.lizrpc.demo.api.User;
import cn.liz.lizrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
//@EnableApolloConfig
@EnableLizConfig
@Import({ProviderConfig.class})
public class LizrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LizrpcDemoProviderApplication.class, args);
    }

    //使用HTTP+JSON来实现序列化和通信

//    @Autowired
//    ProviderInvoker providerInvoker;

    @Autowired
    UserService userService;

    @Autowired
    SpringBootTransport transport;

//    @RequestMapping("/")
//    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
//        return providerInvoker.invoke(request);
//    }

    @RequestMapping("/setTimeoutPorts")
    public RpcResponse<String> setTimeoutPorts(@RequestParam("timeoutPorts") String timeoutPorts) {
        userService.setTimeoutPorts(timeoutPorts);
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("ok, timeoutPorts=" + timeoutPorts);
        return response;
    }

    @Autowired
    Environment environment;

    @RequestMapping("/getConfVal")
    public RpcResponse<String> getConfVal(@RequestParam("key") String key) {
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("ok, key=" + key + ";value=" + environment.getProperty(key));
        return response;
    }

    @Autowired
    ProviderConfigProperties providerConfigProperties;

    @RequestMapping("/meta")
    public RpcResponse<String> getMetas() {
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("ok, meta = " + providerConfigProperties.getMetas());
        return response;
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            testAll();
        };
    }

    private void testAll() {

        String a = environment.getProperty("a");
        System.out.println("======================================================");
        System.out.println("--------a:" + a);
        System.out.println("--------b:" + environment.getProperty("b"));
        System.out.println("======================================================");

        //  test 1 parameter method
        System.out.println("Provider Case 1. >>===[基本测试：1个参数]===");
        RpcRequest request = new RpcRequest();
        request.setService("cn.liz.lizrpc.demo.api.UserService");
        request.setMethodSign("findById@1_int");
        request.setArgs(new Object[]{100});
        RpcResponse<Object> response = transport.invoke(request);
        System.out.println("return : " + response.getData());
        System.out.println();

        // test 2 parameters method
        System.out.println("Provider Case 2. >>===[基本测试：2个参数]===");
        RpcRequest request1 = new RpcRequest();
        request1.setService("cn.liz.lizrpc.demo.api.UserService");
        request1.setMethodSign("findById@2_int_java.lang.String");
        request1.setArgs(new Object[]{100, "lwf"});
        RpcResponse<Object> response1 = transport.invoke(request1);
        System.out.println("return : " + response1.getData());
        System.out.println();

        // test 3 for List<User> method&parameter
        System.out.println("Provider Case 3. >>===[复杂测试：参数类型为List<User>]===");
        RpcRequest request3 = new RpcRequest();
        request3.setService("cn.liz.lizrpc.demo.api.UserService");
        request3.setMethodSign("getList@1_java.util.List");
        List<User> list = new ArrayList<>();
        list.add(new User(201, "liz201"));
        list.add(new User(202, "liz202"));
        request3.setArgs(new Object[]{list});
        RpcResponse<Object> response3 = transport.invoke(request3);
        System.out.println("return : " + response3.getData());
        System.out.println();

        // test 4 for Map<String, User> method&parameter
        System.out.println("Provider Case 4. >>===[复杂测试：参数类型为Map<String, User>]===");
        RpcRequest request4 = new RpcRequest();
        request4.setService("cn.liz.lizrpc.demo.api.UserService");
        request4.setMethodSign("getMap@1_java.util.Map");
        Map<String, User> map = new HashMap<>();
        map.put("m301", new User(301, "liz301"));
        map.put("m302", new User(302, "liz302"));
        request4.setArgs(new Object[]{map});
        RpcResponse<Object> response4 = transport.invoke(request4);
        System.out.println("return : " + response4.getData());
        System.out.println();

        // test 5 for traffic control
        System.out.println("Provider Case 5. >>===[复杂测试：测试流量并发控制]===");
//        for (int i = 0; i < 120; i++) {
//            try {
//                Thread.sleep(1000);
//                RpcResponse<Object> r = transport.invoke(request);
//                System.out.println(i + " ***>>> " + r.getData());
//            } catch (RpcException e) {
//                // ignore
//                System.out.println(i + " ***>>> " + e.getMessage() + " -> " + e.getErrCode());
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
        System.out.println();

    }
}


