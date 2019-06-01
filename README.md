# 乐优商城

服务框架为SpringBoot分布式微服务  
===================================

ly-gateway网关，拦截、鉴权等。    
ly-registry注册中心  
ly-common复用工具类组件  
ly-item有interface和service，一个用来对外告诉自身的pojo构造函数，一个用来实现（不对外）。  
ly-upload用来上传图片等东西的微服务，通用上传  
ly-search搜索微服务，elasticsearch搜索等大数据搜索用的  
ly-page放置静态页面Thymeleaf的(因只做后台，所以该微服务未做详细的item.html)  
ly-sms发送短信的微服务，里面有发送短信、rabbitMQ、redis例子。  
ly-user用户中心，包括用户注册、修改用户信息等。  
ly-auth授权中心，用户登陆。用户用来鉴权和授权的。有rsa加密和jwt协议。  
ly-cart购物车微服务。之所以购物车数据存在redis，是因为读写频率快。  
ly-order订单微服务          

## -----------------接口-----------------
关于接口返回，一定要是用rest风格返回，即：不能出现动词，修改：post，删除：delete等。返回状态码也要遵循rest风格如404、500等。  
写接口时，一定要按照接口文档来写，一般都是有框架人员来写。  
获取接口请求参数时：@CookieValue("LY_TOKEN") String token获取cookie。@RequestParam("username") String username获取请求参数username。@PathVariable("data") String data获取请求参数@GetMapping("/check/{data}/{type}")的data值。  

## -----------------Swagger-UI-----------------
没有API文档工具之前，大家都是手写API文档的，在什么地方书写的都有，而且API文档没有统一规范和格式，每个公司都不一样。这无疑给开发带来了灾难。  
OpenAPI规范（OpenAPI Specification 简称OAS）是Linux基金会的一个项目，试图通过定义一种用来描述API格式或API定义的语言，来规范RESTful服务开发过程。  
OpenAPI是一个编写API文档的规范，然而如果手动去编写OpenAPI规范的文档，是非常麻烦的。而Swagger就是一个实现了OpenAPI规范的工具集。            
官网：https://swagger.io/  

```
<dependency>    
     <groupId>io.springfox</groupId>
     <artifactId>springfox-swagger2</artifactId>
     <version>2.8.0</version>
 </dependency>
 <dependency>
     <groupId>io.springfox</groupId>
     <artifactId>springfox-swagger-ui</artifactId>
     <version>2.8.0</version>
 </dependency>
 ```
```
//配置
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .host("http://order.leyou.com")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.leyou.order.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("乐优商城订单系统")
                .description("乐优商城订单系统接口文档")
                .version("1.0")
                .build();
    }
}
``` 
在controller的每个handler上添加接口说明注解  
```
/**
 @Api：修饰整个类，描述Controller的作用
 @ApiOperation：描述一个类的一个方法，或者说一个接口
 @ApiParam：单个参数描述
 @ApiModel：用对象来接收参数
 @ApiProperty：用对象接收参数时，描述对象的一个字段
 @ApiResponse：HTTP响应其中1个描述
 @ApiResponses：HTTP响应整体描述
 @ApiIgnore：使用该注解忽略这个API
 @ApiError ：发生错误返回的信息
 @ApiImplicitParam：一个请求参数
 @ApiImplicitParams：多个请求参数
 */
```
启动服务去访问http://localhost:8089/swagger-ui.html即可查看接口文档。  

  
## -----------------插件----------------- 
通用mapper和分页助手也很好用，只要是单个数据库查询的，都可以使用通用mapper    
mapper接口继承的类有很多，现在举个例子：IdListMapper（根据批量id查询、删除），Mapper（通用mapper，包含各种普通新增改查）,InsertListMapper(批量新增)。
请注意，这些继承的，有些包是不同的，所以功能不同。就比如InsertListMapper，有两个包，一个只可以识别id进行批量新增，一个可以不是id批量新增 

lombok很好用  
@Getter//为非final字段添加  
@NoArgsConstructor //自动生成无参数构造函数。  
@AllArgsConstructor //自动生成全参数构造函数。 

## -----------------注意-----------------  
记住扫描controller包的时候，spring启动函数放到包外，不然扫不到。  

自定义属性时，先在application.yaml设置属性格式，如：ly: sms: test1:123。然后创建一个类如TestProperties，@ConfigurationProperties(prefix = "ly.sms")//获取自定义属性，@Data。记得，这个类的属性字段必须和自定义名字相同，private String test1    
获取时@Component，@EnableConfigurationProperties(TestProperties.class)，然后注入这个类TestProperties，就可以获取这个类的自定义属性名了。  
或者：@Value("${ly.jwt.cookieName}")  
private String cookieName;  也可以获取自定义属性。      
   

