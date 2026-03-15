
## Entity Event Handling
- @EntityListeners, @PreUpdate 등의 annotation 을 사용
- org.hibernate.event.spi.XXX Listener 구현

### @EntityListeners 

```kotlin
@EntityListeners(StockEntityListener::class)
@Entity
class Stock( /* 생략 */ )
```

```kotlin
@Component
class StockEntityListener(
    private val applicationContext: ApplicationContext
) {

    private lateinit var stockHistoryRepository: StockHistoryRepository

    @PreUpdate
    fun onPreUpdate(stock: Stock) {
        if (!::stockHistoryRepository.isInitialized) {
            stockHistoryRepository = applicationContext.getBean(StockHistoryRepository::class.java)
        }

        val isStockCodeChanged = stock.previousStockCode != stock.stockCode
        val isExchangeCodeChanged = stock.previousExchangeCode != stock.exchangeCode

        if (isStockCodeChanged || isExchangeCodeChanged) {
            stockHistoryRepository.save(
                StockHistory(
                    stockId = stock.id,
                    exchangeCode = stock.exchangeCode,
                    stockCode = stock.stockCode,
                    businessDate = LocalDate.now(),
                    createdAt = LocalDateTime.now()
                )
            )
        }
    }
}
```

### Hibernate Event Listener 구현

Listener 구현
```kotlin
@Component
class StockEventListener(
    private val stockHistoryRepository: StockHistoryRepository
) : PostInsertEventListener, PreUpdateEventListener, PostUpdateEventListener {
 
    override fun onPostUpdate(event: PostUpdateEvent) {
        val entity = event.entity
        if (entity !is Stock) return
        log.info("onPostUpdate")
        event.session.actionQueue.registerProcess { success, _ ->
            if (success) {
                log.info("onPostUpdate PostCommit success: [{}]", event.entity)
            }
        }
    }

```
Listener 등록
```kotlin
@Configuration
@DependsOn("entityManagerFactory")
class JpaListenerConfig(
    private val entityManagerFactory: EntityManagerFactory,
    private val stockEventListener: StockEventListener,
    private val stockCommitEventListener: StockCommitEventListener
) : InitializingBean {

    override fun afterPropertiesSet() {
        val sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor::class.java)
        val registry = sessionFactory.serviceRegistry.getService(EventListenerRegistry::class.java)!!
        registry.appendListeners(EventType.POST_INSERT, stockEventListener)
        registry.appendListeners(EventType.PRE_UPDATE, stockEventListener)
        registry.appendListeners(EventType.POST_UPDATE, stockEventListener)
        registry.appendListeners(EventType.POST_COMMIT_UPDATE, stockCommitEventListener)
    }
}
```

## MySQL Logging
```text
SET global log_output='table'
SET global general_log=1
```

```sql
SELECT * FROM mysql.general_log;
```



