package jhkim105.tutorials.routing.controller

import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.routing.domain.Member
import jhkim105.tutorials.routing.service.ConnectionInfo
import jhkim105.tutorials.routing.service.MemberService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

data class CreateMemberRequest(
    val username: String,
    val email: String,
)

data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String,
    val data: T? = null,
    val connectionInfo: ConnectionInfo? = null,
)

data class PoolStatus(
    val poolName: String,
    val activeConnections: Int,
    val idleConnections: Int,
    val totalConnections: Int,
    val threadsAwaitingConnection: Int,
)

@RestController
@RequestMapping("/api")
class RoutingDemoController(
    private val memberService: MemberService,
    @Qualifier("masterDataSource") private val masterDataSource: HikariDataSource,
    @Qualifier("slaveDataSources") private val slaveDataSources: Map<String, HikariDataSource>,
) {

    @PostMapping("/members")
    fun createMember(@RequestBody request: CreateMemberRequest): ResponseEntity<ApiResponse<Member>> {
        logger.info { "HTTP POST /api/members called with username=${request.username}" }
        val (member, connectionInfo) = memberService.createMember(request.username, request.email)
        return ResponseEntity.ok(
            ApiResponse(
                message = "Member created successfully on MASTER DataSource",
                data = member,
                connectionInfo = connectionInfo,
            )
        )
    }

    @GetMapping("/members/{id}")
    fun getMember(@PathVariable id: Long): ResponseEntity<ApiResponse<Member>> {
        logger.info { "HTTP GET /api/members/$id called" }
        val (member, connectionInfo) = memberService.getMember(id)
        return if (member != null) {
            ResponseEntity.ok(
                ApiResponse(
                    message = "Member retrieved from SLAVE DataSource",
                    data = member,
                    connectionInfo = connectionInfo,
                )
            )
        } else {
            ResponseEntity.ok(
                ApiResponse(
                    success = false,
                    message = "Member not found on SLAVE DataSource (Note: Data might only exist on Master if replication is not configured)",
                    data = null,
                    connectionInfo = connectionInfo,
                )
            )
        }
    }

    @GetMapping("/members")
    fun getAllMembers(): ResponseEntity<ApiResponse<List<Member>>> {
        logger.info { "HTTP GET /api/members called" }
        val (members, connectionInfo) = memberService.getAllMembers()
        return ResponseEntity.ok(
            ApiResponse(
                message = "Members list retrieved from SLAVE DataSource",
                data = members,
                connectionInfo = connectionInfo,
            )
        )
    }

    @GetMapping("/routing/write-check")
    fun checkWriteRouting(): ResponseEntity<ApiResponse<Unit>> {
        logger.info { "HTTP GET /api/routing/write-check called" }
        val info = memberService.getWriteConnectionInfo()
        return ResponseEntity.ok(
            ApiResponse(
                message = "Write routing verified. Routed to MASTER DataSource.",
                connectionInfo = info,
            )
        )
    }

    @GetMapping("/routing/read-check")
    fun checkReadRouting(): ResponseEntity<ApiResponse<Unit>> {
        logger.info { "HTTP GET /api/routing/read-check called" }
        val info = memberService.getReadOnlyConnectionInfo()
        return ResponseEntity.ok(
            ApiResponse(
                message = "Read-only routing verified. Routed to SLAVE DataSource (Round-Robin).",
                connectionInfo = info,
            )
        )
    }

    @GetMapping("/routing/pool-status")
    fun getPoolStatus(): ResponseEntity<Map<String, PoolStatus>> {
        val statuses = mutableMapOf<String, PoolStatus>()

        masterDataSource.hikariPoolMXBean?.let { mxBean ->
            statuses["master"] = PoolStatus(
                poolName = masterDataSource.poolName,
                activeConnections = mxBean.activeConnections,
                idleConnections = mxBean.idleConnections,
                totalConnections = mxBean.totalConnections,
                threadsAwaitingConnection = mxBean.threadsAwaitingConnection,
            )
        }

        slaveDataSources.forEach { (key, ds) ->
            ds.hikariPoolMXBean?.let { mxBean ->
                statuses[key] = PoolStatus(
                    poolName = ds.poolName,
                    activeConnections = mxBean.activeConnections,
                    idleConnections = mxBean.idleConnections,
                    totalConnections = mxBean.totalConnections,
                    threadsAwaitingConnection = mxBean.threadsAwaitingConnection,
                )
            }
        }

        return ResponseEntity.ok(statuses)
    }
}
