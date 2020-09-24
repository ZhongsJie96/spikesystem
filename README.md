# Spring自定义ArgumentResolver参数解析器

1. Spring MVC通过以注解往函数添加额外信息的方式，使得**servlet请求转换到java**的数据转换过程能够交由框架自动处理。

2. 参数解析器

   - `handlerMethodArgumentResolver.java`

   - ```java
     public interface HandlerMethodArgumentResolver {
        boolean supportsParameter(MethodParameter parameter); 
        Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, 
                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception;
     }
     ```

     - `MethodParameter`是spring对被注解修饰过参数的包装，从其中能拿到**参数的反射相关信息**。
     - `supportsParameter`传入一个参数，用以判断此参数是否能够使用该解析器。
     - `resolveArgument`就是之前讨论的解析函数，传入必要信息，计算并返回一个值。
     - 综合来看，框架会将每一个`MethodParameter`传入`supportsParameter`测试是否能够被处理，如果能够，就使用`resolveArgument`处理。

# 两次MD5加密

1. 第一次 （在前端加密，客户端）：密码加密是（明文密码+固定盐值）生成md5用于传输，目的，由于http是明文传输，当输入密码若直接发送服务端验证，此时被截取将直接获取到明文密码，获取用户信息。
   - 加盐值是为了混淆密码，原则就是明文密码不能在网路上传输
2. 第二次：在服务端再次加密，当获取到前端发送来的密码后。通过MD5（密码+随机盐值）再次生成密码后存入数据库。
   - 防止数据库被盗的情况下，通过md5反查，查获用户密码。方法是盐值会在用户登陆的时候随机生成，并存在数据库中，这个时候就会获取到

# CodeMsg类

> CodeMsg用于保存 异常码和异常信息

1. 私有化构造方法，并且使得所有set方法失效，保证接口健壮性

# 集成Redis

1. 添加Jedis依赖

   ```xml
   <dependency>
     <groupId>redis.clients</groupId>
     <artifactId>jedis</artifactId>
   </dependency>
   ```

2. 添加Fastjson依赖

   ```xml
   <dependency>
     <groupId>com.alibaba</groupId>
     <artifactId>fastjson</artifactId>
     <version>1.2.73</version>
   </dependency>
   ```


## 通用缓存key封装

1. 当项目中的模块越来越多的时候，需要存的缓存也越来越多，比如商品Id,订单Id，用户id等,此时若是id出现重复，将给系统带来错误。

2. 方法：利用一个前缀来规定不同模块redis缓存的key

   - ```java
     public interface KeyPrefix {
         /** 过期时间 */
         public int expireSeconds();
         /** 获得前缀*/
         public String getPrefix();
     }
     /** 抽象类  作为前缀类的父类用于继承
      * */
     public abstract class BasePrefix implements KeyPrefix {
     	// 过期时间
         private int expireSeconds;
     
         private String prefix;
     
         public BasePrefix(String prefix) {
             this.expireSeconds = 0;
             this.prefix = prefix;
         }
     
         public BasePrefix(int expireSeconds, String prefix) {
             this.expireSeconds = expireSeconds;
             this.prefix = prefix;
         }
     
         @Override
         public int expireSeconds() {
             return expireSeconds;// 定义 0 为用永不过期
         }
     
         @Override
         public String getPrefix() {
             String className = this.getClass().getSimpleName();
             return className + ":"+ prefix;
         }
     }
     ```

3. 为了将不同不同的bean缓存到redis需要使用stringToBean以及beanToString方法

   - ```java
     // 其他类型 默认为Bean对象 利用JSON 转化为json串
     // 利用阿里的 库 fastjson 将对象转化为 json 串  序列化和反序列化
     JSON.toJSONString(value);
     // 转换为对象
     JSON.toJavaObject(JSON.parseObject(str), clazz)
     ```

## RedisService

1. 通过JedisPool获取Jedis资源，然后利用Jedis来完成缓存的读写，最后returnToPool(jedis)
   - `jedis.del()`
   - `jedis.incr()`
   - `jedis.decr()`
   - `jedis.exists()`
   - `jedis.set()`
   - `jedis.get()`
