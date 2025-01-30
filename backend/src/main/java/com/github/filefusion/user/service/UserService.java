package com.github.filefusion.user.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.user.entity.Role;
import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.user.entity.UserRole;
import com.github.filefusion.user.model.UpdateUserModel;
import com.github.filefusion.user.model.UserToken;
import com.github.filefusion.user.model.UserTokenResponse;
import com.github.filefusion.user.repository.*;
import com.github.filefusion.util.EncryptUtil;
import com.github.filefusion.util.I18n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UserService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserInfoRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final OrgUserRepository orgUserRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder,
                       UserInfoRepository userRepository,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       OrgUserRepository orgUserRepository,
                       PermissionRepository permissionRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.orgUserRepository = orgUserRepository;
        this.permissionRepository = permissionRepository;
    }

    public UserTokenResponse login(UserInfo user) throws AuthenticationException {
        String username = user.getUsername();
        String password = EncryptUtil.sha256(user.getPassword());
        user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(I18n.get("usernameNotFound"));
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException(I18n.get("passwordError"));
        }
        user.verifyUser();
        return new UserTokenResponse(UserToken.encoder(user.getId()));
    }

    public void updateCurrentUser(UpdateUserModel user) {
        UserInfo u = userRepository.findById(user.getId()).orElseThrow();
        u.setId(user.getId());
        u.setName(user.getName());
        u.setEmail(user.getEmail());
        u.setPhone(user.getPhone());
        if (!StringUtils.hasLength(u.getPhone())) {
            u.setAreaCode(null);
        } else {
            u.setAreaCode(user.getAreaCode());
        }
        userRepository.save(u);
    }

    public void updateCurrentUserPassword(String userId, String originalPassword, String newPassword) {
        UserInfo u = userRepository.findById(userId).orElseThrow();
        originalPassword = EncryptUtil.sha256(originalPassword);
        if (!passwordEncoder.matches(originalPassword, u.getPassword())) {
            throw new HttpException(I18n.get("originalPasswordError"));
        }
        u.setPassword(passwordEncoder.encode(EncryptUtil.sha256(newPassword)));
        u.setEarliestCredentials(new Date());
        userRepository.save(u);
    }

    public Page<UserInfo> get(PageRequest page, String search) {
        Page<UserInfo> users = userRepository.findAllOrderBySort(search, page);
        List<UserRole> userRoles = userRoleRepository.findAllByUserIdIn(users.getContent().stream().map(UserInfo::getId).distinct().toList());
        List<Role> roles = roleRepository.findAllById(userRoles.stream().map(UserRole::getRoleId).distinct().toList());
        for (UserInfo user : users.getContent()) {
            List<Role> ur = new ArrayList<>();
            for (UserRole userRole : userRoles) {
                if (user.getId().equals(userRole.getUserId())) {
                    for (Role role : roles) {
                        if (userRole.getRoleId().equals(role.getId())) {
                            ur.add(role);
                            break;
                        }
                    }
                }
            }
            user.setRoles(ur);
        }
        return users;
    }

    public UserInfo loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userRepository.findById(username).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException(I18n.get("usernameNotFound"));
        }
        user.setPermissions(permissionRepository.findAllByUserId(user.getId()));
        return user;
    }

    @Transactional(rollbackFor = HttpException.class)
    public UserInfo add(UserInfo user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new HttpException(I18n.get("usernameExits"));
        }
        user.setId(null);
        user.setPassword(passwordEncoder.encode(EncryptUtil.sha256(user.getPassword())));
        user.setEarliestCredentials(new Date());
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
            oldUser.setPassword(passwordEncoder.encode(EncryptUtil.sha256(user.getPassword())));
        }

        if (modifyPassword || oldUser.getEnabled() != user.getEnabled()) {
            oldUser.setEarliestCredentials(new Date());
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

    public void saveUserRoles(String userId, List<Role> roles) {
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
    public void batchDelete(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        List<UserInfo> userList = userRepository.findAllById(userIds);
        for (UserInfo user : userList) {
            if (user.getSystemdUser()) {
                throw new HttpException(I18n.get("systemdUserCannotDeleted"));
            }
        }
        // todo Other items that prevent deletion
        orgUserRepository.deleteAllByUserIdIn(userIds);
        userRoleRepository.deleteAllByUserIdIn(userIds);
        userRepository.deleteAllByIdInBatch(userIds);
    }

}
