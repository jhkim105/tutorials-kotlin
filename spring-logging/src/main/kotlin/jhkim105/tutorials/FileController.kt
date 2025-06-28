package jhkim105.tutorials

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths

@RestController
@RequestMapping("/files")
class FileController {
    @Value("\${file.upload-dir}")
    private lateinit var uploadDir: String
    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var uploadPath: Path

    @PostConstruct
    fun init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize()
        File(uploadPath.toUri()).mkdirs()
    }

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        if (file.isEmpty) return ResponseEntity.badRequest().body("빈 파일입니다.")

        val originalFilename = StringUtils.cleanPath(file.originalFilename ?: "uploaded.dat")
        val targetFile = uploadPath.resolve(originalFilename).toFile()

        return try {
            file.transferTo(targetFile)
            log.info("파일 업로드 성공: ${targetFile.absolutePath}")
            ResponseEntity.ok("업로드 완료: ${targetFile.name}")
        } catch (ex: IOException) {
            log.error("파일 업로드 실패", ex)
            ResponseEntity.internalServerError().body("업로드 실패")
        }
    }

    @GetMapping("/download/{filename}")
    fun downloadFile(@PathVariable filename: String, response: HttpServletResponse): ResponseEntity<Resource> {
        val file = uploadPath.resolve(filename).toFile()
        if (!file.exists()) {
            return ResponseEntity.notFound().build()
        }

        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")
        val resource: Resource = FileSystemResource(file)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$encodedFilename\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }
}