## -----------------文件上传-----------------   
①、所有请求经过网关时，springMVC会先预处理，并缓存，对普通请求没什么影响，但对上传文件会造成网络负担，在高并发时，有可能造成网络阻塞。所以在请求路径前加一个/zuul，这个默认值是可以更改的
这个还是经过网关，但不会进行缓存，这样就不会有压力了。此时我们需要使用Ngin进行地址重写，使用rewrite重写，这样服务器就不用改代码了。  
具体在nginx的config下的sercer字段下面添加：location /api/upload{ rewrite "^/(.*)$" /zuul/$1; }    
②、采用分布式文件储存中小型用FastDFS，大型用HDFS（会切割文件，这样读取和上传速度会很快）。需要安装，因为需要使用虚拟机，和Nginx一样，暂未实现。  
③、使用FastDFS可以用java的别人写的api，测试例子已经有了。  
④、前端添加图片或者头像那些，其实是点击图片，会上传，此时返回一个图片url，然后在界面显示图片的。因为修改名字和头像是不同步的。     

## -----------------跨域问题----------------- 
跨域：一个域名访问另一个域名，出了ajax之外，任何请求都可以跨域。  
ajax不允许发送跨域请求。解决跨域问题：  
jsonp：利用script标签，很久以前的解决方案，但只能发起get请求。  
nginx：虽然可以解决，但配置太繁琐，维护起来很麻烦。  
CORS：安全可靠，支持各种请求方式，但会产生而外的请求。（浏览器和服务器需要同时支持）  

CORS原理，分为简单请求和特殊请求。  
简单请求：两个条件，请求方式是HEAD、GET、POST。   
信息头不超过以下几个，Accept、Accept-Language、Content-Language、Last-Event-ID、Content-Type只能三个值之一：application/x-www-form-urlencoded、multipart/form-data、text/plain  
其底层依然是ajax，凡是不符合上诉条件的，就为特殊请求。  
当浏览器发现是简单请求时，会添加origin字段，后面跟着跨域网址，后台需要识别，若是对的话，返回Access-Control-Allow-Origin和Access-Control-Allow-Credentials，即可跨域访问  
特殊请求需要先预检请求，也就是发两次请求，返回有一大堆东西、   

## -----------------Elasticsearch-----------------  
官网：https://www.elastic.co/cn/  
需要下载，配合kibana和ik分词器使用更佳      
了解solr，毕竟也是搜索，但配置麻烦，而且有延时，技多不压身  
当数据达到PB级别时，数据库全文查找将会有延时，而elasticsearch近实时搜索，并且遵循rest原则、而且分布式，无需人工搭建集群   
其底层需要依赖java，基于Lucene实现，所以把这个配置java虚拟机内存调小点，不然会很卡。配置jvm.options设置内存，设置elasticsearch.yml设置名字、集群、地址、端口等，他默认端口是9200，http端口。还会启动一个9300端口，这个是tcp端口，集群通信用的。  
因为这个没有界面，需要安装插件，不过一般安装Kibana，其底层依赖node.js，也是在那个官网下载，他这也有个kibana.yml文件，用以修改连接elasticsearch地址的。其默认端口为5601。  
另外还需要安装ik分词器，属于elasticsearch插件，用以中文分词  
ik分词器"analyzer": "ik_smart"用以人性化分类，而ik_max_word则细分。elasticsearch并且支持http的rest风格访问。   

elasticsearch需要创建索引，其实类似mysql   
  
浏览器访问Kibana：http://localhost:5601/  
  
## -----------------Thymeleaf-----------------
静态页面，因只涉及后台，这里暂不多说明，可以自行查看ly-page源码    

##  -----------------RabbitMQ-----------------  
官网：https://www.rabbitmq.com/  
需要下载，并且需要配置他的虚拟机，和java类似，官网地址http://www.erlang.org/downloads。消息队列，和KafKa一样，不过RabbitMQ高稳定性，kafka高吞吐量  
RabbitMQ默认端口15672，通信端口为5672，集群端口25672      
浏览器输入：http://localhost:15672。  
用户名：guest， 密码：guest  

 
### 面试经常问：    
如何避免消息丢失Simple：    
消费者的消息确认机制。消费者获取消息后，会想RabbitMQ发送回执ACK，告知接收，此时就会确认，避免小时丢失。  
ACK分为两种：自动ACK，意思当接收到消息后，自动确认并发送回执，不管后面自己写的代码有没报错有没处理完成业务，都会发送确认。  
手动ACK:意思是需要代码控制，把自动ack设置为false即可。虽然信息，但你未消费，所以未确认，消息未丢失。  
// 手动进行ACK   
channel.basicAck(envelope.getDeliveryTag(), false);  
// 监听队列，第二个参数false，手动进行ACK     
channel.basicConsume(QUEUE_NAME, false, consumer);  

