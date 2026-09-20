package jhkim105.tutorials.routing.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import jhkim105.tutorials.routing.domain.Member
import jhkim105.tutorials.routing.domain.MemberRepository
import org.hibernate.Session
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

data class ConnectionInfo(
    val transactionType: String,
    val databaseUrl: String,
    val catalog: String?,
    val isReadOnly: Boolean,
)

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val entityManager: EntityManager,
) {

    @Transactional
    fun createMember(username: String, email: String): Pair<Member, ConnectionInfo> {
        val info = extractConnectionInfo("WRITE (createMember)")
        logger.info { ">>> [MemberService.createMember] Saving member '$username' on DB: ${info.databaseUrl}" }
        val member = memberRepository.save(Member(username = username, email = email))
        return member to info
    }

    @Transactional
    fun updateMemberEmail(id: Long, newEmail: String): Pair<Member, ConnectionInfo> {
        val info = extractConnectionInfo("WRITE (updateMemberEmail)")
        logger.info { ">>> [MemberService.updateMemberEmail] Updating member id=$id on DB: ${info.databaseUrl}" }
        val member = memberRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Member not found with id=$id")
        member.email = newEmail
        return memberRepository.save(member) to info
    }

    @Transactional(readOnly = true)
    fun getMember(id: Long): Pair<Member?, ConnectionInfo> {
        val info = extractConnectionInfo("READ-ONLY (getMember)")
        logger.info { ">>> [MemberService.getMember] Querying member id=$id on DB: ${info.databaseUrl}" }
        val member = memberRepository.findByIdOrNull(id)
        return member to info
    }

    @Transactional(readOnly = true)
    fun getAllMembers(): Pair<List<Member>, ConnectionInfo> {
        val info = extractConnectionInfo("READ-ONLY (getAllMembers)")
        logger.info { ">>> [MemberService.getAllMembers] Querying all members on DB: ${info.databaseUrl}" }
        val members = memberRepository.findAll()
        return members to info
    }

    @Transactional(readOnly = true)
    fun getReadOnlyConnectionInfo(): ConnectionInfo {
        return extractConnectionInfo("READ-ONLY (Check)")
    }

    @Transactional
    fun getWriteConnectionInfo(): ConnectionInfo {
        return extractConnectionInfo("WRITE (Check)")
    }

    private fun extractConnectionInfo(operationName: String): ConnectionInfo {
        val session = entityManager.unwrap(Session::class.java)
        var url = ""
        var catalog: String? = null
        var isReadOnly = false

        session.doWork { connection ->
            url = connection.metaData.url
            catalog = connection.catalog
            isReadOnly = connection.isReadOnly
        }
        val info = ConnectionInfo(
            transactionType = operationName,
            databaseUrl = url,
            catalog = catalog,
            isReadOnly = isReadOnly,
        )
        logger.info { ">>> [Connection Info] Operation: $operationName | URL: $url | Catalog: $catalog | ReadOnly: $isReadOnly" }
        return info
    }
}
