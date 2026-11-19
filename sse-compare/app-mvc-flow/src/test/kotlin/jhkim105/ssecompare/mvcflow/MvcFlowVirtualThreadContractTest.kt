package jhkim105.ssecompare.mvcflow

import jhkim105.ssecompare.common.ChatStreamContractSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("vt")
@TestPropertySource(properties = ["sse.compare.heartbeat-interval-ms=100"])
class MvcFlowVirtualThreadContractTest : ChatStreamContractSpec()
