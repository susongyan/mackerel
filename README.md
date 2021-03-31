# mackerel
based on jdbc api-4.0
- Macherel represent for a resource/connection
- MacherelCan (a macherel container) contains serveral macherel ready to use


## MacherelConfig

### maxLifetime
a macherel can live after born(default 7 hours) that a slice shorter than mysql's 8 hours(wait_timeout) which auto close connections after being idle so long;
see [mysql-connection-options](https://dev.mysql.com/doc/connectors/en/connector-net-8-0-connection-options.html)

strong liveness guarantee, would not affect in using ones

### testWhileIdle
default true, if macherel taken is idle more than `idleTimeBeforeTest`，`select 1` will be used to check if the macherel is alive yet;
not worry about performance，the process like ping takes little expend； 