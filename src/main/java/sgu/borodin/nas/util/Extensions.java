package sgu.borodin.nas.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

@UtilityClass
@Slf4j
public class Extensions {
    public static final String EMPTY_STRING = "";

    public static long getDirectorySize(Path directoryPath) throws IOException {
        final long[] totalSize = {0};

        Files.walkFileTree(directoryPath, new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                totalSize[0] += Files.size(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                log.warn("Failed to visit file {} during traversal of directory {}",
                        file.getFileName(), directoryPath.getFileName(), e);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });

        return totalSize[0];
    }

    public static <T> T ternary(boolean expression, T returnOnTrue, T returnOnFalse) {
        return expression
                ? returnOnTrue
                : returnOnFalse;
    }
}
