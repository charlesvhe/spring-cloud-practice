# spring cloud 使用 HTTP2

我本人是从 Dubbo 转用 Spring cloud。2016年9月左右刚接触 Spring cloud，那个时候跟大家一样做了很多 dubbo vs Spring cloud 的对比分析。当时最大的疑问是性能对比，问 Josh Long 后续有无支持类似于 dubbo 的其他RPC、序列化协议，他说 HTTP2性能已经足够好了，没有计划支持。当时想到 HTTP2是多路复用，长连接，性能损失仅仅是序列化反序列化的区别，因此就此打住没有深入测试。

上周在 W3上看到张琦的帖子里说到 ServiceCombo以性能问题第一个就把 Spring cloud 淘汰了，加上之前的dubbo vs Spring cloud 性能测试结果，就想到用 HTTP2进一步优化 Spring cloud 性能。

https://mp.weixin.qq.com/s?__biz=MzA5MzQ2NTY0OA==&mid=2650796496&idx=1&sn=a544b76660484b9914b65f038cc39e6d&chksm=88562c8fbf21a5995909ffa9f172f31651b1ebd04897917e43caef3491954e24ed0d0477a5a1&mpshare=1&scene=23&srcid=01245faqrBlQETYK9c7zVmd3#rd

## 启动顺序

### 启动 config
### 启动 eureka
### 启动 provider
### 启动 consumer
JDK9以下不默认支持HTTP2，需要添加启动参数：
> -Xbootclasspath/p:/Users/charles/.m2/repository/org/mortbay/jetty/alpn/alpn-boot/8.1.12-SNAPSHOT/alpn-boot-8.1.12-SNAPSHOT.jar

## 测试 HTTP2

### 测试服务提供者
访问 https://localhost:8443/user
访问 chrome://net-internals/#http2
确认服务器HTTP2支持已经开启

### 测试服务消费者
访问 http://localhost:18090/test
在服务提供者控制台查看日志 是否是 http2 协议
> 2017-12-17 11:36:59.479  INFO 663 --- [  XNIO-2 task-7] c.g.c.s.p.p.controller.UserController    : query all HTTP/2.0  [accept-language:en-US,en;q=0.9]  [upgrade-insecure-requests:1]  [Host:localhost:8443]  [accept-encoding:gzip, deflate, br]  [accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8]  [user-agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36] 


### 测试性能
访问：
> http://localhost:18090/test/count?thread=200&api=test100

thread 可以为任意值  
api为 test10 test100 test1000  
服务消费者控制台查看吞吐量


## 测试 HTTP with SSL 性能对比
provider bootstrap.properties 注释 server.http2.enabled=true
和上面一样进行性能测试

## 测试 HTTP 性能对比
provider bootstrap.properties 注释 server.http2.enabled=true以及下方所有配置项
和上面一样进行性能测试

## 测试 HTTP without Keep-Alive
修改TestController 每次 new RestTemplate (其实在 Header 中指定 Connection=close 可以关闭 Keep-Alive 但是会运行一段时间后超时 error)
> new RestTemplate().getForObject("http://127.0.0.1:8080/user/" + api, String.class);
>
> // restTemplate.getForObject("http://provider/user/" + api, String.class);

## 测试结果 吞吐量/秒

| 测试组合 | HTTP without Keep-Alive | HTTP with Keep-Alive | HTTP+SSL | HTTP2 |  
| T100 O10 | 1210 | 8850 | 3310 | 6320 |  
| T100 O100 | 1115 | 7525 | 2225 | 3410 |  
| T100 O1000 | 950 | 2710 | 1150 | 1080 |  
| T200 O10 | 1050 | 8650+error | 4200+error | 6120 |  
| T200 O100 | 1035 | 7250 | 3330+error | 3250 |  
| T200 O1000 | 870 | 2650 | 495 | 930 |  


分析：

测试结果与 Josh Long 所说不同，HTTP2并不能带来性能的提升。HTTP2 的多路复用相比 HTTP1.1 Keep-Alive 的"单路复用"相比优势并不大，反而带来了 SSL 加解密的性能损失(HTTP2协议本身不要求 SSL，但是目前实现均为 HTTP2强制 SSL)。HTTP2多路复用可以节省链接，避免链接超容器上限。

HTTP2+SSL 相比 HTTP1.1+SSL 有性能优势，但是针对 Spring cloud 内部调用场景我们并不会开启 HTTPS，因此是个废的，在此场景之下最佳选择是 **HTTP1.1 + Keep-Alive**。

