

## Virtual thread 사용 설정
```properties
spring.threads.virtual.enabled=true
```

## Parallel Processing

### CompletableFuture

### Coroutine Dispatchers.IO
- 요청수에 따리 지연이 있음
- Dispatchers.IO 스레드 수 제한 영향
- 스레드 수를 늘리면 지연 없음
```
Percentage of the requests served within a certain time (ms)
  50%   1012
  66%   1997
  75%   1998
  80%   1999
  90%   2001
  95%   2003
  98%   2014
  99%   2018
 100%   2019 (longest request)

```
### Coroutine Dispatchers and Virtual Thread