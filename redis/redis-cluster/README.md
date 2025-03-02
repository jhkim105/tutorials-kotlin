## Redis Cluster
- node 실행
```shell
redis-server redis.conf
```
- cluster 시작
```shell
redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 --cluster-replicas 1
```

- cluster 정보
```shell
cluster info
cluster nodes
```

- cluster 초기화
```shell
redis-cli --cluster call 127.0.0.1:7000 cluster reset
```
