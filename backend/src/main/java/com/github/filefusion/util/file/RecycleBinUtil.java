package com.github.filefusion.util.file;

import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.util.ULID;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RecycleBinUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Getter
@Component
public final class RecycleBinUtil {

    private final Path baseDir;

    @Autowired
    public RecycleBinUtil(@Value("${recycle-bin.dir}") String recycleBinDir) {
        this.baseDir = Paths.get(recycleBinDir).normalize().toAbsolutePath();
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

}
