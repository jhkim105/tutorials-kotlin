package jhkim105.tutorials.enum_mapping

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class EnumMappingController {

    @GetMapping("/enum-mapping")
    fun enumParam(idp: Idp):String {
        return idp.name
    }

}