面试经常问持久化：  
就是当RabbitMQ挂了咋办，挂了消息和队列都没有了，集群可以解决，但回答持久化更好。  
声明交换机或队列的时候，设置durable为true即可（channel.queueDeclare第二个参数），这是消费者和生产者持久化。消息持久化需要设置发送信息，第三个参数设置(chanel.basicPublish设置为MessageProperties.其他)。  
   

消息堆积Work：  
多弄几个消费者，就启动多个微服务就行了。谁先领完任务谁先完成就算结束，和负载均衡差不多，默认是平均分，可以设置。  
// 设置每个消费者同时只能处理一条消息，就是谁先完成，继续去领任务，而不是平均分，做完就闲着。    
channel.basicQos(1);  

广播Fanout：  
中间有一个交换机，生产者发送给交换机，交换机再发送给消费者，如果有多个消费者，则一起接收。注意：若交换机不在，则消息丢失，因为消息只能存在队列，而交换机在队列前面，先经过交换机再分发给消费者队列。  

订阅模型Direct：  
中间有交换机，但消费者会绑定路由key，这样就不是任意绑定了，也不是同时一起收了，而是发送方发送的时候，选择RoutingKey，发送给匹配的人。  

订阅模式Topic：  
和direct差不多，只不多RoutingKey可以使用通配符，如：usa.#匹配usa开头的RoutingKey，而不是direct的insert、delete、update。    

SpringAMQP已经整合了RabbitMQ，发消息直接一个amqpTemplate.convertAndSend即可。  
接收消息在component上的方法加上@RabbitListener注解即可，注解属性多一点，参考源码Demo。  

这个类似Android的eventBus            
             
  
## -----------------Redis----------------- 
需要安装（Windows好像不维护了，Windows最新版是3.2，而linux版本都5.0了）   
缓存，端口默认6379    
NoSql 不仅仅是数据库  
面试常问：缓存击穿、缓存雪崩，热点key失效。需自行百度  
keys命令是查询所有key，由于redis是单线程，切记不要在生产环境下使用该命令，不然查询几亿条数据卡死就完蛋了。最好禁用。  
del key 删除一个key   
exists key 判断是否有这个key   
redis默认有16个库，select 3 选择第三个库，一般集群是禁用select，只用一个库，可以修改。  
expire key seconds，redis默认key无限时间，这个expire后面跟着key再跟着秒，设置多少秒就失效。   
上面是通用指令，下面是特殊指令，通用如del是删除del所属的key，而特殊如hdel只是删除哈希key下的value下的key对应的value值。  
获取string的命令：set key，get key。  还有批量设置：mset k1 v1 k2 v2。mget k1 v1 k2 v2  
hset 和hget 是用来存储哈希和取出哈希的。如 hset user:123 name "rouse" 这里存了user:123的key，其value是map<name,"rouse">。取就hget user:123 name。  

不用redisTemplate，而是使用StringRedisTemplate，因为他已经默认string，并且序列化字节的时候也默认了，StringRedisTemplate的opsForHash/List/Set/Value(字符串) 对应redis的数据结构                

## -----------------Hibernate Validator-----------------  
数据校验。在接收的bean使用注解@Valid，用来校验Bean的合法性等，然后在实体类写上相应的注解即可。具体可查看user-service的pojo  
这里要注意，因为错误返回是spring自己写的，所以我们需要在接收的bean后面加上BindingResult result，然后if (result.hasFieldErrors()判断即可返回我们自己的或者throw new RuntimeException(result.getFieldErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.joining("|")));  
不过一般这里并不需要去判断和返回信息，直接无视掉就好了，因为这些信息是前端做的，我们不要显示就好了。  
可查看UserController。  

