# 乐优商城
ly-gateway网关  
ly-registry注册中心  
ly-common复用工具类组件  
ly-item有interface和service，一个用来对外告诉自身的pojo构造函数，一个用来实现（不对外）。

/////////////////////////////////////插件///////////////////////////////////////////  
lombok真的很好用  
@Getter//为非final字段添加  
@NoArgsConstructor //自动生成无参数构造函数。  
@AllArgsConstructor //自动生成全参数构造函数。 

/////////////////////////////////////注意///////////////////////////////////////////  
记住扫描controller包的时候，spring启动函数放到包外，不然扫不到。   

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