package com.github.filefusion.user.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.common.SecurityProperties;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.user.entity.Permission;
import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.user.entity.UserRole;
import com.github.filefusion.user.model.UpdateUserModel;
import com.github.filefusion.user.model.UserTokenModel;
import com.github.filefusion.user.repository.OrgUserRepository;
import com.github.filefusion.user.repository.PermissionRepository;
import com.github.filefusion.user.repository.UserInfoRepository;
import com.github.filefusion.user.repository.UserRoleRepository;
import com.github.filefusion.util.EncryptUtil;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.ULID;
import io.jsonwebtoken.Jwts;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * UserService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class UserService {

    private static final String TOKEN_HEADER = "Bearer ";
    private static final long TOKEN_EXPIRATION = 1000 * 60 * 60 * 24;
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final RedissonClient redissonClient;
    private final SecurityProperties securityProperties;
    private final UserInfoRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final OrgUserRepository orgUserRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public UserService(RedissonClient redissonClient,
                       SecurityProperties securityProperties,
                       UserInfoRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       OrgUserRepository orgUserRepository,
                       PermissionRepository permissionRepository) {
        this.redissonClient = redissonClient;
        this.securityProperties = securityProperties;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.orgUserRepository = orgUserRepository;
        this.permissionRepository = permissionRepository;
    }

    private String getUserToken(String userId, String tokenId, Date currentTime) {
        return TOKEN_HEADER + Jwts.builder()
                .subject(userId)
                .id(tokenId)
                .issuedAt(currentTime)
                .signWith(securityProperties.getSecret().getPrivateKey(), Jwts.SIG.EdDSA)
                .compact();
    }

    public void verifyToken(String userId, String tokenId) throws AccountStatusException {
        RMapCache<String, UserTokenModel> userTokenMap = redissonClient.getMapCache(RedisAttribute.TOKEN_PREFIX + userId);
        if (!userTokenMap.containsKey(tokenId)) {
            throw new CredentialsExpiredException(I18n.get("certificationExpired"));
        }
    }

    public void verifyUser(UserInfo userInfo) throws AccountStatusException {
        if (!userInfo.getNonExpired()) {
            throw new AccountExpiredException(I18n.get("userExpired"));
        }
        if (!userInfo.getNonLocked()) {
            throw new LockedException(I18n.get("userLocked"));
        }
        if (!userInfo.getCredentialsNonExpired()) {
            throw new CredentialsExpiredException(I18n.get("userPasswordExpired"));
        }
        if (!userInfo.getEnabled()) {
            throw new DisabledException(I18n.get("userDisabled"));
        }
    }

    public String login(UserInfo user, String userAgent, String clientIp) throws AuthenticationException {
        String username = user.getUsername();
        String password = EncryptUtil.blake3(user.getPassword());
        user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException(I18n.get("invalidCredentials")));
        if (!PASSWORD_ENCODER.matches(password, user.getPassword())) {
            throw new BadCredentialsException(I18n.get("invalidCredentials"));
        }
        verifyUser(user);

        String tokenId = ULID.randomULID();
        Date currentTime = new Date();
        String token = getUserToken(user.getId(), tokenId, currentTime);
        UserTokenModel userTokenModel = new UserTokenModel();
        userTokenModel.setUserAgent(userAgent);
        userTokenModel.setClientIp(clientIp);
        userTokenModel.setIssuedAt(currentTime);
        RMapCache<String, UserTokenModel> userTokenMap = redissonClient.getMapCache(RedisAttribute.TOKEN_PREFIX + user.getId());
        userTokenMap.put(tokenId, userTokenModel, TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);

        return token;
    }

    public UserInfo getById(String userId) {
        UserInfo user = userRepository.findById(userId).orElseThrow();
        user.setPermissionIds(permissionRepository.findAllByUserId(userId).stream().map(Permission::getId).toList());
        return user;
    }

    public UserInfo updateCurrentUser(UpdateUserModel user) {
        UserInfo u = userRepository.findById(user.getId()).orElseThrow();
        u.setId(user.getId());
        u.setName(user.getName());
        if (!StringUtils.hasLength(user.getEmail())) {
            u.setEmail(null);
        } else {
            u.setEmail(user.getEmail());
        }
        if (!StringUtils.hasLength(user.getAreaCode()) || !StringUtils.hasLength(user.getPhone())) {
            u.setAreaCode(null);
            u.setPhone(null);
        } else {
            u.setAreaCode(user.getAreaCode());
            u.setPhone(user.getPhone());
        }
        return userRepository.save(u);
    }

    public UserInfo updateCurrentUserPassword(String userId, String originalPassword, String newPassword) {
        UserInfo u = userRepository.findById(userId).orElseThrow();
        if (!PASSWORD_ENCODER.matches(EncryptUtil.blake3(originalPassword), u.getPassword())) {
            throw new HttpException(I18n.get("originalPasswordError"));
        }
        u.setPassword(PASSWORD_ENCODER.encode(EncryptUtil.blake3(newPassword)));
        u.setEarliestCredentials(LocalDateTime.now());
        return userRepository.save(u);
    }

    public Page<UserInfo> get(PageRequest page, String search) {
        Page<UserInfo> users = userRepository.findAllOrderBySort(search, page);
        Map<String, List<String>> userIdRoleListMap = userRoleRepository.findAllByUserIdIn(users.getContent().stream().map(UserInfo::getId).toList())
                .stream().collect(Collectors.groupingBy(
                        UserRole::getUserId,
                        Collectors.mapping(UserRole::getRoleId, Collectors.toList())
                ));
        for (UserInfo user : users.getContent()) {
            user.setRoleIds(userIdRoleListMap.getOrDefault(user.getId(), List.of()));
        }
        return users;
    }

    @Transactional(rollbackFor = HttpException.class)
    public UserInfo add(UserInfo user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new HttpException(I18n.get("usernameExits"));
        }
        user.setId(null);
        user.setPassword(PASSWORD_ENCODER.encode(EncryptUtil.blake3(user.getPassword())));
        if (!StringUtils.hasLength(user.getEmail())) {
            user.setEmail(null);
        }
        if (!StringUtils.hasLength(user.getAreaCode()) || !StringUtils.hasLength(user.getPhone())) {
            user.setAreaCode(null);
            user.setPhone(null);
        }
        user.setEarliestCredentials(LocalDateTime.now());
        user.setSystemdUser(false);
        user.setNonExpired(true);
        user.setNonLocked(true);
        user.setCredentialsNonExpired(true);

        List<String> roleIds = user.getRoleIds();
        user = userRepository.save(user);
        saveUserRoles(user.getId(), roleIds);
        return user;
    }

    @Transactional(rollbackFor = HttpException.class)
    public UserInfo update(UserInfo user) {
        UserInfo oldUser = userRepository.findById(user.getId()).orElseThrow();
        oldUser.setName(user.getName());
        oldUser.setEmail(user.getEmail());
        oldUser.setAreaCode(user.getAreaCode());
        oldUser.setPhone(user.getPhone());

        boolean modifyPassword = !oldUser.getPassword().equals(user.getPassword());
        if (modifyPassword) {
            oldUser.setPassword(PASSWORD_ENCODER.encode(EncryptUtil.blake3(user.getPassword())));
        }

        if (modifyPassword || oldUser.getEnabled() != user.getEnabled()) {
            oldUser.setEarliestCredentials(LocalDateTime.now());
        }
        oldUser.setEnabled(user.getEnabled());

        if (oldUser.getSystemdUser() && !oldUser.getEnabled()) {
            throw new HttpException(I18n.get("systemdUserCannotDisable"));
        }

        if (!oldUser.getSystemdUser()) {
            userRoleRepository.deleteAllByUserId(oldUser.getId());
            saveUserRoles(oldUser.getId(), user.getRoleIds());
        }

        return userRepository.save(oldUser);
    }

    private void saveUserRoles(String userId, List<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        List<UserRole> userRoles = new ArrayList<>(roleIds.size());
        for (String roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setRoleId(roleId);
            userRole.setUserId(userId);
            userRoles.add(userRole);
        }
        userRoleRepository.saveAll(userRoles);
    }

    @Transactional(rollbackFor = HttpException.class)
    public void delete(String userId) {
        if (!StringUtils.hasLength(userId)) {
            return;
        }
        UserInfo user = userRepository.findById(userId).orElseThrow();
        if (user.getSystemdUser()) {
            throw new HttpException(I18n.get("systemdUserCannotDeleted"));
        }
        // todo Other items that prevent deletion
        orgUserRepository.deleteAllByUserId(userId);
        userRoleRepository.deleteAllByUserId(userId);
        userRepository.deleteById(userId);
    }

}
