package sgu.borodin.nas.service;

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
public class FileOperationsService {
    private static final Path UPLOAD_DIR = Path.of("C:\\Users\\sgnot\\uploads");

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    public Resource download(String filename) {
        File file = Paths.get(UPLOAD_DIR.toString(), filename).toFile();

        if (!file.exists() || !file.isFile()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File " + filename + " not found");
        }

        log.info("Downloading file {}", file.getPath());

        return new FileSystemResource(file);
    }

    public List<FileMetadata> list(String path, Map<String, String> requestParams) {
        try {
            Path directoryPath = Paths.get(UPLOAD_DIR.toString(), path);

            if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Directory " + path + " not found");
            }

            return getFilesMetadata(directoryPath, requestParams);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error listing files for directory " + path,
                    e
            );
        }
    }

    public URI upload(MultipartFile file) throws IOException {
        if (!Files.exists(UPLOAD_DIR)) {
            Files.createDirectory(UPLOAD_DIR);
        }

        validateFile(file);
        Path filePath = Paths.get(UPLOAD_DIR.toString(), file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File {} was successfully uploaded", file.getOriginalFilename());

        return filePath.toUri();
    }

    public URI move(String sourcePath, String destinationPath) {
        return performMoveOrCopyOperation(sourcePath, destinationPath, Operation.MOVE);
    }

    public URI copy(String sourcePath, String destinationPath) {
        return performMoveOrCopyOperation(sourcePath, destinationPath, Operation.COPY);
    }

    public void delete(String path) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR.toString(), path);

        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File or directory " + path + " not found");
        }

        if (Files.isDirectory(filePath)) {
            deleteDirectoryRecursively(filePath);
        } else {
            Files.delete(filePath);
            log.info("Deleted file {}", filePath.getFileName());
        }
    }

    public HttpHeaders getMetadata(String path) throws IOException {
        File file = Paths.get(UPLOAD_DIR.toString(), path).toFile();

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

                String sort = requestParams.get("sort"), order = requestParams.get("order");

                if (Objects.nonNull(sort) && Objects.nonNull(order)) {
                    fileMetadataStream = fileMetadataStream.sorted(FileMetadata.getComparator(sort, Order.valueOf(order)));
                }
            }

            return fileMetadataStream.toList();
        }
    }

    private void deleteDirectoryRecursively(Path directory) throws IOException {
        try (Stream<Path> walk = Files.walk(directory)) {
            for (Path path : walk.sorted((p1, p2) -> -p1.compareTo(p2)).toList()) {
                Files.delete(path);
                log.info("Deleted {}", path);
            }
        }
    }

    private URI performMoveOrCopyOperation(String sourcePath, String destinationPath, Operation operation) {
        try {
            Path source = Paths.get(UPLOAD_DIR.toString(), sourcePath);
            Path destination = Paths.get(UPLOAD_DIR.toString(), destinationPath);

            if (!Files.exists(source)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Original file not found");
            }

            if (!Files.exists(destination.getParent())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target directory not found");
            }

            return (switch (operation) {
                case MOVE -> Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                case COPY -> Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }).toUri();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File move error", e);
        }
    }
}
