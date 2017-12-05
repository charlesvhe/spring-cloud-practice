# spring cloud 实践

## 启动顺序

### 启动 config
### 启动 eureka
### 启动 provider
### 启动 consumer
添加启动参数：-Xbootclasspath/p:/Users/charles/.m2/repository/org/mortbay/jetty/alpn/alpn-boot/8.1.12-SNAPSHOT/alpn-boot-8.1.12-SNAPSHOT.jar

## 测试 HTTP2

### 测试服务提供者
访问 https://localhost:8443/user
访问 chrome://net-internals/#http2
确认服务器HTTP2支持已经开启

### 测试服务消费者
访问 http://localhost:18090/test
在服务提供者控制台查看日志 是否是 http2 协议

### 测试性能
访问 http://localhost:18090/test/count?thread=200&api=test100
thread 可以为任意值
api为 test10 test100 test1000
服务消费者控制台查看吞吐量

## 测试 HTTP 性能对比
provider bootstrap.properties 注释 server.http2.enabled=true以及下方所有配置项
CoreAutoConfiguration 注释 public RestTemplate restTemplate() 方法内的 okhttp3配置
和上面一样进行性能测试








