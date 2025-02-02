package com.github.filefusion.user.controller;

import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.user.entity.Org;
import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.user.model.UpdateUserModel;
import com.github.filefusion.user.model.UpdateUserPasswordModel;
import com.github.filefusion.user.model.UserTokenResponse;
import com.github.filefusion.user.service.OrgService;
import com.github.filefusion.user.service.UserService;
import com.github.filefusion.util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final OrgService orgService;

    @Autowired
    public UserController(UserService userService,
                          OrgService orgService) {
        this.userService = userService;
        this.orgService = orgService;
    }

    /**
     * user login
     *
     * @param user user
     * @return authentication key
     */
    @PostMapping("/_login")
    public UserTokenResponse login(@RequestBody UserInfo user) {
        return userService.login(user);
    }

    /**
     * get current user
     *
     * @return user
     */
    @GetMapping("/current")
    @PreAuthorize("hasAuthority('user:read')")
    public UserInfo getCurrentUser() {
        return CurrentUser.get();
    }

    /**
     * get current user org
     *
     * @return org list
     */
    @GetMapping("/current/org")
    @PreAuthorize("hasAuthority('user:read')")
    public List<Org> getCurrentUserOrg() {
        return orgService.getUserOrg(CurrentUser.get().getId());
    }

    /**
     * update current user
     *
     * @param user user
     */
    @PutMapping("/current")
    @PreAuthorize("hasAuthority('user:edit')")
    public void updateCurrentUser(@RequestBody UpdateUserModel user) {
        user.setId(CurrentUser.get().getId());
        userService.updateCurrentUser(user);
    }

    /**
     * update current user password
     *
     * @param updateUserPasswordModel update user password model
     */
    @PutMapping("/password/current")
    @PreAuthorize("hasAuthority('user:edit')")
    public void updateCurrentUserPassword(@RequestBody UpdateUserPasswordModel updateUserPasswordModel) {
        userService.updateCurrentUserPassword(CurrentUser.get().getId(),
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
     * batch delete user
     *
     * @param userIds user ids
     */
    @PostMapping("/_batch_delete")
    @PreAuthorize("hasAuthority('user_management:delete')")
    public void batchDelete(@RequestBody List<String> userIds) {
        userService.batchDelete(userIds);
    }

}
