package jhkim105.tutorials.webflux

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/samples")
class SampleController {

    @GetMapping
    fun sample(): Mono<String> {
        return Mono.just("Hello, world!")
    }

}