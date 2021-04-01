# mackerel
based on jdbc api-4.0 
- Macherel represent for a resource/connection
- MacherelCan (a macherel container) contains serveral macherel ready to use


## MacherelConfig
## maxWait
client's max waiting milliseconds for a connection

### maxLifetime
a macherel can live after born(default 7 hours) that a slice shorter than mysql's 8 hours(wait_timeout) which auto close connections after being idle so long;
see [mysql-connection-options](https://dev.mysql.com/doc/connectors/en/connector-net-8-0-connection-options.html)

strong liveness guarantee, would not affect in using ones;

### testWhileIdle
default true, if macherel taken is idle more than `validateWindow`，`select 1` will be used to check if the macherel is alive yet;
not worry about performance，the process like ping takes little expend； 

testWhileIdle=true时，取出的连接如果空闲多久`validateWindow` 就要检测活性？
- hikariCP 默认是500毫秒
- druid的 minEvictableIdle 默认30分钟，最小3秒; 它同时也是清除 min~max 这部分线程的依据

使用线程池，理想的情况下是恰好能满足qps的需求，大部分线程都处在活跃状态，所以呆在池内的空闲线程应该不会很多 

## 如何检测连接是否存活？
jdbc规范定义了 Connection#isValid(int timeout)，具体实现交给各数据库厂商提供的驱动(低版本的驱动可能不支持)，通常是发送一个检测sql或者其他机制
如 
- mysql connector里边有会话机制，发送ping来检查连接活性 
- pgsql 则是发送一个空语句(preparedStatement)


## supports
based on jdbc4+, [jdbc4 specification](https://download.oracle.com/otndocs/jcp/jdbc-4.0-pr-spec-oth-JSpec/)

- mysql connector 8.x recommended [mysql&java version](https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-versions.html)

