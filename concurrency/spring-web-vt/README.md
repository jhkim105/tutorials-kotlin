

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

### Coroutine Dispatchers and Virtual Thread