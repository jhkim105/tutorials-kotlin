Spring MVC Kotlin Example
==============
## References


## WorkLog
### Entity 
* private setter 안됨
  - private setters are not allowed for open properties
  - protected 라도 사용해서 제한해야하나? 
* 접근자 제어 위해 생성자 선언방식을 사용못함
  - 생성자에서 선언하는 방식
    ```kotlin
      @Entity
      class User(
          @Id
          @GeneratedValue(generator = "system-uuid")
          @GenericGenerator(name = "system-uuid", strategy = "uuid2")
          @Column(name = "id", length = ColumnLengths.UUID)
          var id: String? = null,
      
          var username: String,
          var password: String,
          var name: String,
          var description: String? = null,
      
          @ManyToOne(fetch = FetchType.LAZY)
          var company: Company? = null
      
      ) {
          override fun toString(): String {
              return "User(id=$id, username='$username', password='$password', name='$name', description=$description)"
          }
      }
    ```
  - body에서 선언
    ```kotlin
      @Entity
      class User(username: String, password:String, name:String) {
      @Id
      @GeneratedValue(generator = "system-uuid")
      @GenericGenerator(name = "system-uuid", strategy = "uuid2")
      @Column(name = "id", length = ColumnLengths.UUID)
      var id: String? = null
      protected set
      
          var username: String = username
              protected set
      
          var password: String = password
              protected set
      
          var name: String = name
              protected set
      
          var description: String? = null
              protected set
      
          @ManyToOne(fetch = FetchType.LAZY)
          var company: Company? = null
              protected set
      
          override fun toString(): String {
              return "User(id=$id, username='$username', password='$password', name='$name', description=$description)"
          }
      }
    ```
  - 이건 나중에.. 하자.

* lazy loading을 사용하려면 final이면 안됨. data class는 open이 안됨(final). 따라서 data class를 사용할 수 없다
  - allopen plugin 
  - noArgs plugin
  
  
#### equals(), hascode(), toString()
* Intellij가 제공하는 Generate를 쓰거나
* kassava 라이브러리를 사용
  https://github.com/consoleau/kassava

### QueryDSL


### Data initialize





### Constants
```kotlin
object ColumnLengths {

    const val UUID = 50
}

```

### DTO
* val 사용 -> private 사용필요 없음






