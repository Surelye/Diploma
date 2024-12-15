package sgu.borodin.nas.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sgu.borodin.nas.dto.FileMetadata;
import sgu.borodin.nas.service.FileOperationsService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileOperationsController {
    private final FileOperationsService fileOperationsService;

    @GetMapping("/download/{path:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String path) throws IOException {
        Resource resource = fileOperationsService.download(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(Path.of(path)))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping("/list/{path:.+}")
    public ResponseEntity<List<FileMetadata>> listFiles(
            @PathVariable String path,
            @RequestParam Map<String, String> requestParams
    ) {
        return ResponseEntity.ok(fileOperationsService.list(path, requestParams));
    }

    @PostMapping("/upload")
    public ResponseEntity<URI> uploadFile(@RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.created(fileOperationsService.upload(file)).build();
    }

    @PostMapping("/move")
    public ResponseEntity<URI> moveFile(@RequestParam String sourcePath, @RequestParam String destinationPath) {
        return ResponseEntity.created(fileOperationsService.move(sourcePath, destinationPath)).build();
    }

    @PostMapping("/copy")
    public ResponseEntity<URI> copyFile(@RequestParam String sourcePath, @RequestParam String destinationPath) {
        return ResponseEntity.created(fileOperationsService.copy(sourcePath, destinationPath)).build();
    }

    @DeleteMapping("/delete/{path:.+}")
    public ResponseEntity<Void> deleteFileOrDirectory(@PathVariable String path) throws IOException {
        fileOperationsService.delete(path);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/metadata/{path:.+}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> getMetadata(@PathVariable String path) throws IOException {
        HttpHeaders headers = fileOperationsService.getMetadata(path);
        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }
}
