package jhkim105.tutorials.jpa.service

import jakarta.persistence.EntityManager
import jhkim105.tutorials.jpa.model.Group
import jhkim105.tutorials.jpa.repository.GroupRepository
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.query.AuditEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GroupService(
    private val groupRepository: GroupRepository,
    private val entityManager: EntityManager
) {

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

}