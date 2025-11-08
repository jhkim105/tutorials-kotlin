package jhkim105.dualcache.sample

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cache/local")
class LocalCacheDemoController(
	private val localCacheDemoService: LocalCacheDemoService
) {

	@GetMapping("/{id}")
	fun loadSnapshot(@PathVariable id: String) = mapOf(
		"type" to "local",
		"id" to id,
		"value" to localCacheDemoService.loadSnapshot(id)
	)

	@GetMapping("/{id}/list")
	fun loadSnapshotList(@PathVariable id: String) = mapOf(
		"type" to "local",
		"id" to id,
		"values" to localCacheDemoService.loadSnapshotList(id)
	)
}
