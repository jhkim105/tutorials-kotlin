package jhkim105.tutorials.beanio

import org.beanio.annotation.Field

class Contact(
    var firstName: String? = null,
    var lastName: String? = null,
    var number: String? = null,
) {
    override fun toString(): String {
        return "Contact(firstName=$firstName, lastName=$lastName, number=$number)"
    }

}
