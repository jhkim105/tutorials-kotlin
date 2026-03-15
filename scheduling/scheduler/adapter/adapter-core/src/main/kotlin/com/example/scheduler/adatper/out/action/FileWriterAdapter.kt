package com.example.scheduler.adatper.out.action

import com.example.scheduler.application.port.FileWriterPort
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

@Component
class FileWriterAdapter : FileWriterPort {
    override fun writeFile(directory: String, fileName: String, content: String) {
        val dirPath = Paths.get(directory)
        Files.createDirectories(dirPath)
        Files.writeString(dirPath.resolve(fileName), content)
    }
}
