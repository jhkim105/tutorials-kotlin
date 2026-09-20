package jhkim105.tutorials.routing

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import jhkim105.tutorials.routing.service.MemberService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class DataSourceRoutingTest(
    private val memberService: MemberService,
) : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("Master-Slave 멀티 데이터소스 환경에서") {

            When("쓰기 트랜잭션(@Transactional)을 실행하면") {
                val info = memberService.getWriteConnectionInfo()

                Then("Master 데이터베이스(master_db)의 Connection URL로 라우팅된다") {
                    info.databaseUrl shouldContain "master_db"
                    info.isReadOnly shouldBe false
                }
            }

            When("읽기 전용 트랜잭션(@Transactional(readOnly = true))을 연속으로 실행하면") {
                val info1 = memberService.getReadOnlyConnectionInfo()
                val info2 = memberService.getReadOnlyConnectionInfo()
                val info3 = memberService.getReadOnlyConnectionInfo()

                Then("Slave1(slave1_db)과 Slave2(slave2_db)로 Round-Robin 방식으로 번갈아가며 라우팅된다") {
                    val urls = listOf(info1.databaseUrl, info2.databaseUrl, info3.databaseUrl)
                    (urls[0].contains("slave1_db") || urls[0].contains("slave2_db")) shouldBe true
                    (urls[1].contains("slave1_db") || urls[1].contains("slave2_db")) shouldBe true

                    // 두 연속 호출의 대상 Slave DB가 서로 다름 (slave1_db -> slave2_db 또는 slave2_db -> slave1_db)
                    (urls[0] != urls[1]) shouldBe true
                    // 3번째 호출은 1번째와 동일한 Slave 데이터소스로 순환
                    urls[0] shouldBe urls[2]
                }
            }

            When("Master에 엔티티를 저장하고 조회할 때") {
                val (savedMember, writeInfo) = memberService.createMember("alice", "alice@example.com")

                Then("Master에서 생성된 엔티티 ID가 정상 발급되고 Master DB URL이 반환된다") {
                    savedMember.id shouldBe 1L
                    savedMember.username shouldBe "alice"
                    writeInfo.databaseUrl shouldContain "master_db"
                }

                Then("별도 복제가 없는 Slave에서 조회 시 Master/Slave가 분리된 인스턴스임을 확인할 수 있다") {
                    val (memberInSlave, readInfo) = memberService.getMember(savedMember.id!!)
                    memberInSlave shouldBe null
                    (readInfo.databaseUrl.contains("slave1_db") || readInfo.databaseUrl.contains("slave2_db")) shouldBe true
                }
            }
        }
    }
}
