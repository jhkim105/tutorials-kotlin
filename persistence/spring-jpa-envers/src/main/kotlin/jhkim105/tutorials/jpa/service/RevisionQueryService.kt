package jhkim105.tutorials.jpa.service

import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.query.AuditEntity
import org.hibernate.envers.query.AuditQuery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class RevisionQueryService(
    private val entityManager: EntityManager
) {

    fun <T : Any> getRevisions(clazz: Class<T>, id: Any,  maxResults: Int? = null): List<T> {
        val query: AuditQuery = AuditReaderFactory.get(entityManager)
            .createQuery()
            .forRevisionsOfEntity(clazz, true, true)
            .add(AuditEntity.id().eq(id))
            .addOrder(AuditEntity.revisionProperty("timestamp").desc())
        maxResults?.let { query.setMaxResults(it) }
        @Suppress("UNCHECKED_CAST")
        return query.resultList as List<T>
    }

    fun getLatestRevisionNumber(clazz: Class<*>, entityId: Any): Long {
        val result = AuditReaderFactory.get(entityManager).createQuery()
            .forRevisionsOfEntity(clazz, true)
            .addProjection(AuditEntity.revisionNumber().max())
            .add(AuditEntity.id().eq(entityId))
            .singleResult

        return (result as Number).toLong()
    }

    fun <T : Any> get(clazz: Class<T>, rev: Long): T? {
        val query = AuditReaderFactory.get(entityManager)
            .createQuery()
            .forEntitiesAtRevision(clazz, rev)
            .setMaxResults(1)

        @Suppress("UNCHECKED_CAST")
        val resultList = query.resultList as List<T>
        return resultList.firstOrNull()
    }

    fun isModified(clazz: Class<*>, id: Any, columnName: String, rev: Long): Boolean {
        val query = AuditReaderFactory.get(entityManager).createQuery()
            .forEntitiesAtRevision(clazz, rev)
            .add(AuditEntity.id().eq(id))
            .add(AuditEntity.property(columnName).hasChanged())
            .addProjection(AuditEntity.id())

        return try {
            query.singleResult
            true
        } catch (e: NoResultException) {
            false
        }
    }
}
