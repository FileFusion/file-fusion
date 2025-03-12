package com.github.filefusion.util.file;

import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.file.entity.FileData;
import org.springframework.http.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    private static String buildZipPath(FileData file, Map<String, FileData> idToFileMap) {
        Deque<String> pathSegments = new ArrayDeque<>();
        while (file != null && !FileAttribute.PARENT_ROOT.equals(file.getParentId())) {
            pathSegments.addFirst(file.getName());
            file = idToFileMap.get(file.getParentId());
        }
        if (file != null) {
            pathSegments.addFirst(file.getName());
        }
        return String.join(FileAttribute.SEPARATOR, pathSegments);
    }

    public static ResponseEntity<StreamingResponseBody> download(String name, String mimeType, Path path) {
        return downloadResponse(name, MediaType.valueOf(mimeType),
                HttpStatus.OK,
                out -> FileUtil.transferTo(path, Channels.newChannel(out)),
                new HttpHeaders());
    }

    public static ResponseEntity<StreamingResponseBody> downloadChunked(
            String name, String mimeType, Path path, long start, long end) throws IOException {
        long size = Files.size(path);
        long endReal = Math.min(end, size - 1);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.add(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, endReal, size));
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(endReal - start + 1));
        return downloadResponse(name, MediaType.valueOf(mimeType),
                HttpStatus.PARTIAL_CONTENT,
                out -> FileUtil.transferTo(path, Channels.newChannel(out), true, start, endReal + 1),
                headers);
    }

    public static ResponseEntity<StreamingResponseBody> downloadZip(Path dir, List<FileData> fileList) {
        return downloadResponse(FileAttribute.DOWNLOAD_ZIP_NAME,
                FileAttribute.MimeType.ZIP.value(),
                HttpStatus.OK,
                out -> {
                    Map<String, FileData> idToFileMap = fileList.stream()
                            .collect(Collectors.toMap(FileData::getId, Function.identity()));
                    Map<String, String> idToZipPath = fileList.stream()
                            .collect(Collectors.toMap(FileData::getId, file -> buildZipPath(file, idToFileMap)));
                    try (ZipOutputStream zos = new ZipOutputStream(out, StandardCharsets.UTF_8);
                         WritableByteChannel outChannel = Channels.newChannel(zos)) {
                        zos.setLevel(Deflater.NO_COMPRESSION);
                        for (FileData file : fileList) {
                            if (FileAttribute.MimeType.FOLDER.value().toString().equals(file.getMimeType())) {
                                zos.putNextEntry(new ZipEntry(idToZipPath.get(file.getId()) + FileAttribute.SEPARATOR));
                                zos.closeEntry();
                            }
                        }
                        for (FileData file : fileList) {
                            if (!FileAttribute.MimeType.FOLDER.value().toString().equals(file.getMimeType())) {
                                zos.putNextEntry(new ZipEntry(idToZipPath.get(file.getId())));
                                FileUtil.transferTo(dir.resolve(file.getPath()), outChannel, false);
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
