package jhkim105.cache.sample

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/samples")
class SampleController(
    private val sampleService: SampleService,
) {

    @GetMapping("/{id}")
    fun getSample(@PathVariable id: Long): SamplePayload = sampleService.getSample(id)

    @GetMapping
    fun getSamples(
        @RequestParam(defaultValue = "general") category: String,
        @RequestParam(defaultValue = "3") limit: Int,
    ): List<SamplePayload> = sampleService.getSamples(category, limit)
}
