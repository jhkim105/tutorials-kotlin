package jhkim105.tutorials.beanio.fixedlength

import org.beanio.annotation.Field

@org.beanio.annotation.Record(minOccurs = 0, maxOccurs = -1)
class AnnotatedContact(
    @Field(name = "firstName", length = 20, padding ='-'.code)
    var firstName: String? = null,

    @Field(name = "lastName", length = 30)
    var lastName: String? = null,

    @Field(name = "number", length = 11)
    var number: String? = null,
) {
    override fun toString(): String {
        return "AnnotatedContact(firstName=$firstName, lastName=$lastName, number=$number)"
    }

}


@org.beanio.annotation.Record
class Contacts (
    @org.beanio.annotation.Segment(minOccurs = 0, maxOccurs = -1, collection = List::class)
    var contactList: MutableList<AnnotatedContact> = mutableListOf()
) {
    override fun toString(): String {
        return "Contacts(contactList=$contactList)"
    }

}