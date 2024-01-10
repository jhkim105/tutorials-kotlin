package jhkim105.tutorials.kotlin.spring_security

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api")
class ApiController {


  @GetMapping
  fun get(): ResponseEntity<ApiResponse> = ResponseEntity.ok(ApiResponse("this is api response."))
}

data class ApiResponse(val message: String)