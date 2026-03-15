package jhkim105.files

import org.springframework.http.HttpMethod
import org.springframework.util.StreamUtils
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

object Downloader {
    fun downloadFileFromUrl(urlString: String, targetDir: String = "."): File {
        val url = URL(urlString)

        // URL 경로에서 파일명 추출
        val fileName = url.path.substringAfterLast("/").ifBlank {
            // 경로 끝에 파일명이 없으면 기본 이름 사용
            "downloaded_file"
        }

        val targetDirectory = File(targetDir)
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs() // 디렉토리 없으면 생성
        }

        val targetFile = File(targetDirectory, fileName)
        downloadWithNIOFiles(urlString, targetFile.absolutePath)

        return targetFile
    }

    fun downloadWithRestTemplate(url: String, dirPath: String): File {
        val restTemplate = RestTemplate()
        return restTemplate.execute(URI(url), HttpMethod.GET, null) { response ->
            var fileName = response.headers.contentDisposition?.filename
            if (fileName.isNullOrBlank()) {
                fileName = UUID.randomUUID().toString()
            }
            val targetFile = File(dirPath, fileName)
            response.body?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    StreamUtils.copy(inputStream, outputStream)
                }
            }
            targetFile
        }!!
    }
    fun downloadByRestClient(url: String, dirPath: String): File? {
        val restClient = RestClient.create()

        return restClient.method(HttpMethod.GET)
            .uri(url)
            .exchange { request, response ->
                val headers = response.headers
                var fileName = headers.contentDisposition?.filename
                if (fileName.isNullOrBlank()) {
                    // URL 마지막 path 에서 파일명 추출 시도, 없으면 UUID 사용
                    val pathFileName = url.substringAfterLast("/").takeIf { it.isNotBlank() }
                    fileName = pathFileName ?: UUID.randomUUID().toString()
                }

                val targetFile = File(dirPath, fileName)
                response.body?.use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        StreamUtils.copy(inputStream, outputStream)
                    }
                }
                targetFile
            }
    }

    fun downloadWithJavaIO(urlStr: String, localFilename: String) {
        try {
            BufferedInputStream(URL(urlStr).openStream()).use { input ->
                FileOutputStream(localFilename).use { output ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    fun downloadWithNIOFiles(fileUrl: String, filePath: String) {
        try {
            URL(fileUrl).openStream().use { input ->
                Files.copy(input, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun downloadWithNIOChannel(fileUrl: String, filePath: String) {
        try {
            val url = URL(fileUrl)
            url.openStream().use { input ->
                Channels.newChannel(input).use { channel: ReadableByteChannel ->
                    FileOutputStream(filePath).use { output ->
                        val fileChannel: FileChannel = output.channel
                        fileChannel.transferFrom(channel, 0, Long.MAX_VALUE)
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
