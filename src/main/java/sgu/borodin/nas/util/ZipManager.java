package sgu.borodin.nas.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
public class ZipManager {

    public void createZipFile(String rootPath, File zipFile, List<String> filePaths) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (String filePath : filePaths) {
                File fileToZip = new File(Path.of(rootPath, filePath).toString());
                if (fileToZip.exists()) {
                    addFileToZip(fileToZip, zipOut, fileToZip.getName());
                } else {
                    log.error("Could not add file [{}] to zip archive", filePath);
                }
            }
        }
    }

    private void addFileToZip(File fileToZip, ZipOutputStream zipOut, String fileName) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }

        if (fileToZip.isDirectory()) {
            for (File childFile : fileToZip.listFiles()) {
                addFileToZip(childFile, zipOut, fileName + "/" + childFile.getName());
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }

    public record ZipFile(String path, byte[] content) {}
}
