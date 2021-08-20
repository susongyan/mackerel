# mackerel
based on jdbc api-4.0 
- Macherel represent for a resource/connection
- MacherelCan (a macherel container) contains serveral macherel ready to use

## MackerelConfig
## maxWait
client's max waiting milliseconds for a connection

## maxLifetime
a macherel can live after born(default 7 hours) that a slice shorter than mysql's 8 hours(wait_timeout) which auto close connections after being idle so long;
see [mysql-connection-options](https://dev.mysql.com/doc/connectors/en/connector-net-8-0-connection-options.html)

strong liveness guarantee, would not affect in using ones;

## 什么时候释放常驻连接池的连接？ 
业务低峰期，比如凌晨到早高峰 24:00:00 ~ 第二天8点 肯定是超过服务端wait_time的
这个时候，拿出来的连接都被服务端给关闭了，都需要重新连，会对业务有影响，也可能对数据库造成瞬时连接压力

所以，需要在数据库服务端wait_time之前，把常驻连接池的连接给renew一下，有个检查线程定期检测 

实际上，具有一定规模的公司，微服务的机器数可能是几十甚至几百，就会对数据库的连接数的造成压力，而且mysql会把一些资源缓存在长连接上(sort_buffer_size  + read_buffer_size  + read_rnd_buffer_size  + join_buffer_size  + thread_stack  + binlog_cache_size)，长连接较多，太久没释放对内存也会有压力；
通常dba会把 wait_time设置为 1~2小时，期望快些释放压力； 

### maxLifetime 还是 maxIdleTime?  
前提，都要小于数据库服务端的 wait_time 

高并发的服务，一般几分钟甚至一分钟一次ygc，当db连接被清除的时候 肯定已经在老年代了

maxLifeTime
- 如果是 maxLifetime 概念，即使连接最近还是活跃的，一过maxLifetime就变为gc垃圾，导致old区存在大量待回收的db连接；cms的话 final remark 阶段扫描的引用数量会很多，耗时就会增加，对于高并发的服务（stw比较敏感）影响就比较大
- 不过 maxLifetime 能更有效的清理db连接资源，减轻数据库的压力

maxIdleTime
- 常驻连接池的连接（min），只要是活跃的就不会被清理，也不会被数据库服务端断开，对业务服务来说，池子里一直有可用的连接，减少了连接建立的开销 
- 连接可能维持很久，一直占着数据库服务端连接的资源 

个人倾向： maxIdleTime
- 低峰期，保证maxIdle < wati_time < 低峰时段，就能保证进入高峰期的时候，不会说都需要重新建立连接，影响请求rt 和 db瞬时连接压力

## testWhileIdle
default true, if macherel taken is idle more than `validateWindow`，`select 1` will be used to check if the macherel is alive yet;
not worry about performance，the process like ping takes little expend； 

testWhileIdle=true时，取出的连接如果空闲多久`validateWindow` 就要检测活性？
- hikariCP 默认是500毫秒
- druid的 minEvictableIdle 默认30分钟，最小3秒; 它同时也是清除 min~max 这部分线程的依据

使用线程池，理想的情况下是恰好能满足qps的需求，大部分线程都处在活跃状态，所以呆在池内的空闲线程应该不会很多 

## 如何检测连接是否存活？
jdbc4.x 规范定义了 Connection#isValid(int timeout)，具体实现交给各数据库厂商提供的驱动(低版本的驱动可能不支持)，通常是发送一个检测sql或者其他机制
如 
- mysql connector里边有会话机制，发送ping来检查连接活性 
- pgsql 则是发送一个空语句(preparedStatement)

所以，用这个作为检测连接活性，能发挥数据库厂商驱动的优化 

## 连接异常断开如何快速发现？
- 在数据库升级的时候，短时间内连接可能会全部不可用
- 也可能是数据库故障、网络抖动等，导致客户端现存连接都不可用了
- 也可能是有些其他问题导致连接被数据库标记为不可用
这些场景就需要尽快检测到，并且主动剔除这些不可用的连接, 所以就要识别SQLException中带有的连接相关的错误码（比如 08开头的错误码），如果是连接不可用相关的错误码就需要剔除掉这个连接，以免它继续荼毒后续的sql执行

- [postgreSql ErrorCode](https://www.postgresql.org/docs/9.4/errcodes-appendix.html)
- [mysql error code](https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html)

也就是说在 jdbc api 里边抛出 SQLException 的方法里，都要做数据库错误码的归类和处理
## supports
based on jdbc4+, [jdbc4 specification](https://download.oracle.com/otndocs/jcp/jdbc-4.0-pr-spec-oth-JSpec/)

- mysql connector 8.x recommended [mysql&java version](https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-versions.html)


## jdbc DriverManager 数据库驱动 driver 注册逻辑
- jdbc4.0 自动加载 `System.getProperty("jdbc.drivers")` 中设置的冒号隔开的 driver
- jdbc4.0 自动加载基于 ServiceLoader SPI 暴露的 Driver
- Driver 主动调用 DriverManager#registerDriver 注册，通常Driver会在静态代码块中进行注册，所以 Class.forName({driverClassName}) 就能注册相应的 driver；

- mysql connector 早在[5.0.0(2005-12-22)](https://dev.mysql.com/doc/relnotes/connector-j/5.1/en/news-5-0-0.html) 版本就添加了 META-INF/services/java.sql.Driver 文件支持 service-provicer SPI
- pg connector 也在[Version 42.2.13(2020-06-04)](https://jdbc.postgresql.org/documentation/changelog.html#version_42.2.19) 之后支持了

 所以不再需要在代码里显示设置 driverClassName 或者 jdbcUrl 的前缀去匹配对应的 driverClassName 然后 Class.forName 的形式去注册驱动 那么繁琐了， 只要对应的数据库驱动在classpath路径下，就会自动加载注册

## 默认连接池个数多少？
在索引合理的情况下，并且在数据库引擎的缓冲池 buffer pool 作用下，简单sql的执行其实是毫秒级的，按 10ms算，一个连接1秒能执行 1000/10=100个sql，那么连接池内常驻 10 个连接，能应对 100 * 10 = 1000 qps的请求，足够非高并发服务的数据库请求使用了；

## 怎么检查并销毁不可用连接
- 定时检查空闲连接的时候，如果检查失败 则销毁
- 取出连接时，如果空闲超过一定时间，也进行检查
- 以上两个检测都需要连接空闲一定时间后才进行检查，但是故障总是无时无刻发生的，如果故障是在检查间隙中发生，比如：由于数据库服务端的原因导致连接关闭、或者网络问题导致底层tcp连接不可用，这时候也要能快速识别到，快速销毁不可用连接才行，这个时候就要在sql执行异常的时候基于特定的code判定连接是否不可用

## connection代理类
由于需要在连接关闭的时候，将其归还至连接池中，需要改写 Connection#close() 的行为，所以需要在底层厂商的连接实现基础上封装个代理类； 

另外，db连接是具有一些属性设置api的，我们不排除应用在连接使用的时候修改属性状态，所以在连接归还的时候需要重置这些属性到初始状态，避免影响下一个连接使用者的正常使用； Connection 定义了以下属性set api：
- void setAutoCommit(boolean autoCommit) throws SQLException; // 设置事务自否自动提交,发送命令到服务端(SET autocommit=1、0)
- void setReadOnly(boolean readOnly) throws SQLException; // 设置只读连接, 发送命令到服务端
- void setCatalog(String catalog) throws SQLException; // 切换catalog（对于mysql是database), 发送命令到服务端
- void setTransactionIsolation(int level) throws SQLException; // 切换事务隔离级别, 发送命令到服务端
- void setSchema(String schema) throws SQLException; // 切换schema (对于mysql无效), 发送命令到服务端
- void setTypeMap(java.util.Map<String,Class<?>> map) throws SQLException; // 设置自定义类型映射

- void setHoldability(int holdability) throws SQLException; // 设置resultSet是否在事务提交后还持有游标， mysql的实现是空; 
- void setClientInfo(String name, String value) / void setClientInfo(Properties properties) // 设置客户端信息（ApplicationName、ClientUser、ClientHostname), 不会对服务端sql执行有影响，只在客户端测用来做诊断、调试
- void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException; // 设置连接级别的网络超时时长，超过这个时间则认为连接已关闭; pg不支持

对sql在当前连接会话上执行有影响的属性：autoCommit、readOnly、catalog、schema、trasactionIsolation、networkTimeout 