# -----------------登陆-----------------
有状态登录:登陆的时候把session存在tomcat中。这样就不能做集群了，因为每个tomcat都不一样。并且存的session太多会造成服务器压力。  
无状态登录:服务端不存信息，让客户端携带。这样可以搭建集群了，并且并发也高了。  
无登录流程：先登陆验证，认证通过将用户信息加密形成token返回给客户端，以后每次请求，客户端都携带token，服务器对token解密，判断是否有效。  
采用JWT+RSA非对称加密对token加密，防止伪造。jwt官网（https://jwt.io/）,jwt只是规范。  
jwt包括三部分，header、payload、signature。  
步骤：用户登陆，认证、通过后根据secret生成token，返回token给用户，用户每次请求携带token，服务器截图jwt签名，有效后从payload取出用户信息，处理请求、返回结果。  

由于登陆的时候，是把token写到cookie里面，但cookie不允许跨域。  
解决方法：①、有Nginx的时候：  
这时跨域访问的时候是不会传回token的，所以需要配置Nginx，返回路径填写原来的地址$host。  
②无论有没Nginx，都需要设置网关，因为zuul把host拦截了。    
zuul添加配置add-host-header: true。以及过滤敏感头忽略。sensitive-headers: #放行所有敏感头 （感觉不安全，还不如返回的时候不要写cookie，直接返回token就好。）  
  
登陆可优化的点：①权限需要引用。②在AuthFilter需要判断权限。③授权中心还要做服务鉴权（如果别人知道你其他微服务地址跨过了网关怎么办）。    

③是个面试点：万一别人知道你微服务地址绕过网关怎么办：这时，已经不是进行用户鉴权了，而是服务间进行鉴权。数据库两个表：服务列表以及服务对服务关系表。
每次服务调用其他服务时，先去授权中心获取权限，如search服务调用item商品服务，先把服务id和密码给授权中心，授权中心查询数据库看看是否通过，通过的话就通过jwt发放给search，每次search访问item就带着token。其实就是把微服务当成用户了。  
这里的难点就是，服务启动时，就要把信息注册给授权中心，并且服务间的调用是使用feign，这时需要使用feign的拦截器先拦截判断是否有token。   

cookie面试点：    
cookie被禁用怎么办：一、提示用户打开cookie。二、把token放到web存储中（localStorage、SessionStorage），每次请求都需要手动携带token，写入头中。    
cookie被盗用怎么办：可以在cookie加入身份识别，如网卡、mac等加入payload。不担心cookie被篡改，因为有token。但如果是网络环境如被黑了，那没办法了，但可以预防：网络访问可以https，有效避免被盗用。  

# -----------------传递对象-----------------
在同一个tomcat中，单个用户访问时，所有的request都是，可以request.setAttribute("user", user);传递user，但spring不推荐这么做，可以使用private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();  
因为不但request是共享的，线程thread也是共享的，而ThreadLocal存储的是map，key是线程，value是对象。直接tl.set(user);，key不用写，因为他会自己取当前线程。               
      
# -----------------后记----------------- 
多活用StringUtils.isNotBlank(key)和CollectionUtils.isEmpty(list)，一个是lang3的，一个是springframework的   
StringUtils.join拼接字符串 ,Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())加入List,  

JDK1.8流的用法,比for好用:   
//我们把stock变成一个map，其key是:sku的id，值是库存值  
Map<Long, Integer> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));  
skuList.forEach(s ->s.setStock(stockMap.get(s.getId())));  

List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());后面这个toList可以改需要的类型，如toSet  
spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());可以放入方法searchService::buildGoods，返回值为Goods        

注释的TODO很好用，不但可以高亮注释代码，还会在idea上提示你需要todo的事情，将来你忘了要做什么的时候，直接点开左下角的todo就可以看到你标记过的东西  

@Controller//如果是RestController是把返回结果按照json处理，这是普通的controller      

  //synchronized锁，只能允许一个线程通过。也就同一个时间只有一个人访问这个方法。  

打包过程：打开右边的Maven Projects =》Lifecycle =》install 即可打包成jar。然后上传到服务器或Nginx，使用命令java -jar ***.jar即可，不用tomcat，已经内置了tomcat。    


CTRL+ALT+B查看接口实现。CTRL+SHIFT+U转大写字母。  
     
SpringCloud  
==================================  
按照module、微服务形式进行学习cloud，从SpringCloudDemo学习。  

要在父项目新增module，若想不麻烦，还需配置maven父目录。  
使用springboot架构，具体学习可以看前面项目  
因为使用module，所以可以使用idea右边maven projects 点一下刷新，右下角会弹出一个框，点击show run dashboard。（或者直接点下面的Run Dashboard）

# -----------------项目介绍-----------------  
核心是高并发   
 
