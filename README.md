# spring cloud 实践

## 项目结构
### config 配置中心
端口：8888，方便起见直接读取配置文件，生产环境可以读取git。application-dev.properties为全局配置。先启动配置中心，所有服务的配置（包括注册中心的地址）均从配置中心读取。

### eureka 注册中心
端口：8761，/metadata端点实现metadata信息配置。

### zuul 网关
端口：8080，演示解析token获得label并放入header往后传递

### core 框架核心包
核心jar包，所有微服务均引用该包，使用AutoConfig实现免配置，模拟生产环境下spring-cloud的使用。

### starter spring cloud以及core框架依赖简化starter

### provider 服务提供者
#### api 服务提供者SDK
强制服务消费方只能通过服务提供者提供的SDK包(api项目)进行调用，以便更加方便控制服务消费方的行为：  
提供可能的优化，SDK直接读取缓存（SDK只读缓存，写缓存放还是在微服务）。  
统计有哪些服务消费者，info端点显示包依赖，通过注册中心遍历所有服务。  
方便dubbo平滑迁移，接口加feign注解。

####service 服务提供者微服务
端口：18090，服务提供者，无特殊逻辑。

### consumer 服务消费者
端口：18090，调用服务提供者。

## Restful API 设计规范
/版本/访问控制/域对象  
/版本/访问控制/域对象/action/动作

### 版本
版本为微服务级别，也就是说不存在一个API是v3版，其他API还只是v1版的问题，要升级所有API版本一起升级，但是需要保证之前版本v1-v3还可以使用。  
原则上要兼容上一个版本 如果当前是 /v3 则 /v2 要求可以正常使用 /v1 不做要求  
如果无法兼容 需要通知所有服务消费者 并约定版本火车 一起上线时间  
#### 小技巧
下面swagger注解就可以实现上述要求，路径中的{version}没有限定，其实可以是任意内容，通过swagger文档来进行约定  

    // v1版api 即将废弃
    @ApiOperation("分页查询")
    @RequestMapping(value = "/v1/pb/product", method = RequestMethod.GET)
    @Deprecated
    List<Product> selectAll(@RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit);
     
    //ProviderApiAutoConfig.CURRENT_VERSION="v2" 用来替换上面的v1版本
    @ApiOperation("带过滤条件和排序的分页查询")
    @RequestMapping(value = "/{version}/pb/product", method = RequestMethod.GET)
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ProviderApiAutoConfig.CURRENT_VERSION, required = true)
    Response<PageData<Product, Product>> selectAllGet(Page page);
      
    // ProviderApiAutoConfig.COMPATIBLE_VERSION="v2,v1" swagger-ui上会显示一个version的下拉框, 默认v2
    @ApiOperation(value = "带过滤条件和排序的复杂分页查询")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ProviderApiAutoConfig.COMPATIBLE_VERSION, required = true)
    @RequestMapping(value = "/{version}/pb/product/action/search", method = RequestMethod.POST)
    Response<PageData<Product, Product>> selectAll(@RequestBody Page page);
     

### 访问控制
用于在网关统一进行访问控制, 访问控制分为  
pb - public 所有请求均可访问  
pt - protected 需要进行token认证通过后方可访问  
df - default 网关进行token认证 并且请求参数和返回结果进行加解密  
pv - private 无法通过网关访问 只能微服务内部调用  
其他 同pv

### action
无法用名词+请求方法表述的可以扩展为 /域对象/action/动词 必须为POST方法  
例如 POST /users/action/login


## 问题描述
主要由于使用了API(SDK)为了偷懒，以及Restful API路径中的版本带来的一系列问题。  

### spring MVC 不支持继承接口中方法参数上的注解（支持继承类、方法上的注解）
API中为了方便，使用feign替代RestTemplate手动调用。带来的问题：springMVC注解想偷懒，只在feign接口写一遍，然后实现类继承此接口即可。
例如：
feign接口定义如下  

    @FeignClient(ProviderApiAutoConfig.PLACE_HOLD_SERVICE_NAME)
    public interface ProductService {
        // 为了让spring mvc能够正确绑定变量
        public class Page extends PageRequest<Product> {
        }
        @RequestMapping(value = "/{version}/pt/product", method = RequestMethod.POST)
        Response<Product> insert(@RequestBody Product product);
    }

service实现类方法参数必须再写一次@RequestBody注解，方法上的@RequestMapping注解可以省略  

    @RestController
    public class ProductServiceImpl implements ProductService {
        @Override
        public Response<Product> insert(@RequestBody Product product) {
            product.setId(1L);
            return new Response(product);
        }
    }

