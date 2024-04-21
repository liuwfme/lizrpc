# ================== 1.provider
## 创建配置类，管理bean
### ProviderBootStrap：
扫描Provider注解的类，创建代理存根；
把当前服务注册进注册中心；
服务停止时反注册；

### ApplicationRunner：执行providerBootstrap.start方法

### ProviderInvoker：
远程请求的实际执行地方在invoke方法中；
根据请求的方法签名等信息，处理范型，反射调用方法，并返回执行结果；
在请求执行前可以做流控，滑动时间窗口等；

### RegistryCenter：注册中心
start：启动注册中心；
stop；
register
unregister

### ApolloChangedListener：用于刷新配置信息

## 创建配置信息的配置类；
ProviderConfigProperties

# ================== 2.consumer
## 创建配置类，管理bean
### ConsumerBootstrap：
扫描Consumer注解的字段，创建服务存根，指定代理类LizInvocationHandler，
创建 LizInvocationHandler，invoke方法执行代理调用，httpInvoker处理类型转换和序列化，
在调用前做过滤、路由、负载均衡；在路由和负载均衡时可以选择一个半开的服务调用，如果成功就恢复；
在调用后做服务容错（失败重试、故障隔离、故障恢复）；

订阅服务变动事件，重新获取服务提供者列表；

### ApplicationRunner：用于启动 ConsumerBootstrap

### LoadBalancer：

### Router：

### Filter：

### RpcContext：

### RegistryCenter：注册中心
start：启动注册中心；
stop；
fetchAll
subscribe

### ApolloChangedListener：用于刷新配置信息

## 创建配置信息的配置类；
ConsumerConfigProperties

# 