2. 真实的redis key将包含类名:前缀以及key值
   - `String realKey = predix.getPrefix() + key;`

## Jedis/Redis配置

> **Jedis** :jedis就是集成了redis的一些命令操作，封装了redis的java客户端。提供了连接池管理。一般不直接使用jedis，而是在其上在封装一层，作为业务的使用。

1. RedisConfig:配置Redis连接的相关属性
2. RedisPoolFactory 通过配置文件，**生成Jedis连接池**（配置），方便在RedisService中调用。

# 集成Mybatis

> MyBatis 是一款优秀的**持久层框架**，它支持自定义 SQL、存储过程以及高级映射。MyBatis 免除了几乎所有的 JDBC 代码以及设置参数和获取结果集的工作。MyBatis 可以通过简单的 XML 或注解来配置和映射原始类型、接口和 Java POJO（Plain Old Java Objects，普通老式 Java 对象）为数据库中的记录。

1. maven依赖

   ```xml
   <dependency>
     <groupId>org.mybatis</groupId>
     <artifactId>mybatis</artifactId>
     <version>x.x.x</version>
   </dependency>
   ```

2. 从XML构建SqlSessionFactury

   - 每个基于 MyBatis 的应用都是以一个 **SqlSessionFactory** 的实例为核心的。SqlSessionFactory 的实例可以通过 SqlSessionFactoryBuilder 获得。而 SqlSessionFactoryBuilder 则可以从 XML 配置文件或一个预先配置的 Configuration 实例来构建出 SqlSessionFactory 实例。

   - ```java
     String resource = "org/mybatis/example/mybatis-config.xml";
     InputStream inputStream = Resources.getResourceAsStream(resource);
     SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
     ```

   - XML 配置文件中包含了对 MyBatis 系统的核心设置，包括获取数据库连接实例的数据源（DataSource）以及决定事务作用域和控制方式的事务管理器（TransactionManager）

   - ```xml
     <?xml version="1.0" encoding="UTF-8" ?>
     <!DOCTYPE configuration
       PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
       "http://mybatis.org/dtd/mybatis-3-config.dtd">
     <configuration>
       <environments default="development">
         <environment id="development">
           <transactionManager type="JDBC"/>
           <dataSource type="POOLED">
             <property name="driver" value="${driver}"/>
             <property name="url" value="${url}"/>
             <property name="username" value="${username}"/>
             <property name="password" value="${password}"/>
           </dataSource>
         </environment>
       </environments>
       <mappers>
         <mapper resource="org/mybatis/example/BlogMapper.xml"/>
       </mappers>
     </configuration>
     ```

3. 集成pom依赖：mybatis-spring-boot-starter

4. 添加mysql客户端

# druid

>  JDBC 连接池、监控组件 Druid

