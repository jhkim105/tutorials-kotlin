

## Log Compaction
- cleanup.policy=compact
- 메시지 전송시 메시지 키를 같이 보내야 한다
- 각 키에 대한 최신값을 유지, 이전 값은 삭제 (log compaction) -> 공간 절약
- https://kafka.apache.org/documentation/#compaction
- ![log-compaction.png](log-compaction.png)
## References

- [Spring Boot Kafka](https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.kafka)
- [Baeldung](https://www.baeldung.com/spring-kafka)
- [Apache Kafka](https://kafka.apache.org/documentation/#gettingStarted) 