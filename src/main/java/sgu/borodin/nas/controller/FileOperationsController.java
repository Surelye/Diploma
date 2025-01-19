package sgu.borodin.nas.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sgu.borodin.nas.dto.CurrentUser;
import sgu.borodin.nas.dto.FileMetadata;
import sgu.borodin.nas.service.FileOperationsService;
import sgu.borodin.nas.util.ZipManager.ZipFile;

import java.io.IOException;
import java.net.URI;
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
    public ResponseEntity<?> downloadElements(@PathVariable String path, @RequestParam List<String> filePaths) throws IOException {
        log.info("Downloading files [{}] at path [{}] for user [{}]", filePaths, path, currentUser.getUsername());
        ZipFile zipFile = fileOperationsService.download(path, filePaths);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, zipFile.path())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipFile.content().length))
                .body(zipFile.content());
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
    public ResponseEntity<List<URI>> uploadFiles(
            @PathVariable(required = false) String path,
            @RequestParam List<MultipartFile> files
    ) throws IOException {
        log.info("Uploading files [{}] on directory [{}] for user [{}]",
                files.stream().map(MultipartFile::getOriginalFilename).toList(), path, currentUser.getUsername()
        );
        return ResponseEntity.created(fileOperationsService.upload(path, files)).build();
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

    @PostMapping("/folder/{*path}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createFolder(
            @PathVariable(required = false) String path,
            @RequestParam String folderName
    ) throws IOException {
        log.info("Creating a folder [{}] for user [{}] at [{}]", folderName, currentUser.getUsername(), path);
        fileOperationsService.createFolder(path, folderName);
    }

    @DeleteMapping("/delete/{*path}")
    public ResponseEntity<Void> deleteElements(@PathVariable String path, @RequestBody List<String> elements)
            throws IOException {
        log.info("Deleting resources [{}] at location [{}] for user [{}]", elements, path, currentUser.getUsername());
        fileOperationsService.delete(path, elements);
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