解决办法，@Configuration配置类添加如下代码，扩展spring默认的ArgumentResolvers  

    public static MethodParameter interfaceMethodParameter(MethodParameter parameter, Class annotationType) {
        if (!parameter.hasParameterAnnotation(annotationType)) {
            for (Class<?> itf : parameter.getDeclaringClass().getInterfaces()) {
                try {
                    Method method = itf.getMethod(parameter.getMethod().getName(), parameter.getMethod().getParameterTypes());
                    MethodParameter itfParameter = new MethodParameter(method, parameter.getParameterIndex());
                    if (itfParameter.hasParameterAnnotation(annotationType)) {
                        return itfParameter;
                    }
                } catch (NoSuchMethodException e) {
                    continue;
                }
            }
        }
        return parameter;
    }
        
    @PostConstruct
    public void modifyArgumentResolvers() {
        List<HandlerMethodArgumentResolver> list = new ArrayList<>(adapter.getArgumentResolvers());

        list.add(0, new PathVariableMethodArgumentResolver() {  // PathVariable 支持接口注解
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return super.supportsParameter(interfaceMethodParameter(parameter, PathVariable.class));
            }

            @Override
            protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
                return super.createNamedValueInfo(interfaceMethodParameter(parameter, PathVariable.class));
            }
        });

        list.add(0, new RequestHeaderMethodArgumentResolver(beanFactory) {  // RequestHeader 支持接口注解
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return super.supportsParameter(interfaceMethodParameter(parameter, RequestHeader.class));
            }

            @Override
            protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
                return super.createNamedValueInfo(interfaceMethodParameter(parameter, RequestHeader.class));
            }
        });

        list.add(0, new ServletCookieValueMethodArgumentResolver(beanFactory) {  // CookieValue 支持接口注解
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return super.supportsParameter(interfaceMethodParameter(parameter, CookieValue.class));
            }

            @Override
            protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
                return super.createNamedValueInfo(interfaceMethodParameter(parameter, CookieValue.class));
            }
        });

        list.add(0, new RequestResponseBodyMethodProcessor(adapter.getMessageConverters()) {    // RequestBody 支持接口注解
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return super.supportsParameter(interfaceMethodParameter(parameter, RequestBody.class));
            }

            @Override
            protected void validateIfApplicable(WebDataBinder binder, MethodParameter methodParam) {    // 支持@Valid验证
                super.validateIfApplicable(binder, interfaceMethodParameter(methodParam, Valid.class));
            }
        });

        // 修改ArgumentResolvers, 支持接口注解
        adapter.setArgumentResolvers(list);
    }

### swagger不支持继承接口中方法参数上的注解（支持继承类、方法上的注解）
没有找到swagger自带扩展点能够优雅扩展的方法，只好修改源码了，下载springfox-spring-web 2.8.0 release源码包。
添加pom.xml  

    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-spring-web</artifactId>
        <version>2.8.0-charles</version>
        <packaging>jar</packaging>
    
        <properties>
            <java.version>1.8</java.version>
            <resource.delimiter>@</resource.delimiter>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
            <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
            <maven.compiler.source>${java.version}</maven.compiler.source>
            <maven.compiler.target>${java.version}</maven.compiler.target>
        </properties>
    
        <dependencyManagement>
            <dependencies>
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-dependencies</artifactId>
                    <!--<version>2.0.0.RELEASE</version>-->
                    <version>1.5.10.RELEASE</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
            </dependencies>
        </dependencyManagement>
    
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
            </dependency>
    
            <dependency>
                <groupId>org.reflections</groupId>
                <artifactId>reflections</artifactId>
                <version>0.9.11</version>
            </dependency>
    
            <!-- swagger -->
            <dependency>
                <groupId>io.springfox</groupId>
                <artifactId>springfox-swagger2</artifactId>
                <version>2.8.0</version>
                <exclusions>
                    <exclusion>
                        <groupId>io.springfox</groupId>
                        <artifactId>springfox-spring-web</artifactId>
                    </exclusion>
                </exclusions>
            </dependency>
        </dependencies>
    </project>

