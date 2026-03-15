
##

worker count 지정
```text
-Dreactor.netty.ioWorkerCount=1
```

log 에서 확인
```text
2025-05-11T00:02:35.601+09:00 DEBUG 16716 --- [spring-webclient] [           main] reactor.netty.tcp.TcpResources           : [http] resources will use the default LoopResources: DefaultLoopResources {prefix=reactor-http, daemon=true, selectCount=1, workerCount=1}

```
## Metrics

```text
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-core")
```

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.metrics.enable.reactor.netty=true
```


- http://localhost:8080/actuator/metrics
```text
    "reactor.netty.connection.provider.active.connections",
    "reactor.netty.connection.provider.idle.connections",
    "reactor.netty.connection.provider.max.connections",
    "reactor.netty.connection.provider.max.pending.connections",
    "reactor.netty.connection.provider.pending.connections",
    "reactor.netty.connection.provider.pending.connections.time",
    "reactor.netty.connection.provider.total.connections",
```

- http://localhost:8080/actuator/metrics/reactor.netty.connection.provider.total.connections
```json
{
  "name": "reactor.netty.connection.provider.total.connections",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 1
    }
  ],
  "availableTags": [
    {
      "tag": "name",
      "values": [
        "custom"
      ]
    },
    {
      "tag": "remote.address",
      "values": [
        "localhost:8888"
      ]
    },
    {
      "tag": "id",
      "values": [
        "1250766881"
      ]
    }
  ]
}
```