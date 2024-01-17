package jhkim105.tutorials.kotlin.spring_web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/article")
class ArticleController(private val repository: ArticleRepository) {

  private val log = LoggerFactory.getLogger(javaClass)
  @GetMapping("/")
  fun findAll() {
    log.info("findAll")
    repository.findAllByOrderByAddedAtDesc()
  }

  @GetMapping("/{slug}")
  fun findOne(@PathVariable slug: String) =
    repository.findBySlug(slug) ?: throw ResponseStatusException(NOT_FOUND, "This article does not exist")

}

@RestController
@RequestMapping("/api/user")
class UserController(private val repository: UserRepository) {

  @GetMapping("/")
  fun findAll(): MutableList<User> = repository.findAll()

  @GetMapping("/{login}")
  fun findOne(@PathVariable login: String) = repository.findByLogin(login) ?: throw ResponseStatusException(NOT_FOUND, "This user does not exist")
}