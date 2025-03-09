package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.util.I18n;
import org.springframework.http.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

    private static void transferTo(Path path, OutputStream out) throws IOException {
        transferTo(path, out, true);
    }

    private static void transferTo(Path path, OutputStream out, boolean closeOut) throws IOException {
        transferTo(path, out, closeOut, 0, Long.MAX_VALUE);
    }

    private static void transferTo(Path path, OutputStream out, boolean closeOut, long start, long end) throws IOException {
        WritableByteChannel outChannel = Channels.newChannel(out);
        try (FileChannel inChannel = FileChannel.open(path, StandardOpenOption.READ);
             AutoCloseable ignored = closeOut ? outChannel : null) {
            long total = inChannel.size();
            start = Math.min(Math.max(start, 0), total);
            end = Math.min(end, total);
            if (start >= end) {
                return;
            }
            while (start < end) {
                long transferred = inChannel.transferTo(start, end - start, outChannel);
                if (transferred == 0) {
                    long currentPosition = inChannel.position();
                    if (currentPosition >= total) {
                        break;
                    }
                    throw new IOException("Failed to transfer any bytes. Check if the output stream is closed or full.");
                }
                start += transferred;
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static ResponseEntity<StreamingResponseBody> download(String name, String mimeType, Path path) {
        return downloadResponse(name, MediaType.valueOf(mimeType),
                HttpStatus.OK,
                out -> transferTo(path, out),
                new HttpHeaders());
    }

    public static ResponseEntity<StreamingResponseBody> downloadChunked(String name, String mimeType, Path path, long start, long end) {
        long size;
        try {
            size = Files.size(path);
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("getFileSizeFailed"));
        }
        long endReal = Math.min(end, size - 1);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.add(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, endReal, size));
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(endReal - start + 1));
        return downloadResponse(name, MediaType.valueOf(mimeType),
                HttpStatus.PARTIAL_CONTENT,
                out -> transferTo(path, out, true, start, endReal + 1),
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
                    try (ZipOutputStream zos = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
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
                                transferTo(dir.resolve(file.getPath()), zos, false);
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
