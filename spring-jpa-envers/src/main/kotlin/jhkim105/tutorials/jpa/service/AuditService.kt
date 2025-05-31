package jhkim105.tutorials.jpa.service

import jakarta.persistence.EntityManager
import jhkim105.tutorials.jpa.model.Group
import jhkim105.tutorials.jpa.model.User
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.query.AuditEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class AuditService(
    private val entityManager: EntityManager
) {

    @Transactional(readOnly = true)
    fun getGroupRevisions(groupId: Long): List<Group> {
        val reader = AuditReaderFactory.get(entityManager)
        val resultList =  reader.createQuery()
            .forRevisionsOfEntity(Group::class.java, true, true)
            .add(AuditEntity.id().eq(groupId))
        .resultList as List<Group>

        resultList.forEach {
            it.users.size // prevent lazy exception
        }
        return resultList
    }

    @Transactional(readOnly = true)
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