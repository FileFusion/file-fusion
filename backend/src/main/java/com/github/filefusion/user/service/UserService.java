package com.github.filefusion.user.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.common.SecurityProperties;
import com.github.filefusion.user.entity.Permission;
import com.github.filefusion.user.entity.Role;
import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.user.entity.UserRole;
import com.github.filefusion.user.model.UpdateUserModel;
import com.github.filefusion.user.repository.*;
import com.github.filefusion.util.EncryptUtil;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.ULID;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * UserService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class UserService implements UserDetailsService {

    private static final String TOKEN_HEADER = "Bearer ";
    private static final String TOKEN_SCOPE_KEY = "scope";
    private static final long TOKEN_EXPIRATION = 1000 * 60 * 60 * 24;
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final String applicationName;
    private final SecurityProperties securityProperties;
    private final UserInfoRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final OrgUserRepository orgUserRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public UserService(@Value("${spring.application.name}") String applicationName,
                       SecurityProperties securityProperties,
                       UserInfoRepository userRepository,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       OrgUserRepository orgUserRepository,
                       PermissionRepository permissionRepository) {
        this.applicationName = applicationName;
        this.securityProperties = securityProperties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.orgUserRepository = orgUserRepository;
        this.permissionRepository = permissionRepository;
    }

    private String getUserToken(String userId, List<Permission> permissionList) {
        Date currentTime = new Date();
        List<String> scopeList = permissionList.stream().map(Permission::getAuthority).toList();
        return TOKEN_HEADER + Jwts.builder()
                .issuer(applicationName)
                .issuedAt(currentTime)
                .notBefore(currentTime)
                .expiration(new Date(currentTime.getTime() + TOKEN_EXPIRATION))
                .subject(userId)
                .claim(TOKEN_SCOPE_KEY, scopeList)
                .id(ULID.randomULID())
                .signWith(securityProperties.getSecret().getPrivateKey(), Jwts.SIG.EdDSA)
                .compact();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(I18n.get("usernameNotFound")));
        user.setPermissions(permissionRepository.findAllByUserId(user.getId()));
        return user;
    }

    public String login(UserInfo user) throws AuthenticationException {
        String username = user.getUsername();
        String password = EncryptUtil.blake3(user.getPassword());
        user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(I18n.get("usernameNotFound")));
        if (!PASSWORD_ENCODER.matches(password, user.getPassword())) {
            throw new BadCredentialsException(I18n.get("passwordError"));
        }
        user.verifyUser();
        return getUserToken(user.getId(), permissionRepository.findAllByUserId(user.getId()));
    }

    public UserInfo getById(String userId) {
        UserInfo user = userRepository.findById(userId).orElseThrow();
        user.setPermissions(permissionRepository.findAllByUserId(user.getId()));
        return user;
    }

    public void updateCurrentUser(UpdateUserModel user) {
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
        userRepository.save(u);
    }

    public void updateCurrentUserPassword(String userId, String originalPassword, String newPassword) {
        UserInfo u = userRepository.findById(userId).orElseThrow();
        if (!PASSWORD_ENCODER.matches(EncryptUtil.blake3(originalPassword), u.getPassword())) {
            throw new HttpException(I18n.get("originalPasswordError"));
        }
        u.setPassword(PASSWORD_ENCODER.encode(EncryptUtil.blake3(newPassword)));
        u.setEarliestCredentials(LocalDateTime.now());
        userRepository.save(u);
    }

    public Page<UserInfo> get(PageRequest page, String search) {
        Page<UserInfo> users = userRepository.findAllOrderBySort(search, page);
        Map<String, List<String>> userIdRoleListMap = userRoleRepository.findAllByUserIdIn(users.getContent().stream().map(UserInfo::getId).toList())
                .stream().collect(Collectors.groupingBy(
                        UserRole::getUserId,
                        Collectors.mapping(UserRole::getRoleId, Collectors.toList())
                ));
        Map<String, Role> roleMap = roleRepository.findAllById(userIdRoleListMap.values().stream().flatMap(List::stream).distinct().toList())
                .stream().collect(Collectors.toMap(Role::getId, Function.identity()));
        for (UserInfo user : users.getContent()) {
            user.setRoles(userIdRoleListMap.getOrDefault(user.getId(), List.of())
                    .stream().map(roleMap::get).filter(Objects::nonNull).toList());
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

        List<Role> roles = user.getRoles();
        user = userRepository.save(user);
        saveUserRoles(user.getId(), roles);
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
            saveUserRoles(oldUser.getId(), user.getRoles());
        }

        return userRepository.save(oldUser);
    }

    private void saveUserRoles(String userId, List<Role> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return;
        }
        List<UserRole> userRoles = new ArrayList<>(roles.size());
        for (Role role : roles) {
            UserRole userRole = new UserRole();
            userRole.setRoleId(role.getId());
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
