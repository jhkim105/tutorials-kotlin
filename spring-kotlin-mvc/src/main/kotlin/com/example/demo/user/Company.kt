package com.example.demo.user

import com.example.demo.ColumnLengths
import org.hibernate.annotations.GenericGenerator
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Company(
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", length = ColumnLengths.UUID)
    var id: String? = null,
    var name: String
) {

    override fun toString(): String {
        return "Company(id=$id, name='$name')"
    }


}