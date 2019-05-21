# 乐优商城

服务框架为SpringBoot分布式微服务

ly-gateway网关  
ly-registry注册中心  
ly-common复用工具类组件  
ly-item有interface和service，一个用来对外告诉自身的pojo构造函数，一个用来实现（不对外）。  
ly-upload用来上传图片等东西的微服务，通用上传  
ly-search搜索微服务，elasticsearch搜索等大数据搜索用的  
ly-page放置静态页面Thymeleaf的(因只做后台，所以该微服务未做详细的item.html)    

/////////////////////////////////////插件///////////////////////////////////////////  
通用mapper和分页助手也很好用，只要是单个数据库查询的，都可以使用通用mapper    
mapper接口继承的类有很多，现在举个例子：IdListMapper（根据批量id查询、删除），Mapper（通用mapper，包含各种普通新增改查）,InsertListMapper(批量新增)。
请注意，这些继承的，有些包是不同的，所以功能不同。就比如InsertListMapper，有两个包，一个只可以识别id进行批量新增，一个可以不是id批量新增 

lombok很好用  
@Getter//为非final字段添加  
@NoArgsConstructor //自动生成无参数构造函数。  
@AllArgsConstructor //自动生成全参数构造函数。 

/////////////////////////////////////注意///////////////////////////////////////////  
记住扫描controller包的时候，spring启动函数放到包外，不然扫不到。   

///////////////////////////////////文件上传/////////////////////////////////////////   
①、所有请求经过网关时，springMVC会先预处理，并缓存，对普通请求没什么影响，但对上传文件会造成网络负担，在高并发时，有可能造成网络阻塞。所以在请求路径前加一个/zuul，这个默认值是可以更改的
这个还是经过网关，但不会进行缓存，这样就不会有压力了。此时我们需要使用Ngin进行地址重写，使用rewrite重写，这样服务器就不用改代码了。  
具体在nginx的config下的sercer字段下面添加：location /api/upload{ rewrite "^/(.*)$" /zuul/$1; }    
②、采用分布式文件储存中小型用FastDFS，大型用HDFS（会切割文件，这样读取和上传速度会很快）。需要安装，因为需要使用虚拟机，和Nginx一样，暂未实现。  
③、使用FastDFS可以用java的别人写的api，测试例子已经有了。  
④、前端添加图片或者头像那些，其实是点击图片，会上传，此时返回一个图片url，然后在界面显示图片的。因为修改名字和头像是不同步的。     

///////////////////////////////////跨域问题/////////////////////////////////////////  
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

////////////////////////////Elasticsearch/////////////////////////////////////////  
官网：https://www.elastic.co/cn/    
了解solr，毕竟也是搜索，但配置麻烦，而且有延时，技多不压身  
当数据达到PB级别时，数据库全文查找将会有延时，而elasticsearch近实时搜索，并且遵循rest原则、而且分布式，无需人工搭建集群   
其底层需要依赖java，基于Lucene实现，所以把这个配置java虚拟机内存调小点，不然会很卡。配置jvm.options设置内存，设置elasticsearch.yml设置名字、集群、地址、端口等，他默认端口是9200，http端口。还会启动一个9300端口，这个是tcp端口，集群通信用的。  
因为这个没有界面，需要安装插件，不过一般安装Kibana，其底层依赖node.js，也是在那个官网下载，他这也有个kibana.yml文件，用以修改连接elasticsearch地址的。其默认端口为5601。  
另外还需要安装ik分词器，属于elasticsearch插件，用以中文分词  
ik分词器"analyzer": "ik_smart"用以人性化分类，而ik_max_word则细分。elasticsearch并且支持http的rest风格访问。   

elasticsearch需要创建索引，其实类似mysql      


/////////////////////////////////后记//////////////////////////////////////////////  
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
  
