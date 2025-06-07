package jhkim105.tutorials.jpa.service

import jakarta.persistence.EntityManager
import jhkim105.tutorials.jpa.model.User
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.query.AuditEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val entityManager: EntityManager,
) {

    fun getUserRevisions(userId: Long): List<User> {
        val reader = AuditReaderFactory.get(entityManager)
        val resultList =  reader.createQuery()
            .forRevisionsOfEntity(User::class.java, true, true)
            .add(AuditEntity.id().eq(userId))
            .resultList as List<User>

        resultList.forEach {
            it.group?.name // prevent lazy exception
        }
        return resultList
    }
}