## -----------------高可用eureka，即使挂掉一台还有其他注册中心-----------------     
要想在idea测试多个注册中心（eurekaServer）需要复制一份启动项（就是启动按钮左边的配置），然后随便命名为第二个便可。  
原理是，修改端口10086，连接地址改为http://127.0.0.1:10087/eureka，先启动第一个，然后再修改端口10087，连接地址为10086。利用先后顺序启动可以启动两个不同端口。  
（另一方法可以在启动配置时，（该例子是user-service）在VM option设置-Dserver.port=8082参数，覆盖原来8081端口。）  
注意，因为是多台，每个服务需要填多一个地址，例如：defaultZone: http://127.0.0.1:10086/eureka,http://127.0.0.1:10087/eureka（一台就不用使用符号,）  
两个注册中心就填要连接的地址（1个地址），因为互相连接，三个注册中心要填（2个其他注册中心地址），同理。  
总结：也就是consumer连接10086和10087，user-service也是连接10086和10087，注册中心10086和10087相互连接。当注册中心其中一台挂掉的时候，consumer和user-service还是能正常连接！（默认每30秒扫描一次）

## -----------------负载均衡Ribbon-----------------  
顾名思义，就是从多个服务中找一个合适的进行连接，分担压力。  
在http请求那里添加注解@LoadBalanced（RestTemplate 默认httpUrlConnection连接）  
然后String url = "http://user-service/user/" + id;  
String user = restTemplate.getForObject(url, String.class);  
在http连接那里请求地址为eureka服务地址即可（user-service）负载均衡默认采用轮询服务。

## -----------------服务保护Hystrix熔断器-----------------
当多个客户端访问时，如果某个服务线程满或访问超时，返回提示（服务器正忙）并且服务降级，防止服务雪崩。
//@EnableCircuitBreaker//服务熔断和hystrix  
//@EnableDiscoveryClient//增加客户端注解 eureka  
//@SpringBootApplication//启动类  

@SpringCloudApplication//相当于增加上面三个注解，因为微服务eureka一般包含这三个（启动类加）

@DefaultProperties(defaultFallback = "queryByIdFallBack")设置返回超时错误提示，返回的错误方法为queryByIdFallBack()  
需要在方法上@HystrixCommand  

@HystrixCommand(commandProperties = {  
配置超时时长，name在HystrixCommandProperties找。或在yaml配置。  
@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")}) 

重点是熔断器（默认开启），当最近请求20都超过50%请求超时（默认），开启熔断，此时所有都不能访问。熔断开启计时5秒（默认），5秒后半开状态，放行一部分，若还是超时，则继续熔断，一直循环，直到放行通过时，关闭熔断器。异常也会触发熔断。

## -----------------Feign-----------------  
Feign用来封装远程调用，看起来更优雅，此时RestTemplate不需要了。因为还包含了负载均衡Ribbon和熔断Hystrix，所以不需要再引用这两个包，不过玩法不同了，简便了。(黑马引入了Hystrix包，因为他们不知道启动类入口注解开启了Hystrix，实际入口注解不开启这个，就可以不需要引入这个包。但我们还是学他的吧)  
@EnableFeignClients//启动入口开启feign  
然后编写接口  
@FeignClient("user-service")//去eureka根据服务名获取ip进行远程调用  
public interface UserClient {  
    @GetMapping("user/{id}")//路径  
    User queryById(@PathVariable("id") Long id);} //返回值

## -----------------Zuul网关-----------------  
认证、安全、限流、负载等等。他已经内置了负载均衡Ribbon和Hystrix保护机制  
zuul默认给每一个eureka配置了一个映射路径，可以自定义配置。可以去除不需要的微服务网址，不然每一个微服务都可以用来访问。  
可配置路由前缀和取消路由前缀。这些都需要在yaml配置。 
 
zuul的权限，也称为过滤器，需要实现方法ZuulFilter。  
filterType（过滤器类型）  
filterOrder（过滤器顺序）  
shouldFilter（要不要过滤）  
run（过滤逻辑）  
具体看一下过滤器执行生命周期  

# -----------------总结-----------------  
eureka注册中心、ribbon负载均衡、hystrix熔断保护、feign远程调用、zuul网关。  
运维需要的，没用到，但项目部署需要用到，自行学习：  
spring-cloud-config：统一配置中心，需要配合git  
spring-cloud-bus：消息总线  
spring-cloud-stream：消息通信  
spring-cloud-hystrix-dashboard：容错统计，形成图形化界面  
spring-cloud-sleuth：链路追踪 结合zipkin 也是图形化界面
  
