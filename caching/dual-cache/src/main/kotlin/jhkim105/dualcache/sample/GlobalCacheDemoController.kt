package jhkim105.dualcache.sample

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cache/global")
class GlobalCacheDemoController(
	private val globalCacheDemoService: GlobalCacheDemoService
) {

	@GetMapping("/{id}")
	fun loadSnapshot(@PathVariable id: String) = mapOf(
		"type" to "global",
		"id" to id,
		"value" to globalCacheDemoService.loadSnapshot(id)
	)

	@GetMapping("/{id}/list")
	fun loadSnapshotList(@PathVariable id: String) = mapOf(
		"type" to "global",
		"id" to id,
		"values" to globalCacheDemoService.loadSnapshotList(id)
	)
}
