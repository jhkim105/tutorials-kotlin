package com.example.demo.user

import com.example.demo.ColumnLengths
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*
import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode

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

    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)

    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    companion object {
        private val equalsAndHashCodeProperties = arrayOf(User::id)
    }
}
