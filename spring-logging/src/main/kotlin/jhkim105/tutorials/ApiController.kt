package jhkim105.tutorials

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@RestController
@RequestMapping("/api/items")
class ApiController {

    private val items = ConcurrentHashMap<Long, Item>()
    private val idGenerator = AtomicLong(1)

    @PostMapping
    fun createItem(@RequestBody request: CreateItemRequest): ResponseEntity<Item> {
        val id = idGenerator.getAndIncrement()
        val item = Item(id, request.name, request.description)
        items[id] = item
        return ResponseEntity.ok(item)
    }

    @GetMapping("/{id}")
    fun getItem(@PathVariable id: Long): ResponseEntity<Item> {
        return items[id]?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}")
    fun updateItem(@PathVariable id: Long, @RequestBody request: UpdateItemRequest): ResponseEntity<Item> {
        val existing = items[id] ?: return ResponseEntity.notFound().build()
        val updated = existing.copy(name = request.name, description = request.description)
        items[id] = updated
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun deleteItem(@PathVariable id: Long): ResponseEntity<Void> {
        return if (items.remove(id) != null) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }

    @GetMapping
    fun listItems(): ResponseEntity<List<Item>> {
        return ResponseEntity.ok(items.values.toList())
    }
}

data class Item(
    val id: Long,
    val name: String,
    val description: String
)

data class CreateItemRequest(
    val name: String,
    val description: String
)

data class UpdateItemRequest(
    val name: String,
    val description: String
)