package jhkim105.tutorials.user.entity

import jakarta.persistence.*
import jhkim105.tutorials.ColumnLengths
import org.hibernate.Length
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "user", indexes = [Index (columnList = "name")])
class User(
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(length = ColumnLengths.UUID)
    var id: String? = null,

    @Column(unique = true)
    var username: String,
    var password: String,
    var name: String,

    @Column(length = Length.LONG32)
    // column size 에 따라 tinytext, mediumtext, longtext 로 생성됨
    var description: String? = null,

    @Lob
    @Column(length = 1000)
    // column size 에 따라 tinytext, mediumtext, longtext 로 생성됨
    var memo: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val company: Company? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 10)
    val userType: UserType? = null, //not null 인 경우 제약조건 에러 발생함

    @Convert(converter = StringListConverter::class)
    @Column(length = 1000)
    var stringList: List<String>? = null

) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "User(id=$id, username='$username', password='$password', name='$name', description=$description, memo=$memo, company=$company, userType=$userType, stringList=$stringList)"
    }


}

enum class UserType {
    ADMIN, USER
}

