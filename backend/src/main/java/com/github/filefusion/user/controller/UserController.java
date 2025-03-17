package com.github.filefusion.user.controller;

import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.user.model.UpdateUserModel;
import com.github.filefusion.user.model.UpdateUserPasswordModel;
import com.github.filefusion.user.service.UserService;
import com.github.filefusion.util.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * UserController
 *
 * @author hackyo
 * @since 2022/4/1
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * user login
     *
     * @param user user
     * @return authentication key
     */
    @PostMapping("/_login")
    public String login(@RequestBody UserInfo user, HttpServletRequest request) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        return userService.login(user, userAgent, userAgent);
    }

    /**
     * get current user
     *
     * @return user
     */
    @GetMapping("/current")
    @PreAuthorize("hasAuthority('user:read')")
    public UserInfo getCurrentUser() {
        return userService.getById(CurrentUser.getId());
    }

    /**
     * update current user
     *
     * @param user user
     */
    @PutMapping("/current")
    @PreAuthorize("hasAuthority('user:edit')")
    public UserInfo updateCurrentUser(@RequestBody UpdateUserModel user) {
        user.setId(CurrentUser.getId());
        return userService.updateCurrentUser(user);
    }

    /**
     * update current user password
     *
     * @param updateUserPasswordModel update user password model
     */
    @PutMapping("/current/password")
    @PreAuthorize("hasAuthority('user:edit')")
    public UserInfo updateCurrentUserPassword(@RequestBody UpdateUserPasswordModel updateUserPasswordModel) {
        return userService.updateCurrentUserPassword(CurrentUser.getId(),
                updateUserPasswordModel.getOriginalPassword(), updateUserPasswordModel.getNewPassword());
    }

    /**
     * get user list - paged
     *
     * @param page        page
     * @param pageSize    page size
     * @param search      key work
     * @param sorter      sorter
     * @param sorterOrder sorter order
     * @return user list
     */
    @GetMapping("/{page}/{pageSize}")
    @PreAuthorize("hasAuthority('user_management:read')")
    public Page<UserInfo> get(@PathVariable Integer page, @PathVariable Integer pageSize,
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) String sorter,
                              @RequestParam(required = false) SorterOrder sorterOrder) {
        if (!StringUtils.hasLength(sorter)) {
            sorter = "username";
        }
        if (sorterOrder == null) {
            sorterOrder = SorterOrder.ascend;
        }
        return userService.get(PageRequest.of(page - 1, pageSize, sorterOrder.order(), sorter), search);
    }

    /**
     * add user
     *
     * @param user user
     * @return user
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user_management:add')")
    public UserInfo add(@RequestBody UserInfo user) {
        return userService.add(user);
    }

    /**
     * update user
     *
     * @param user user
     * @return user
     */
    @PutMapping
    @PreAuthorize("hasAuthority('user_management:edit')")
    public UserInfo update(@RequestBody UserInfo user) {
        return userService.update(user);
    }

    /**
     * delete user
     *
     * @param userId user id
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user_management:delete')")
    public void delete(@PathVariable String userId) {
        userService.delete(userId);
    }

}
