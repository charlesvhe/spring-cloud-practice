# spring cloud 实践

![metadata管理页面](https://raw.githubusercontent.com/charlesvhe/spring-cloud-practice/master/metadata.html.png)

## 项目结构
### config 配置中心
端口：8888，方便起见直接读取配置文件，生产环境可以读取git。application-dev.properties为全局配置。先启动配置中心，所有服务的配置（包括注册中心的地址）均从配置中心读取。

### consumer 服务消费者
端口：18090，调用服务提供者，为了演示header传递。

### core 框架核心包
核心jar包，所有微服务均引用该包，使用AutoConfig实现免配置，模拟生产环境下spring-cloud的使用。

### eureka 注册中心
端口：8761，/metadata端点实现metadata信息配置。

### provider 服务提供者
端口：18090，服务提供者，无特殊逻辑。

### zuul 网关
端口：8080，演示解析token获得label并放入header往后传递

## 实践：降级、限流、滚动、灰度、AB、金丝雀等等等等
我本人是从dubbo转过来的，经常看到社区里面拿dubbo和spring-cloud做对比，一对比就提到dubbo所谓的降级、限流功能。spring-cloud默认没有这个能力，让我们来扩展spring-cloud，使她具备比dubbo更牛逼的各种能力。

所谓的降级、限流、滚动、灰度、AB、金丝雀等等等等，在我看来无非就是扩展了服务路由能力而已。这里说的服务降级，说的是服务A部署多个实例，实例级别的降级限流。如果要做整个服务A的降级，直接采用docker自动扩容缩容即可。

我们先来看应用场景：

服务A 发布了1.0版，部署了3个实例A1、A2、A3，现在要对服务A进行升级，由1.0升级到2.0。先将A1服务流量关闭，使A2、A3负担；升级A1代码版本到2.0；将A1流量调整为1%，观察新版本运行情况，如果运行稳定，则逐步提升流量5%、10%直到完全放开流量控制。A2、A3重复上述步骤。

在上述步骤中，我们想让特别的人使用2.0，其他人还是使用1.0版，稳定后再全员开放。

我们想不依赖sleuth做链路跟踪，想自己实现一套基于ELK的链路跟踪。

我们还有各种千奇百怪的想法。。。

## 实现思路
要实现这些想法，我们需要对spring-cloud的各个组件、数据流非常熟悉，这样才能知道该在哪里做扩展。一个典型的调用：外网-》Zuul网关-》服务A-》服务B。。。

spring-cloud跟dubbo一样都是客户端负载均衡，所有调用均由Ribbon来做负载均衡选择服务器，所有调用前后会套一层hystrix做隔离、熔断。服务间调用均用带LoadBalanced注解的RestTemplate发出。RestTemplate-》Ribbon-》hystrix

通过上述分析我们可以看到，我们的扩展点就在Ribbon，Ribbon根据我们的规则，选择正确的服务器即可。

我们先来一个dubbo自带的功能：基于权重的流量控制。dubbo自带的控制台可以设置服务实例粒度的半权，倍权。其实就是在客户端负载均衡时，选择服务器带上权重即可，spring-cloud默认是ZoneAvoidanceRule，优先选择相同Zone下的实例，实例间采用轮询方式做负载均衡。我们的想把基于轮询改为基于权重即可。接下来的问题是，每个实例的权重信息保存在哪里？从哪里取？dubbo放在zookeeper中，spring-cloud放在eureka中。我们只需从eureka拿每个实例的权重信息，然后根据权重来选择服务器即可。具体代码LabelAndWeightMetadataRule（先忽略里面的优先匹配label相关代码）。

## 放入核心框架
LabelAndWeightMetadataRule写好了，那么我们如何使用它，使之生效呢？有3种方式。

1）写个AutoConfig将LabelAndWeightMetadataRule声明成@Bean，用来替换默认的ZoneAvoidanceRule。这种方式在技术验证、开发测试阶段使用短平快。但是这种方式是强制全局设置，无法个性化。

2）由于spring-cloud的Ribbon并没有实现netflix Ribbon的所有配置项。netflix配置全局rule方式为：ribbon.NFLoadBalancerRuleClassName=package.YourRule，spring-cloud并不支持，spring-cloud直接到服务粒度，只支持SERVICE_ID.ribbon.NFLoadBalancerRuleClassName=package.YourRule。我们可以扩展org.springframework.cloud.netflix.ribbon.PropertiesFactory修正spring cloud ribbon未能完全支持netflix ribbon配置的问题。这样我们可以将全局配置写到配置中心的application-dev.properties全局配置中，然后各个微服务还可以根据自身情况做个性化定制。但是PropertiesFactory属性均为私有，应该是spring cloud不建议在此扩展。参见https://github.com/spring-cloud/spring-cloud-netflix/issues/1741。

3）使用spring cloud官方建议的@RibbonClient方式。该方式仅存在于spring-cloud单元测试中（在我提问后，现在还存在于spring-cloud issue list）。具体代码参见DefaultRibbonConfiguration、CoreAutoConfiguration。

## 实际测试
依次开启 config eureka provide（开两个实例，通过启动参数server.port指定不同端口区分） consumer zuul

访问 http://localhost:8761/metadata.html 这是我手写的一个简单的metadata管理界面，分别设置两个provider实例的weight值（设置完需要一段2分钟才能生效），然后访问 http://localhost:8080/provider/user 多刷几次来测试zuul是否按权重发送请求，也可以访问 http://localhost:8080/consumer/test 多刷几次来测试consumer是否按权重来调用provide服务。

## 进阶，基于标签
基于权重的搞定之后，接下来才是重头戏：基于标签的路由。入口请求含有各种标签，然后我们可以根据标签幻化出各种各样的路由规则。例如只有标注为粉丝的用户才使用新版本（灰度、AB、金丝雀），例如标注为中国的用户请求必须发送到中国的服务器（全球部署），例如标注为写的请求必须发送到专门的写服务实例（读写分离），等等等等，唯一限制你的就是你的想象力。

## 实现思路
根据标签的控制，我们当然放到之前写的Ribbon的rule中，每个实例配置的不同规则也是跟之前一样放到注册中心的metadata中，关键是标签数据如何传过来。权重随机的实现思路里面有答案，请求都通过zuul进来，因此我们可以在zuul里面给请求打标签，基于用户，IP或其他看你的需求，然后将标签信息放入ThreadLocal中，然后在Ribbon Rule中从ThreadLocal拿出来使用就可以了。然而，按照这个方式去实验时，发现有问题，拿不到ThreadLocal。原因是有hystrix这个东西，回忆下hystrix的原理，为了做到故障隔离，hystrix启用了自己的线程，不在同一个线程ThreadLocal失效。那么还有什么办法能够将标签信息一传到底呢，想想之前有没有人实现过类似的东西，没错sleuth，他的链路跟踪就能够将spam传递下去，翻翻sleuth源码，找找其他资料，发现可以使用HystrixRequestVariableDefault，这里不建议直接使用HystrixConcurrencyStrategy，会和sleuth的strategy冲突。代码参见CoreHeaderInterceptor。现在可以测试zuul里面的rule，看能否拿到标签内容了。

这里还不是终点，解决了zuul的路由，服务A调服务B这里的路由怎么处理呢？zuul算出来的标签如何往后面依次传递下去呢，我们还是抄sleuth：把标签放入header，服务A调服务B时，将服务A header里面的标签放到服务B的header里，依次传递下去。这里的关键点就是：内部的微服务在接收到发来的请求时（zuul-》A，A-》B都是这种情况）我们将请求放入ThreadLocal，哦，不对，是HystrixRequestVariableDefault，还记得上面说的原因么：）。这个容易处理，写一个spring mvc拦截器即可，代码参见CoreHeaderInterceptor。然后发送请求时自动带上这个里面保存的标签信息，参见RestTemplate的拦截器CoreHttpRequestInterceptor。到此为止，技术上全部走通实现。

总结一下：zuul依据用户或IP等计算标签，并将标签放入header里向后传递，后续的微服务通过拦截器，将header里的标签放入RestTemplate请求的header里继续向后接力传递。标签的内容通过放入类似于ThreadLocal的全局变量（HystrixRequestVariableDefault），使Ribbon Rule可以使用。

## 测试
参见PreFilter源码，模拟了几个用户的标签，参见LabelAndWeightMetadataRule源码，模拟了OR AND两种标签处理策略。依次开启 config eureka provide（开两个实例，通过启动参数server.port指定不同端口区分） consumer zuul

访问 http://localhost:8761/metadata.html 设置第一个provide 实例 orLabel为 CN,Test 发送请求头带入Authorization: emt 访问http://localhost:8080/provider/user 多刷几次，可以看到zuul所有请求均路由给了第一个实例。访问http://localhost:8080/consumer/test 多刷几次，可以看到，consumer调用均路由给了第一个实例。

设置第二个provide 实例 andLabel为 EN,Male 发送请求头带入Authorization: em 访问http://localhost:8080/provider/user 多刷几次，可以看到zuul所有请求均路由给了第二个实例。访问http://localhost:8080/consumer/test 多刷几次，可以看到，consumer调用均路由给了第二个实例。

Authorization头还可以设置为PreFilter里面的模拟token来做测试，至此所有内容讲解完毕，技术路线拉通，剩下的就是根据需求来完善你自己的路由策略啦。








