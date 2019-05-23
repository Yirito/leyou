# 乐优商城

服务框架为SpringBoot分布式微服务  
===================================

ly-gateway网关  
ly-registry注册中心  
ly-common复用工具类组件  
ly-item有interface和service，一个用来对外告诉自身的pojo构造函数，一个用来实现（不对外）。  
ly-upload用来上传图片等东西的微服务，通用上传  
ly-search搜索微服务，elasticsearch搜索等大数据搜索用的  
ly-page放置静态页面Thymeleaf的(因只做后台，所以该微服务未做详细的item.html)    

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
             
  
## -----------------Thymeleaf-----------------
静态页面，因只涉及后台，这里暂不多说明，可以自行查看ly-page源码

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

      
#SpringCloudDemo      
      
-----------------SpringCloud-----------------  
------------------------------------------------  
按照module、微服务形式进行学习cloud，从SpringCloudDemo学习。  

要在父项目新增module，若想不麻烦，还需配置maven父目录。  
使用springboot架构，具体学习可以看前面项目  
因为使用module，所以可以使用idea右边maven projects 点一下刷新，右下角会弹出一个框，点击show run dashboard。（或者直接点下面的Run Dashboard）

# -----------------项目介绍-----------------  
核心是高并发

eureka-server注册中心  
consumer-demo服务调用者  
user-service服务提供者  
gateway网关Zuul   
有这几个的好处是，微服务分离低偶尔，consumer动态调用服务（此时可以不需要知道ip地址）

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
  
