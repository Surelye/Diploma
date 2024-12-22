package sgu.borodin.nas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import sgu.borodin.nas.enums.Order;
import sgu.borodin.nas.util.Extensions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;

@Data
public class FileMetadata {
    @JsonIgnore
    private Path path;

    private long size;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime creationTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime lastModifiedTime;

    boolean isDirectory;

    public FileMetadata(Path path) {
        try {
            this.path = path;
            this.creationTime = Files.readAttributes(path, BasicFileAttributes.class)
                    .creationTime()
                    .toInstant()
                    .atZone(ZoneId.of("UTC"));
            this.lastModifiedTime = Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.of("UTC"));
            this.isDirectory = Files.isDirectory(path);
            this.size = this.isDirectory
                    ? Extensions.getDirectorySize(path)
                    : Files.size(path);
        } catch (IOException e) {
            throw new IllegalStateException("Error getting metadata for file " + path.getFileName(), e);
        }
    }

    public static Comparator<FileMetadata> getComparator(String sort, Order order) {
        Comparator<FileMetadata> comparator = switch (sort) {
            case "name" -> Comparator.comparing(FileMetadata::getName);
            case "size" -> Comparator.comparing(FileMetadata::getSize);
            case "creationTime" -> Comparator.comparing(FileMetadata::getCreationTime);
            default -> Comparator.comparing(FileMetadata::getLastModifiedTime);
        };

        return Order.isDescending(order)
                ? comparator.reversed()
                : comparator;
    }

    public String getName() {
        return path.getFileName().toString();
    }
}
