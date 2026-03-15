package jhkim105.springretry

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired

@SpringBootTest
class RetryTest(
    @Autowired private val annClient: FlakyRemoteClientAnnotation,
    @Autowired private val tplClient: FlakyRemoteClientTemplate
) : StringSpec({

    "annotation: 성공 시나리오(2번 실패 후 성공)" {
        annClient.presetFailures(2)
        val res = annClient.call(Request("A1"))
        res.message shouldBe "OK via annotation for A1"
    }

    "annotation: 모두 실패하면 recover" {
        annClient.presetFailures(10)
        val res = annClient.call(Request("A2"))
        res.message shouldBe "FALLBACK via annotation for A2"
    }

    "template: 성공 시나리오(2번 실패 후 성공)" {
        tplClient.presetFailures(2)
        val res = tplClient.call(Request("T1"))
        res.message shouldBe "OK via template for T1"
    }

    "template: 모두 실패하면 recovery" {
        tplClient.presetFailures(10)
        val res = tplClient.call(Request("T2"))
        res.message shouldBe "FALLBACK via template for T2"
    }
})
