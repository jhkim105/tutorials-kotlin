package jhkim105.springkafkadynamic

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// KafkaAdminController.kt
@RestController
@RequestMapping("/api/kafka")
class KafkaAdminController(
    private val listenerControl: KafkaListenerControlService,
    private val dynamicManager: DynamicKafkaListenerContainerService
) {
    data class ConcurrencyReq(val concurrency: Int)
    data class DynamicCreateReq(
        val id: String,
        val topics: List<String>,
        val concurrency: Int = 1
    )


    // --- @KafkaListener 컨테이너 제어 ---
    @PostMapping("/listeners/{id}/start")
    fun start(@PathVariable id: String) = ApiResult.success<Unit>().also { listenerControl.start(id) }

    @PostMapping("/listeners/{id}/stop")
    fun stop(@PathVariable id: String) = ApiResult.success<Unit>().also { listenerControl.stop(id) }

    @PostMapping("/listeners/{id}/pause")
    fun pause(@PathVariable id: String) = ApiResult.success<Unit>().also { listenerControl.pause(id) }

    @PostMapping("/listeners/{id}/resume")
    fun resume(@PathVariable id: String) = ApiResult.success<Unit>().also { listenerControl.resume(id) }

    @PostMapping("/listeners/{id}/concurrency")
    fun changeConcurrency(@PathVariable id: String, @RequestBody req: ConcurrencyReq) =
        ApiResult.success<Unit>().also { listenerControl.updateConcurrency(id, req.concurrency) }

    // --- 동적 컨테이너 제어 ---
    @PostMapping("/dynamic")
    fun createDynamic(@RequestBody req: DynamicCreateReq): ApiResult<String> {
        dynamicManager.createAndStart(
            id = req.id,
            topics = req.topics,
            concurrency = req.concurrency
        ) { record ->
            // 여기서 동적 컨테이너 메시지 처리 로직
            println("[dynamic:${req.id}] ${record.topic()} offset=${record.offset()} value=${record.value()}")
        }
        return ApiResult.success(data = req.id)
    }

    @DeleteMapping("/dynamic/{id}")
    fun removeDynamic(@PathVariable id: String) =
        ApiResult.success<Unit>().also { dynamicManager.stopAndRemove(id) }

    @PostMapping("/dynamic/{id}/pause")
    fun pauseDynamic(@PathVariable id: String) =
        ApiResult.success<Unit>().also { dynamicManager.pause(id) }

    @PostMapping("/dynamic/{id}/resume")
    fun resumeDynamic(@PathVariable id: String) =
        ApiResult.success<Unit>().also { dynamicManager.resume(id) }

    @PostMapping("/dynamic/{id}/concurrency")
    fun changeDynamicConcurrency(@PathVariable id: String, @RequestBody req: ConcurrencyReq) =
        ApiResult.success<Unit>().also { dynamicManager.updateConcurrency(id, req.concurrency) }
}

