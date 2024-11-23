

## Batch 기본
### JobParameter 와 Scope
실행시 파라미터를 전달받아 사용할 수 있다. 
- @Value("#{jobParameters[requestDate]}"
- Scope 를 선언해야 한다 (@JobScope, @StepScope)
  * JobParameter의 Late Binding이 가능
- @JobScope 에서는 jobParameters 와 jobExecutionContext 만 사용가능
- spring boot 3 (spring batch 5) 이전 버전에서 JobParameter 타입은 Double, Long, Date, String 만 가능하다.
- 실행시 인자로 전달
  - java -jar spring-batch.jar --job.name=simple-job filename=stock
  
```java
    @Bean("${JOB_NAME}_step1")
    @JobScope
    fun step1(@Value("#{jobParameters[requestDate]}") requestDate: LocalDateTime?): Step {

```
![img.png](img.png)


## Job Flow
### BatchStatus VS ExitStatus
- BatchStatus: Batch 실행 결과
- ExitStatus: Step 실행 결과

### JobExecutionDecider 를 통한 분기처리




## Refs
- https://www.baeldung.com/spring-boot-spring-batch