1. 可以监控数据库访问性能，Druid内置提供了一个功能强大的StatFilter插件，能够详细统计SQL的执行性能，这对于线上分析数据库访问性能有帮助。 
2. 替换[DBCP](http://www.oschina.net/p/dbcp)和[C3P0](http://www.oschina.net/p/c3p0)。Druid提供了一个高效、功能强大、可扩展性好的数据库连接池。 
3. 数据库密码加密。直接把数据库密码写在配置文件中，这是不好的行为，容易导致安全问题。DruidDruiver和DruidDataSource都支持PasswordCallback。 
4. SQL执行日志，Druid提供了不同的LogFilter，能够支持[Common-Logging](http://www.oschina.net/p/commons+logging)、[Log4j](http://www.oschina.net/p/log4j)和JdkLog，你可以按需要选择相应的LogFilter，监控你应用的数据库访问情况。

# 实现登录功能

1. 数据库设计

   ```mysql
   CREATE TABLE `spike_user` (
      	`id` BIGINT ( 20 ) NOT NULL COMMENT '用户ID，手机号码',
      	`nickname` VARCHAR ( 255 ) NOT NULL,
      	`password` VARCHAR ( 32 ) DEFAULT NULL COMMENT 'MD5(MD5(pass明文+固定salt) + salt)',
      	`salt` VARCHAR ( 10 ) DEFAULT NULL,
      	`head` VARCHAR ( 128 ) DEFAULT NULL COMMENT '头像，云存储的ID',
      	`register_data` datetime DEFAULT NULL COMMENT '注册时间',
      	`last_login_date` datetime DEFAULT NULL COMMENT '上次登录时间',
      	`login_count` INT ( 11 ) DEFAULT 0 COMMENT '登录次数',
      PRIMARY KEY ( `id` ) 
      ) ENGINE = INNODB DEFAULT CHARSET = utf8mb4
   
   ```

2. 明文密码两次MD5处理

   - 用户端：PASS=MD5（明文+固定Salt）

   - 服务端：PASS=MD5（用户输入+随机Salt）

     - **MD5消息摘要算法**（英语：MD5 Message-Digest Algorithm），一种被广泛使用的[密码散列函数](https://zh.wikipedia.org/wiki/密碼雜湊函數)，可以产生出一个128位（16[字节](https://zh.wikipedia.org/wiki/位元组)）的散列值（hash value）

     - 无法防止碰撞攻击，MD5已经被禁止用作[密钥散列消息认证码](https://zh.wikipedia.org/wiki/金鑰雜湊訊息鑑別碼)。对于需要高度安全性的资料，专家一般建议改用其他算法，如[SHA-2](https://zh.wikipedia.org/wiki/SHA-2)

       

   - Salt值

     - **盐**（Salt），在密码学中，是指在[散列](https://zh.wikipedia.org/wiki/散列)之前将散列内容（例如：密码）的任意固定位置插入特定的字符串。这个在散列中加入字符串的方式称为“加盐”。其作用是让加盐后的散列结果和没有加盐的结果不相同，在不同的应用情景中，这个处理可以**增加额外的安全性**
     - salt值只是为了**防御彩虹表**

3. JSR303参数校验+全局异常处理器

   - 实现参数校验

   - 添加依赖

     ```xml
     <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-validation</artifactId>
     </dependency>
     ```

4. 分布式Session（会话控制）

   - Session与Cookie的区别在于Session是记录在服务端的，而Cookie是记录在客户端的。
   - **Token** 是在服务端产生的。如果前端使用用户名/密码向服务端请求认证，服务端认证成功，那么在服务端会返回 Token 给前端。前端可以在每次请求的时候带上 Token 证明自己的合法地位

5. 引入依赖

   - ```xml
     <dependency>
       <groupId>commons-codec</groupId>
       <artifactId>commons-codec</artifactId>
     </dependency>
     <dependency>
       <groupId>org.apache.commons</groupId>
       <artifactId>commons-lang3</artifactId>
     </dependency>
     ```


## [Token](https://www.cnblogs.com/xuxinstyle/p/9675541.html)

> Token 是在服务端产生的。如果前端使用用户名/密码向服务端请求认证，服务端认证成功，那么在服务端会返回 Token 给前端。前端可以在每次请求的时候带上 Token 证明自己的合法地位

1. 可以解决以下内容
   - Token 完全由应用管理，所以它可以避开同源策略
   - Token 可以避免 CSRF 攻击(http://dwz.cn/7joLzx)
   - Token 可以是无状态的，可以在多个服务间共享
2. 如果这个Token在服务端持久化（比如存入数据库），就是一个永久的身份令牌
3. Token的目的是为了减轻服务器的压力，减少频繁的查询数据库，使服务器更加健壮。
4. 有效期问题
   - 登录密码，一般要求定期改变密码，以防止泄漏，所以密码是有有效期的；
   - 安全证书。SSL 安全证书都有有效期，目的是为了解决吊销的问题
5. 为了解决在操作过程不能让用户感到 Token 失效这个问题
   - 一种方案是在服务器端保存 Token 状态，用户每次操作都会**自动刷新（推迟） Token** 的过期时间——**Session** 就是采用这种策略来**保持用户登录状态**的。
   - 使用 **Refresh Token**，它可以避免频繁的读写操作。这种方案中，服务端不需要刷新 Token 的过期时间，一旦 Token 过期，就反馈给前端，前端**使用 Refresh Token 申请一个全新 Token 继续使用**。这种方案中，服务端只需要在客户端请求更新 Token 的时候对 Refresh Token 的有效性进行一次检查，大大减少了更新有效期的操作，也就避免了频繁读写。
6. 通常为了提升效率，减少消耗，会把Token的过期时间保存在缓存或者内存中。
7. 时序图
   - 登录
     ![image-20200821143417931](pics/image-20200821143417931.png)
   - 业务请求
     ![image-20200821143437232](pics/image-20200821143437232.png)
   - Token过期，刷新Token
     ![image-20200821143516647](pics/image-20200821143516647.png)\
8. 无状态Token
   - 服务器端需要认真Token有效：可以通过对称加密的算法
   - 不需要还原加密内容，可以指定密码的散列算法（HMAC）
9. 如何使用Token
   - 用设备号/设备mac地址作为Token
   - 用session值作为Token
     - 客户端：客户端只需携带用户名和密码登陆即可。
     - 客户端：客户端接收到用户名和密码后并判断，如果正确了就将本地获取sessionID作为Token返回给客户端，客户端以后只需带上请求数据即可。

## 分布式Session

> Session是一个在单个操作人员整个操作过程中，与**服务端保持通信的惟一识别信息**。在同一操作人员的多次请求当中，session始终保证是同一个对象，而不是多个对象，因为可以对其加锁。当同一操作人员多个请求进入时，可以通过session限制只能单向通行。

1. 分布式session的实现方式
   - 基于数据库的Session共享
   - 基于NFS共享文件系统
   - 基于memcached 的session，如何保证 memcached 本身的高可用性？
   - 基于resin/tomcat web容器本身的session复制机制
   - 基于TT/Redis 或 jbosscache 进行 session 共享。
   - 基于cookie 进行session共享
2. 缓存集中式管理
   - 将Session存入分布式缓存集群中的某台机器上，当用户访问不同节点时先从缓存中拿Session信息
3. **Session Replication 方式管理 (即session复制)**
   - 将一台机器上的Session数据广播复制到集群中其余机器上
4. Session Sticky方式管理
   - 即粘性Session、当用户访问集群中某台机器后，强制指定后续所有请求均落到此机器上
5. 登录完成的最后，需要带着Session信息
   - 利用uuid生成token
   - 将用户所对应的token+前缀 缓存到Redis
   - 利用token生成cookie
     - 设置过期时间
   - response写入cookie返回到客户端

## [Cookie](https://www.jianshu.com/p/6fc9cea6daa2)

>  用于解决存储web中的状态信息，以方便服务器端使用

1. HTTP协议本身是无状态的。什么是无状态呢，即服务器无法判断用户身份。Cookie实际上是一小段的文本信息（key-value格式）。客户端向服务器发起请求，如果服务器需要记录该用户状态，就使用**response向客户端浏览器颁发一个Cookie**。客户端浏览器会把Cookie保存起来。当浏览器再请求该网站时，浏览器把请求的网址连同该Cookie一同提交给服务器。服务器检查该Cookie，以此来辨认用户状态。
2. Cookie机制
   - ![img](pics/13949989-dcf024be2733e725.png)
3. Cookie是不可以跨域名的，隐私安全机制禁止网站非法获取其他网站的Cookie。
   - 一级域名又称为顶级域名，一般由字符串+后缀组成。熟悉的一级域名有baidu.com，qq.com。com，cn，net等均是常见的后缀。
   - 二级域名是在一级域名下衍生的，比如有个一级域名为[mcrfun.com](http://mcrfun.com)，则[blog.mcrfun.com](http://blog.mcrfun.com)和[www.mcrfun.com](http://www.mcrfun.com)均是其衍生出来的二级域名。
4. path属性决定允许访问Cookie的路径。比如，设置为"/"表示允许所有路径都可以使用Cookie
5. 修改或者删除Cookie
   - HttpServletResponse提供的Cookie操作只有一个`addCookie(Cookie cookie)`，所以想要修改Cookie只能使用一个**同名的Cookie来覆盖**原先的Cookie。如果要删除某个Cookie，则只需要新建一个同名的Cookie，并将maxAge设置为0，并覆盖原来的Cookie即可。

# 商品列表页

## 数据库设计





# 秒杀实现

1. MySQL使用的是UTC标准时间，因此插入到里面之后会产生八个小时的时差，我们所处的是东8区因此需要设置
   - `serverTimezone=GMT%2B8`
2. 

# JMeter压测

## JMeter入门

1. 

## 自定义变量模拟多用户

1. 测试计划->添加配置文件->CSV数据配置
2. 引用变量${}

## JMeter命令行使用

1. 在windows上录好jmx
2. 命令行：sh jmeter.sh -n -t XXX.jmx -l result.jtl
3. 把result.jtl导入到JMeter

## Redis压测工具redis-benchmark

1. redis-benchmark -h 127.0.0.1 -p 6379 -c 100 -n 100000
   - 100个并发连接，100000个请求
2. redis-benchmark -h 127.0.0.1 -p 6379 -q -d 100
3. redis-benchmark -t set,lpush -n 100000 -q
   - 只侧某些操作
4. redis-benchmark -n 100000 -q script load "redis.call('set')"
5. redis效率高

## Spring Boot打war包

1. 添加spring-boot-starter-tomcat的provided依赖
2. 添加maven-war-plugin插件

# 页面优化技术

1. 页面缓存+URL缓存+对象缓存
2. 页面静态化，前后端分离
3. 静态资源优化
4. CDN优化

## 页面缓存

1. 取缓存
2. 手动渲染模板
   - 
3. 结果输出

## 对象缓存

1. 更细粒度的缓存

## 页面静态化

1. 常用技术AngularJS， Vue.js
2. 优点：利用浏览器的缓存
3. 网页静态HTML化
   - ![你了解大型网站的页面静态化吗？](pics/19d86172c2344067b8ad584c0f3cafcc)
   - ![你了解大型网站的页面静态化吗？](pics/4ebc23d7acd84a47a643e997edf381f4)

## 静态资源优化

1. JS/CSS压缩，减少流量
2. 多个JS/CSS组合，减少连接数
3. CDN就近访问

# 解决超卖

1. 数据库加条件

2. 通过唯一索引避免用户同时发出两个请求

3. 不同用户在读请求的时候，发现商品库存足够，然后同时发起请求，进行秒杀操作，减库存，导致库存减为负数。

   - 更新数据库减库存的时候，进行库存限制条件（stock_count > 0)

   - 超卖的深层原因，是因为数据库底层的写操作和读操作可以同时进行。可以对读操作加上显式锁（select..语句最后加上for update）
   - 应用一个队列缓存，将多线程变为单线程读写

4. 同一个用户在有库存的时候，连续发出多个请求，两个请求同时存在，于是生成多个订单。

   - 将userId和商品Id 加上唯一索引

# 接口优化

1. Redis预减库存减少数据库访问
   - 思路：减少数据库访问
     - 系统初始化，把商品库存数量加载到Redis
     - 收到请求，Redis预减库存，库存不足，直接返回3
     - 请求入队，立即返回排队中
     - 请求出对，生成订单，减少库存
     - 客户端轮询，是否秒杀成功
2. 分库分表
   - Mycat
3. 内存标记减少Redis访问
4. 请求先入队缓存，异步下单，增强用户体验
5. RabbitMQ安装与Spring Boot集成
6. Nginx水平扩展
7. 压测

## 集成RabbitMQ

> RabbitMQ是目前非常热门的一款消息中间件

1. 消息队列提供一个**异步通信机制**，消息的发送者不必一直等待到消息被成功处理才返回，而是立即返回。消息中间件负责处理网络通信，如果网络连接不可用，消息被暂存于队列当中，当网络畅通的时候在将消息转发给相应的应用程序或者服务，当然前提是这些服务订阅了该队列。
2. 如果在商品服务和订单服务之间使用消息中间件，既可以提高并发量，又降低服务之间的耦合度。
3. RabbitMQ是使用**Erlang**语言来编写的，并且RabbitMQ是基于AMQP协议的。
   - AMQP协议是具有现代特征的二进制协议。是一个提供统一消息服务的应用层标准高级消息队列协议，是应用层协议的一个开放标准，为面向消息的中间件设计
4. 典型应用场景
   - **异步处理**。把消息放入消息中间件中，等到需要的时候再去处理。
   - **流量削峰**。例如秒杀活动，在短时间内访问量急剧增加，使用消息队列，当消息队列满了就拒绝响应，跳转到错误页面，这样就可以使得系统不会因为超负载而崩溃。
   - **日志处理**
   - **应用解耦**。假设某个服务A需要给许多个服务（B、C、D）发送消息，当某个服务（例如B）不需要发送消息了，服务A需要改代码再次部署；当新加入一个服务（服务E）需要服务A的消息的时候，也需要改代码重新部署；另外服务A也要考虑其他服务挂掉，没有收到消息怎么办？要不要重新发送呢？是不是很麻烦，使用MQ发布订阅模式，服务A只生产消息发送到MQ，B、C、D从MQ中读取消息，需要A的消息就订阅，不需要了就取消订阅，服务A不再操心其他的事情，使用这种方式可以降低服务或者系统之间的耦合。
5. AMQP协议模型有三部分组成：生产者、消费者和服务端
   ![img](pics/17039633-b0adf1dfade2f122.png)
6. 常用交换器
   - **direct**
     - 该类型的交换器将所有发送到该交换器的消息被转发到RoutingKey指定的队列中，也就是说路由到BindingKey和RoutingKey完全匹配的队列中。
       ![img](pics/1538609-20190720105736817-253615143.png)
   - **topic**
     - 该类型的交换器将所有发送到Topic Exchange的消息被转发到所有RoutingKey中指定的Topic的队列上面。
       ![img](pics/1538609-20190720105754635-2077492605.png)
   - **fanout**
     - 该类型不处理路由键，会把所有发送到交换器的消息路由到所有绑定的队列中。优点是转发消息最快，性能最好。
       ![img](pics/1538609-20190720105808645-873494263.png)
   - **headers**
     - 该类型的交换器不依赖路由规则来路由消息，而是根据消息内容中的headers属性进行匹配。headers类型交换器性能差，在实际中并不常用。
7. RabbitMQ各组件功能
   ![img](pics/5015984-367dd717d89ae5db.png)
   - **`Broker：`**标识消息队列服务器实体.
   - **`Virtual Host：`**虚拟主机。标识一批交换机、消息队列和相关对象。虚拟主机是共享相同的身份认证和加密环境的独立服务器域。每个vhost本质上就是一个mini版的RabbitMQ服务器，拥有自己的队列、交换器、绑定和权限机制。vhost是AMQP概念的基础，**必须在链接时指定**，RabbitMQ默认的vhost是 /。
   - **`Exchange：`**交换器，用来接收生产者发送的消息并将这些消息路由给服务器中的队列。
   - **`Queue：`**消息队列，用来保存消息直到发送给消费者。它是消息的容器，也是消息的终点。一个消息可投入一个或多个队列。消息一直在队列里面，等待消费者连接到这个队列将其取走。
   - **`Banding：`**绑定，用于**消息队列和交换机之间的关联**。一个绑定就是基于路由键将交换机和消息队列连接起来的路由规则，所以可以将交换器理解成一个由绑定构成的路由表。
   - **`Channel：`**信道，多路复用连接中的一条独立的**双向数据流通道**。信道是建立在真实的TCP连接内地虚拟链接，AMQP命令都是通过信道发出去的，不管是发布消息、订阅队列还是接收消息，这些动作都是通过信道完成。因为对于操作系统来说，建立和销毁TCP都是非常昂贵的开销，所以引入了信道的概念，以复用一条TCP连接。
   - **`Connection：`**网络连接，比如一个TCP连接。
   - **`Publisher：`**消息的生产者，也是一个向交换器发布消息的客户端应用程序。
   - **`Consumer：`**消息的消费者，表示一个从一个消息队列中取得消息的客户端应用程序。
   - **`Message：`**消息，消息是不具名的，它是由消息头和消息体组成。消息体是不透明的，而消息头则是由一系列的可选属性组成，这些属性包括routing-key(路由键)、priority(优先级)、delivery-mode(消息可能需要持久性存储[消息的路由模式])等。
8. TTL
   - **TTL(Time To Live)**：生存时间。RabbitMQ支持消息的过期时间，一共两种。
     - 在消息发送时可以进行指定。通过配置消息体的properties，可以指定当前消息的过期时间。
     - 在创建Exchange时可进行指定。从进入消息队列开始计算，只要超过了队列的超时时间配置，那么消息会自动清除。
9. 生产者Confirm确认消息
   - ![img](pics/17039633-95cb101479ed6092.png)
   - 在channel上开启确认模式：`channel.confirmSelect()`
   - 在channel上开启监听：`addConfirmListener`，监听成功和失败的处理结果，根据具体的结果对消息进行重新发送或记录日志处理等后续操作。
10. 如何实现幂等性
    - 消费端实现幂等性，就意味着我们的消息永远不会消费多次，即使我们收到了多条一样的信息。
    - 唯一ID+指纹码机制，利用数据库主键去重
    - 利用Redis的原子性去实现
11. RabbitMQ安装
    - 安装Erlang
      - 解压：`tar xf otp_src_23.0.tar.gz`
      - 配置：`./configure --prefix=/usr/local/erlang23 --without-javac`
      - 编译：`make`
      - 安装：`make install`
      - 验证：`.erl`
    - 安装依赖
      - `yum install python -y`
      - `yum install xmlto -y`
      - `yum install python-simplejson -y`
    - 安装RabbitMQ
12. SpringBoot集成添加依赖
    - 添加依赖spring-boot-starter-amqp
    - 创建消息接受者
    - 创建消息发送者
      - `AmqpTemplate`

## 优化秒杀接口

1. Redis预减库存，RabbitMQ异步下单

# 安全优化

1. 秒杀接口地址隐藏
2. 数学公式验证码
3. 接口限流防刷

## 秒杀接口地址隐藏

1. 思路：秒杀开始之前，先去请求接口获取秒杀地址
   - 接口改造，带上PathVariable参数
   - 添加生成地址的接口
   - 秒杀收到请求，先验证PathVariable

## 数学公式验证码

1. 思路：点击秒杀之前，先输入验证码，分散用户的请求
   - 添加生成验证码的接口
   - 在获取秒杀路径的时候，验证验证码
   - ScriptEngine使用

## 接口防刷

1. 思路：对接口做限流
   - 可以用拦截器减少对业务侵入



# 项目亮点

1. 难点：并发数达到一定量的时候，会对数据库服务器带来很大的压力，**如何缓解这些压力并提高并发的QPS**，

2. 亮点

   - 利用缓存减少数据库的压力，以及读取缓存的速度远远快于数据库
   - 页面静态化技术加快用户访问速度，提高QPS，异步下单增强用户体验，内存标记减少Redis的访问
   - 安全性优化：双重md5密码校验，隐藏接口，限流防刷，数学公式验证

3. 分布式session

   - 利用redis缓存的方法，另外布置一个Redis服务器专门用于存放用户的session信息。这样就不会出现用户session丢失的情况

4. 大量使用了缓存，那么就存在缓存的国企时间控制以及缓存击穿以及缓存雪崩等问题

   - 缓存穿透：使用布隆过滤器

   


