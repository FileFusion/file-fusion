package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.ULID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RecycleBinUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public final class RecycleBinUtil {

    private final Path baseDir;
    private final FileUtil fileUtil;

    @Autowired
    public RecycleBinUtil(@Value("${recycle-bin.dir}") String recycleBinDir,
                          FileUtil fileUtil) {
        this.baseDir = Paths.get(recycleBinDir).normalize().toAbsolutePath();
        this.fileUtil = fileUtil;
        if (!Files.exists(this.baseDir)) {
            try {
                Files.createDirectories(this.baseDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<FileData> setRecycleInfo(List<FileData> fileList, Map<String, List<FileData>> childFileMap) {
        LocalDateTime deletedDate = LocalDateTime.now();
        String recycleId = ULID.randomULID();
        List<FileData> allFileList = new ArrayList<>();
        fileList.forEach(file -> {
            file.setDeleted(true);
            file.setDeletedDate(deletedDate);
            Path filePath = Paths.get(file.getPath());
            Path fileRecyclePath = Paths.get(filePath.getName(0).toString(), recycleId, filePath.getFileName().toString());
            file.setRecyclePath(fileRecyclePath.toString());
            allFileList.add(file);
            List<FileData> childFileList = childFileMap.get(file.getPath());
            if (!CollectionUtils.isEmpty(childFileList)) {
                childFileList.forEach(childFile -> {
                    childFile.setDeleted(true);
                    childFile.setDeletedDate(deletedDate);
                    String childFileRelativePath = childFile.getPath().substring(file.getPath().length() + 1);
                    Path childFileRecyclePath = Paths.get(file.getRecyclePath(), childFileRelativePath);
                    childFile.setRecyclePath(childFileRecyclePath.toString());
                    allFileList.add(childFile);
                });
            }
        });
        return allFileList;
    }

    public void recycle(List<FileData> recycleFileList) {
        AtomicBoolean success = new AtomicBoolean(true);
        recycleFileList.forEach(fileData -> {
            try {
                Path originalPath = fileUtil.validatePath(fileData.getPath());
                Path targetPath = baseDir.resolve(fileData.getRecyclePath());
                Files.createDirectories(targetPath.getParent());
                Files.move(originalPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                success.set(false);
            }
        });
        if (!success.get()) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileMoveFailed"));
        }
    }

}
