package jhkim105.tutorials.testcoverage

import org.springframework.stereotype.Service

@Service
class HelloService {
    fun sayHello(name: String): String {
        return "Hello, $name"
    }

    fun add(a: Int, b: Int): Int {
        return a + b
    }
}
