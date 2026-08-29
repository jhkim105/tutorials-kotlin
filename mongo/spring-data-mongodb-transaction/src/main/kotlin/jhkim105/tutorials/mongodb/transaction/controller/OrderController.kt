package jhkim105.tutorials.mongodb.transaction.controller

import jhkim105.tutorials.mongodb.transaction.service.OrderService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    fun createOrder(
            @RequestParam name: String,
            @RequestParam(defaultValue = "false") fail: Boolean
    ) {
        orderService.createOrder(name, fail)
    }
}
