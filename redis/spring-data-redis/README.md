

## Redis Repositories

User Entity
```kotlin
@RedisHash("User")
class User(
    @Id
    var id: String? = null,
    val username: String,
    val role: Role,
    val createdAt: Instant = Instant.now(),
    @TimeToLive
    val ttl: Long
)

```

User Repository
```kotlin
@Repository
interface UserRepository : CrudRepository<User, String>
```

Config
```kotlin
@Configuration
@EnableRedisRepositories
class RedisRepositoryConfig {
}
```

Repository Test
```kotlin
class UserRepositoryTest(
    @Autowired val repository: UserRepository
) {

    @Test
    fun save() {
        val user = User(
            username = "user01",
            role = Role.USER,
            ttl = 60L
        )

        repository.save(user)

        val savedUser = repository.findByIdOrNull(user.id!!)
        assertThat(savedUser?.id).isEqualTo(user.id)
    }
}
```

## Refs
- https://docs.spring.io/spring-data/redis/reference/




