package sgu.borodin.nas.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sgu.borodin.nas.dto.CurrentUser;
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
@Secured({"ADMIN", "USER"})
@Slf4j
@RequiredArgsConstructor
public class FileOperationsController {
    private final FileOperationsService fileOperationsService;
    private final CurrentUser currentUser;

    @GetMapping("/download/{*path}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String path) throws IOException {
        log.info("Downloading file [{}] for user [{}]", path, currentUser.getUsername());
        Resource resource = fileOperationsService.download(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(Path.of(path)))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping(value = "/list/{*path}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileMetadata>> listFiles(
            @PathVariable(required = false) String path,
            @RequestParam(required = false) Map<String, String> requestParams
    ) throws IOException {
        log.info("Listing files {} for user [{}] with request params [{}]",
                path.isBlank() ? "in default directory" : "on path [%s]".formatted(path),
                currentUser.getUsername(),
                requestParams
        );
        return ResponseEntity.ok(fileOperationsService.list(path, requestParams));
    }

    @PostMapping("/upload/{*path}")
    public ResponseEntity<URI> uploadFile(
            @PathVariable(required = false) String path,
            @RequestParam MultipartFile file
    ) throws IOException {
        log.info("Uploading file [{}] on {} for user [{}]",
                file.getOriginalFilename(),
                path.isBlank() ? "default directory" : "directory [%s]".formatted(path),
                currentUser.getUsername()
        );
        return ResponseEntity.created(fileOperationsService.upload(file, path)).build();
    }

    @PostMapping("/move")
    public ResponseEntity<URI> moveFile(@RequestParam String sourcePath, @RequestParam String destinationPath)
            throws IOException {
        log.info("Moving resource from source [{}] to destination [{}] for user [{}]",
                sourcePath, destinationPath, currentUser.getUsername());
        return ResponseEntity.created(fileOperationsService.move(sourcePath, destinationPath)).build();
    }

    @PostMapping("/copy")
    public ResponseEntity<URI> copyFile(@RequestParam String sourcePath, @RequestParam String destinationPath)
            throws IOException {
        log.info("Copying resource from source [{}] to destination [{}] for user [{}]",
                sourcePath, destinationPath, currentUser.getUsername());
        return ResponseEntity.created(fileOperationsService.copy(sourcePath, destinationPath)).build();
    }

    @DeleteMapping("/delete/{*path}")
    public ResponseEntity<Void> deleteFileOrDirectory(@PathVariable String path) throws IOException {
        log.info("Deleting resource [{}] for user [{}]", path, currentUser.getUsername());
        fileOperationsService.delete(path);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/metadata/{*path}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> getMetadata(@PathVariable String path) throws IOException {
        HttpHeaders headers = fileOperationsService.getMetadata(path);
        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }
}
