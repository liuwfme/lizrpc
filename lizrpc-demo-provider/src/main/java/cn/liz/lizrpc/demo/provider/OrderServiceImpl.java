package cn.liz.lizrpc.demo.provider;

import cn.liz.lizrpc.core.annotation.LizProvider;
import cn.liz.lizrpc.demo.api.Order;
import cn.liz.lizrpc.demo.api.OrderService;
import org.springframework.stereotype.Service;

@Service
@LizProvider
public class OrderServiceImpl implements OrderService {
    @Override
    public Order findById(Integer id) {
        if (id == 404) {
            throw new RuntimeException("404 exception");
        }
        return new Order(id.longValue(), 12.3F);
    }
}
