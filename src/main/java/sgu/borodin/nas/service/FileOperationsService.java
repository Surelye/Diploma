package sgu.borodin.nas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sgu.borodin.nas.dto.CurrentUser;
import sgu.borodin.nas.dto.FileMetadata;
import sgu.borodin.nas.dto.Filter;
import sgu.borodin.nas.enums.Operation;
import sgu.borodin.nas.enums.Order;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileOperationsService {
    public static final String UPLOAD_DIR_TEMPLATE = System.getProperty("os.name").toLowerCase().contains("win")
            ? "C:\\Users\\Artem_Borodin\\Pictures\\uploads\\%s"
            : "/home/%s";

    private final CurrentUser currentUser;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    public Resource download(String filename) {
        if (filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename should not be empty");
        }

        File file = Path.of(currentUser.getUploadDirectory(), filename).toFile();

        if (!file.exists() || !file.isFile()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File [" + filename + "] not found");
        }

        return new FileSystemResource(file);
    }

    public List<FileMetadata> list(String path, Map<String, String> requestParams) throws IOException {
        Path directoryPath = Path.of(currentUser.getUploadDirectory(), path);

        if (!Files.exists(directoryPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Directory [" + path + "] not found");
        }

        if (!Files.isDirectory(directoryPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Resource on path [" + directoryPath + "] is not a directory");
        }

        return getFilesMetadata(directoryPath, requestParams);
    }

    public URI upload(MultipartFile file, String path) throws IOException {
        Path uploadDir = Path.of(currentUser.getUploadDirectory(), path);

        if (!Files.exists(uploadDir)) {
            Files.createDirectory(uploadDir);
        }

        validateFile(file);
        Path filePath = Paths.get(uploadDir.toString(), file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File [{}] was successfully uploaded in {} for user [{}]",
                file.getOriginalFilename(),
                Objects.isNull(path) ? "default directory" : "directory [%s]".formatted(path),
                currentUser.getUsername()
        );

        return filePath.toUri();
    }

    public URI move(String sourcePath, String destinationPath) throws IOException {
        return performMoveOrCopyOperation(sourcePath, destinationPath, Operation.MOVE);
    }

    public URI copy(String sourcePath, String destinationPath) throws IOException {
        return performMoveOrCopyOperation(sourcePath, destinationPath, Operation.COPY);
    }

    public void delete(String path) throws IOException {
        Path filePath = Path.of(currentUser.getUploadDirectory(), path);

        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File or directory " + path + " not found");
        }

        if (Files.isDirectory(filePath)) {
            deleteDirectoryRecursively(filePath);
            log.info("Deleted directory [{}]", path);
        } else {
            Files.delete(filePath);
            log.info("Deleted file [{}]", path);
        }
    }

    public HttpHeaders getMetadata(String path) throws IOException {
        File file = Paths.get(currentUser.getUploadDirectory(), path).toFile();

        if (!file.exists() || file.isDirectory()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File " + path + " not found");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(file.toPath()));

        return headers;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalStateException("File " + file.getOriginalFilename() + " is empty");
        }

        if (file.getSize() > maxFileSize.toBytes()) {
            throw new IllegalArgumentException("File " + file.getOriginalFilename() + " size exceeds the limit");
        }
    }

    private List<FileMetadata> getFilesMetadata(Path directoryPath, Map<String, String> requestParams) throws IOException {
        try (Stream<Path> paths = Files.list(directoryPath)) {
            Stream<FileMetadata> fileMetadataStream = paths.map(FileMetadata::new);

            if (requestParams != null && !requestParams.isEmpty()) {
                for (Map.Entry<String, String> filterData : requestParams.entrySet()) {
                    Predicate<FileMetadata> predicate = Filter.getPredicate(filterData.getKey(), filterData.getValue());
                    if (predicate != null) {
                        fileMetadataStream = fileMetadataStream.filter(predicate);
                    }
                }

                fileMetadataStream = fileMetadataStream.sorted(FileMetadata.getComparator(
                        requestParams.getOrDefault("sort", "lastModifiedDate"),
                        Order.valueOf(requestParams.getOrDefault("order", "DESC"))
                ));
            }

            return fileMetadataStream.toList();
        }
    }

    private void deleteDirectoryRecursively(Path directory) throws IOException {
        try (Stream<Path> walk = Files.walk(directory)) {
            for (Path path : walk.sorted((p1, p2) -> -p1.compareTo(p2)).toList()) {
                Files.delete(path);
            }
        }
    }

    private URI performMoveOrCopyOperation(
            String sourcePath,
            String destinationPath,
            Operation operation
    ) throws IOException {
        Path source = Paths.get(currentUser.getUploadDirectory(), sourcePath);
        Path destination = Paths.get(currentUser.getUploadDirectory(), destinationPath);

        if (!Files.exists(source)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Could not [%s] original resource [%s] because it was not found".formatted(
                            operation.getValue(), sourcePath
                    )
            );
        }

        if (!Files.exists(destination.getParent())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Could not [%s] original resource [%s] because target location not found".formatted(
                            operation.getValue(), destinationPath
                    )
            );
        }

        Path newPath = switch (operation) {
            case MOVE -> Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            case COPY -> Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        };

        return newPath.toUri();
    }
}
