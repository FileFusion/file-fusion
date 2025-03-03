package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.ULID;
import lombok.Getter;
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

    @Getter
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

    public List<FileData> setRecycleInfo(List<FileData> parentList, Map<String, List<FileData>> childMap) {
        LocalDateTime deletedDate = LocalDateTime.now();
        String recycleId = ULID.randomULID();
        List<FileData> allList = new ArrayList<>();
        parentList.forEach(file -> {
            file.setDeleted(true);
            file.setDeletedDate(deletedDate);
            Path filePath = Paths.get(file.getPath());
            Path fileRecyclePath = Paths.get(filePath.getName(0).toString(), recycleId, filePath.getFileName().toString());
            file.setRecyclePath(fileRecyclePath.toString());
            allList.add(file);
            List<FileData> childList = childMap.get(file.getPath());
            if (!CollectionUtils.isEmpty(childList)) {
                childList.forEach(childFile -> {
                    childFile.setDeleted(true);
                    childFile.setDeletedDate(deletedDate);
                    Path childFileRecyclePath = Paths.get(file.getRecyclePath(),
                            childFile.getPath().substring(file.getPath().length() + 1));
                    childFile.setRecyclePath(childFileRecyclePath.toString());
                    allList.add(childFile);
                });
            }
        });
        return allList;
    }

    public void recycle(List<FileData> recycleList) {
        AtomicBoolean success = new AtomicBoolean(true);
        recycleList.forEach(file -> {
            try {
                Path originalPath = PathUtil.resolvePath(fileUtil.getBaseDir(), file.getPath(), true);
                Path targetPath = PathUtil.resolvePath(baseDir, file.getRecyclePath(), false);
                Files.createDirectories(targetPath.getParent());
                Files.move(originalPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                success.set(false);
            }
        });
        if (!success.get()) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileRecycleFailed"));
        }
    }

}
