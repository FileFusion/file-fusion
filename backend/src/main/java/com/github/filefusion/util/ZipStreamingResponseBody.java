package com.github.filefusion.util;

import com.github.filefusion.constant.FileAttribute;
import jakarta.annotation.Nonnull;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZipStreamingResponseBody
 *
 * @author hackyo
 * @since 2022/4/1
 */
public record ZipStreamingResponseBody(List<Path> pathList) implements StreamingResponseBody {

    @Override
    public void writeTo(@Nonnull OutputStream out) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            zos.setLevel(Deflater.BEST_SPEED);
            for (Path path : pathList) {
                addToZip(zos, path, "");
            }
        }
    }

    private void addToZip(ZipOutputStream zos, Path path, String parent) throws IOException {
        String entryName = parent + path.getFileName();
        if (Files.isDirectory(path)) {
            entryName += FileAttribute.SEPARATOR;
            zos.putNextEntry(new ZipEntry(entryName));
            zos.closeEntry();
            try (DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
                for (Path child : children) {
                    addToZip(zos, child, entryName);
                }
            }
        } else {
            zos.putNextEntry(new ZipEntry(entryName));
            Files.copy(path, zos);
            zos.closeEntry();
        }
    }

}