添加ResolvedMethodParameterInterface继承ResolvedMethodParameter  

    public class ResolvedMethodParameterInterface extends ResolvedMethodParameter {
        public ResolvedMethodParameterInterface(String paramName, MethodParameter methodParameter, ResolvedType parameterType) {
            this(methodParameter.getParameterIndex(),
                    paramName,
                    interfaceAnnotations(methodParameter),
                    parameterType);
        }
    
        public ResolvedMethodParameterInterface(int parameterIndex, String defaultName, List<Annotation> annotations, ResolvedType parameterType) {
            super(parameterIndex, defaultName, annotations, parameterType);
        }
    
        public static List<Annotation> interfaceAnnotations(MethodParameter methodParameter) {
            List<Annotation> annotationList = new ArrayList<>();
            annotationList.addAll(Arrays.asList(methodParameter.getParameterAnnotations()));
    
            if (CollectionUtils.isEmpty(annotationList)) {
                for (Class<?> itf : methodParameter.getDeclaringClass().getInterfaces()) {
                    try {
                        Method method = itf.getMethod(methodParameter.getMethod().getName(), methodParameter.getMethod().getParameterTypes());
                        MethodParameter itfParameter = new MethodParameter(method, methodParameter.getParameterIndex());
                        annotationList.addAll(Arrays.asList(itfParameter.getParameterAnnotations()));
                    } catch (NoSuchMethodException e) {
                        continue;
                    }
                }
            }
    
            return annotationList;
        }
    }
    
修改HandlerMethodResolver类line 181，将ResolvedMethodParameter替换为ResolvedMethodParameterInterface，重新打包deploy，并在swagger相关依赖中强制指定修改后的版本。

    <!-- swagger -->
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger2</artifactId>
    </dependency>
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger-ui</artifactId>
    </dependency>
    <!--扩展swagger支持从接口获得方法参数注解-->
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-spring-web</artifactId>
        <version>2.8.0-charles</version>
    </dependency>

这样就能够顺利生产swagger文档啦。

### feign不支持GET方法传递POJO
由于springMVC是支持GET方法直接绑定POJO的，只是feign实现并未覆盖所有springMVC特效，网上的很多变通方法都不是很好，要么是吧POJO拆散成一个一个单独的属性放在方法参数里，要么是把方法参数变成Map，要么就是要违反Restful规范，GET传递@RequestBody：  
https://www.jianshu.com/p/7ce46c0ebe9d  
https://github.com/spring-cloud/spring-cloud-netflix/issues/1253  
解决办法，使用feign拦截器：

    public class CharlesRequestInterceptor implements RequestInterceptor {
        @Autowired
        private ObjectMapper objectMapper;
    
        @Override
        public void apply(RequestTemplate template) {
            // feign 不支持 GET 方法传 POJO, json body转query
            if (template.method().equals("GET") && template.body() != null) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(template.body());
                    template.body(null);
    
                    Map<String, Collection<String>> queries = new HashMap<>();
                    buildQuery(jsonNode, "", queries);
                    template.queries(queries);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    
        private void buildQuery(JsonNode jsonNode, String path, Map<String, Collection<String>> queries) {
            if (!jsonNode.isContainerNode()) {   // 叶子节点
                if (jsonNode.isNull()) {
                    return;
                }
                Collection<String> values = queries.get(path);
                if (null == values) {
                    values = new ArrayList<>();
                    queries.put(path, values);
                }
                values.add(jsonNode.asText());
                return;
            }
            if (jsonNode.isArray()) {   // 数组节点
                Iterator<JsonNode> it = jsonNode.elements();
                while (it.hasNext()) {
                    buildQuery(it.next(), path, queries);
                }
            } else {
                Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    if (StringUtils.hasText(path)) {
                        buildQuery(entry.getValue(), path + "." + entry.getKey(), queries);
                    } else {  // 根节点
                        buildQuery(entry.getValue(), entry.getKey(), queries);
                    }
                }
            }
        }
    }
    
### feign不支持路径中的{version}
对于一个典型的Restful API定义如下：

    @ApiOperation("带过滤条件和排序的分页查询")
    @RequestMapping(value = "/{version}/pb/product", method = RequestMethod.GET)
    // 当前版本新开发api 随微服务整体升级 pt=protected 受保护的网关token验证合法可调用
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ProviderApiAutoConfig.CURRENT_VERSION, required = true)
    Response<PageData<Product, Product>> selectAllGet(Page page);
    
