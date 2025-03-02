package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.ULID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
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

    public void setRecycleInfo(List<FileData> recycleFileList) {
        LocalDateTime deletedDate = LocalDateTime.now();
        String recycleId = ULID.randomULID();
        recycleFileList.forEach(fileData -> {
            fileData.setDeleted(true);
            fileData.setDeletedDate(deletedDate);
            Path filePath = Paths.get(fileData.getPath()).normalize();
            fileData.setRecyclePath(Paths.get(filePath.getName(0).toString(),
                    recycleId, filePath.getFileName().toString()).toString());
        });
    }

    public void recycle(List<FileData> recycleFileList) {
        AtomicBoolean success = new AtomicBoolean(true);
        recycleFileList.forEach(fileData -> {
            try {
                Path originalPath = fileUtil.validatePath(fileData.getPath());
                Path targetPath = baseDir.resolve(fileData.getRecyclePath()).normalize();
                fileUtil.delete(targetPath);
                Path targetParentDir = targetPath.getParent();
                if (targetParentDir != null) {
                    Files.createDirectories(targetParentDir);
                }
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
