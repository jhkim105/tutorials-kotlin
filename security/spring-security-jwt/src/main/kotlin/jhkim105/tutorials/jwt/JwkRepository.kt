package jhkim105.tutorials.jwt

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface JwkRepository : JpaRepository<Jwk, String> {
    fun findTopByOrderByCreatedAtDesc(): Optional<Jwk>
}
