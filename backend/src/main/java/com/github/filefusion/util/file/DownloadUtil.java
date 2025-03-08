package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.util.I18n;
import org.springframework.http.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * DownloadUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class DownloadUtil {

    private static String buildZipPath(FileData file, Map<String, FileData> fileMap) {
        if (FileAttribute.PARENT_ROOT.equals(file.getParentId())) {
            return file.getName();
        }
        return buildZipPath(fileMap.get(file.getParentId()), fileMap) + FileAttribute.SEPARATOR + file.getName();
    }

    public static ResponseEntity<StreamingResponseBody> download(String name, String mimeType, Path path) {
        return downloadResponse(name, MediaType.valueOf(mimeType),
                HttpStatus.OK,
                out -> Files.copy(path, out),
                new HttpHeaders());
    }

    public static ResponseEntity<StreamingResponseBody> downloadChunked(String name, String mimeType, Path path, long start, long end) {
        long size;
        try {
            size = Files.size(path);
        } catch (IOException e) {
            throw new HttpException(I18n.get("getFileSizeFailed"));
        }
        long endReal = end == -1 ? size - 1 : end;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.add(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, endReal, size));
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(endReal - start + 1));
        return downloadResponse(name, MediaType.valueOf(mimeType),
                HttpStatus.PARTIAL_CONTENT,
                out -> {
                    try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
                         WritableByteChannel outChannel = Channels.newChannel(out)) {
                        fileChannel.transferTo(start, endReal - start + 1, outChannel);
                    }
                },
                headers);
    }

    public static ResponseEntity<StreamingResponseBody> downloadZip(Path dir, List<FileData> fileList) {
        return downloadResponse(FileAttribute.DOWNLOAD_ZIP_NAME,
                FileAttribute.MimeType.ZIP.value(),
                HttpStatus.OK,
                out -> {
                    Map<String, FileData> fileMap = new HashMap<>();
                    Map<String, String> pathMap = new HashMap<>();
                    fileList.forEach(file -> fileMap.put(file.getId(), file));
                    fileList.forEach(file -> pathMap.put(file.getId(), buildZipPath(file, fileMap)));
                    try (ZipOutputStream zos = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
                        zos.setLevel(Deflater.NO_COMPRESSION);
                        for (FileData file : fileList) {
                            if (FileAttribute.MimeType.FOLDER.value().toString().equals(file.getMimeType())) {
                                zos.putNextEntry(new ZipEntry(pathMap.get(file.getId()) + FileAttribute.SEPARATOR));
                                zos.closeEntry();
                            }
                        }
                        for (FileData file : fileList) {
                            if (!FileAttribute.MimeType.FOLDER.value().toString().equals(file.getMimeType())) {
                                zos.putNextEntry(new ZipEntry(pathMap.get(file.getId())));
                                Files.copy(dir.resolve(file.getPath()), zos);
                                zos.closeEntry();
                            }
                        }
                    }
                },
                new HttpHeaders());
    }

    private static ResponseEntity<StreamingResponseBody> downloadResponse(String filename,
                                                                          MediaType mediaType,
                                                                          HttpStatus status,
                                                                          StreamingResponseBody body,
                                                                          HttpHeaders headers) {
        String contentDisposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build().toString();
        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .headers(headers)
                .contentType(mediaType)
                .body(body);
    }

}
