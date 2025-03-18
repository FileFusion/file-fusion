package com.github.filefusion.user.service;

import com.github.filefusion.user.entity.Permission;
import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.user.repository.PermissionRepository;
import com.github.filefusion.user.repository.UserInfoRepository;
import com.github.filefusion.util.BaseCacheService;
import com.github.filefusion.util.DistributedLock;
import jakarta.persistence.EntityNotFoundException;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * UserCacheService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class UserCacheService extends BaseCacheService<UserInfo> {

    private final UserInfoRepository userRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public UserCacheService(RedissonClient redissonClient,
                            DistributedLock distributedLock,
                            UserInfoRepository userRepository,
                            PermissionRepository permissionRepository) {
        super(redissonClient, distributedLock);
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    protected Class<UserInfo> getEntityClass() {
        return UserInfo.class;
    }

    @Override
    protected String getCacheKeyPrefix() {
        return "users";
    }

    @Override
    protected UserInfo queryFromDb(String id) throws EntityNotFoundException {
        UserInfo user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        user.setPermissionIds(permissionRepository.findAllByUserId(id).stream().map(Permission::getId).toList());
        return user;
    }

}
