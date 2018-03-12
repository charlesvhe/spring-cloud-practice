# spring cloud 实践

![metadata管理页面](https://raw.githubusercontent.com/charlesvhe/spring-cloud-practice/master/metadata.html.png)

## 项目结构
### config 配置中心
端口：8888，方便起见直接读取配置文件，生产环境可以读取git。application-dev.properties为全局配置。先启动配置中心，所有服务的配置（包括注册中心的地址）均从配置中心读取。

### consumer 服务消费者
端口：18090，调用服务提供者。

### core 框架核心包
核心jar包，所有微服务均引用该包，使用AutoConfig实现免配置，模拟生产环境下spring-cloud的使用。

### eureka 注册中心
端口：8761，/metadata端点实现metadata信息配置。

### provider 服务提供者
端口：18090，服务提供者，无特殊逻辑。

### zuul 网关
端口：8080，演示解析token获得label并放入header往后传递

## Restful API 设计规范









