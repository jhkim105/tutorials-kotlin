



## Concurrency Vs Parallelism

### Concurrency
- 동시에 실행되는 것 처럼 보임
- 논리적으로 동시에 실행되지만, 실제로는 번갈아 가며 처리
- 작업간 Context Switching 이 발생할 수 있음
- Coroutine, VirtualThread, WebFlux(reactor)
- 싱글코어에서 비동기적으로 여러 작업을 처리
- I/O Bound

### Parallelism
- 멀티코어 CPU 에서 멀티 스레드를 활용한 병렬 처리
- Dispatchers.Default, ForkJoinPool
- CPU Bound

## Dispatchers.IO Vs Dispatchers.Default
|           | Dispatchers.IO          | Dispatchers.Default    |
|-----------|-------------------------|------------------------|
| 용도        | I/O Boumd(DB, 파일, 네트워크) | CPU Bound (연산, 데이터 처리) |
| 스레드 풀 크기 | 기본적으로 최대 64개            | CPU Core 수와 동일         |

### Dispatchers.IO 스레드 풀 크기 조정
- 기본적으로 64개
- kotlinx.coroutines.io.parallelism 시스템 프로퍼티로 설정 가능
```shell
-Dkotlinx.coroutines.io.parallelism=128
```

## Refs
- https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html