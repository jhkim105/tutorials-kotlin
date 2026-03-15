package jhkim105.tutorials.webflux.file

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.nio.file.Paths


@RestController
@RequestMapping("/files")
class FileUploadController {

    private val log = LoggerFactory.getLogger(FileUploadController::class.java)


    @PostMapping
    fun upload(@RequestPart("file") filePart: Mono<FilePart>): Mono<String> {
        return filePart.flatMap { file ->
            val destination = Paths.get(file.filename())
            log.info("size: ${file.headers().contentLength}")
            file.transferTo(destination).thenReturn("OK")
        }.onErrorResume { e ->
            log.error("Upload error", e)
            filePart.flatMap { file ->
                val message = "file upload failed for file: ${file.filename()} "
                Mono.error(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message)) }
            }
    }
}