我们并不关心路径中的{version}，因此方法参数中也没有@PathVariable("version")，这个时候feign就傻了，不知道路径中的{version}应该被替换成什么值。
解决办法 使用自己的Contract替换SpringMvcContract，先将SpringMvcContract代码复制过来，修改其中processAnnotationOnMethod方法的代码，从swagger注解中获得{version}的值：

    public class CharlesSpringMvcContract extends Contract.BaseContract
            implements ResourceLoaderAware {
        @Override
        protected void processAnnotationOnMethod(MethodMetadata data,
                                                 Annotation methodAnnotation, Method method) {
            if (!RequestMapping.class.isInstance(methodAnnotation) && !methodAnnotation
                    .annotationType().isAnnotationPresent(RequestMapping.class)) {
                return;
            }
    
            RequestMapping methodMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            // HTTP Method
            RequestMethod[] methods = methodMapping.method();
            if (methods.length == 0) {
                methods = new RequestMethod[]{RequestMethod.GET};
            }
            checkOne(method, methods, "method");
            data.template().method(methods[0].name());
    
            // path
            checkAtMostOne(method, methodMapping.value(), "value");
            if (methodMapping.value().length > 0) {
                String pathValue = Util.emptyToNull(methodMapping.value()[0]);
                if (pathValue != null) {
                    pathValue = resolve(pathValue);
                    // Append path from @RequestMapping if value is present on method
                    if (!pathValue.startsWith("/")
                            && !data.template().toString().endsWith("/")) {
                        pathValue = "/" + pathValue;
                    }
                    // 处理version
                    if (pathValue.contains("/{version}/")) {
                        Set<ApiImplicitParam> apiImplicitParams = AnnotatedElementUtils.findAllMergedAnnotations(method, ApiImplicitParam.class);
                        for (ApiImplicitParam apiImplicitParam : apiImplicitParams) {
                            if ("version".equals(apiImplicitParam.name())) {
                                String version = apiImplicitParam.allowableValues().split(",")[0].trim();
                                pathValue = pathValue.replaceFirst("\\{version\\}", version);
                            }
                        }
                    }
                    data.template().append(pathValue);
                }
            }
    
            // produces
            parseProduces(data, method, methodMapping);
    
            // consumes
            parseConsumes(data, method, methodMapping);
    
            // headers
            parseHeaders(data, method, methodMapping);
    
            data.indexToExpander(new LinkedHashMap<Integer, Param.Expander>());
        }
    }

然后在自己的AutoConfig中声明成spring的bean  

    @Configuration
    @ConditionalOnClass(Feign.class)
    public class FeignAutoConfig {
        @Bean
        public Contract charlesSpringMvcContract(ConversionService conversionService) {
            return new CharlesSpringMvcContract(Collections.emptyList(), conversionService);
        }
    
        @Bean
        public CharlesRequestInterceptor charlesRequestInterceptor(){
            return new CharlesRequestInterceptor();
        }
    }

## spring cloud以及feign小技巧
要求源码对dev环境负责（application.properties里的配置均为dev环境地址）  
配置中心dev环境全局配置添加：

    # dev环境允许本地配置override配置中心配置
    spring.cloud.config.overrideNone=true
    
在开发过程中，dev环境会部署一套微服务到服务器上，本机开发时为了不影响dev服务器，spring.application.name添加自己姓名为前缀，注册到dev注册中心也不影响dev服务器，配置中心指定名字，不再随spring.application.name变化。
启动参数添加：
    
    --spring.application.name=charles-framework-provider
    
bootstrap.properties配置模板
    
    # 配置中心预留
    spring.cloud.config.uri=https://config-mo.xxxx.com/config
    # config.name 可以不指定 默认为 spring.application.name
    # 这里指定名字是为了兼容本机注册到dev环境时通过修改spring.application.name=charles-framework-provider来区分微服务以免造成干扰
    spring.cloud.config.name=framework-provider
    spring.cloud.config.profile=${ENV:dev}
    # 连不上配置中心不启动
    spring.cloud.config.fail-fast=true
    
这样本机开发可以调用服务器上的微服务，但是服务器上的微服务不会调用本机（服务名修改了）。  
那么如果两名研发需要互相调用联调该如何处理呢？  
@FeignClient注解不要写死服务名，使用place holder，类似如下代码：

    @FeignClient(ProviderApiAutoConfig.PLACE_HOLD_SERVICE_NAME)
    public interface ProductService {
    }

ProviderApiAutoConfig的代码如下：

    @Configuration
    // 自动配置feign扫描包 使用方零配置 微服务本身不加载自己的API中的feign
    @ConditionalOnExpression("#{!environment['spring.application.name'].endsWith('" + ProviderApiAutoConfig.SERVICE_NAME + "')}")
    @EnableFeignClients(basePackages = "com.github.charlesvhe.springcloud.practice.provider")
    public class ProviderApiAutoConfig {
        public static final String SERVICE_NAME = "provider";
        // FeignClient 用placeholder可以方便的进行内部调用 配置key为charles.service.服务名
        // 配置charles.service.framework-provider=charles-framework-provider来调用charles-framework-provider服务
        public static final String PLACE_HOLD_SERVICE_NAME = "${charles.service." + SERVICE_NAME + ":" + SERVICE_NAME + "}";
    
        public static final String CURRENT_VERSION = "v2";
        public static final String COMPATIBLE_VERSION = "v2,v1";
    
    }

这样在启动参数中指定哪个服务想调哪个人的实例都可以。同时处理了service依赖api时，不会注入feign的api实例。
