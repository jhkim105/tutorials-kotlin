package jhkim105.tutorials.kotlin.spring_security

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * @author Eleftheria Stein
 */
@Controller
class MainController {

    @GetMapping("/")
    fun home(): String {
        return "index"
    }

    @GetMapping("/user/index")
    fun user(): String {
        return "user/index"
    }

    @GetMapping("/admin/index")
    fun admin(): String {
        return "admin/index"
    }

}