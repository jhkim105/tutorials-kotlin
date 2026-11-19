package jhkim105.ssecompare.webfluxflow

import jhkim105.ssecompare.common.ChatStreamContractSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["sse.compare.heartbeat-interval-ms=100"])
class WebFluxFlowContractTest : ChatStreamContractSpec()
