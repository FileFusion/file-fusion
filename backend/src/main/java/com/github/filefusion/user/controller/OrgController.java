package com.github.filefusion.user.controller;

import com.github.filefusion.user.entity.Org;
import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.user.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrgController
 *
 * @author hackyo
 * @since 2022/4/1
 */
@RestController
@RequestMapping("/org")
public class OrgController {

    private final OrgService orgService;

    @Autowired
    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }

    /**
     * get org list
     *
     * @return org list
     */
    @GetMapping
    @PreAuthorize("hasAuthority('org:read')")
    public List<Org> get() {
        return orgService.get();
    }

    /**
     * add org
     *
     * @param org org
     * @return org
     */
    @PostMapping
    @PreAuthorize("hasAuthority('org:add')")
    public Org add(@RequestBody Org org) {
        return orgService.add(org);
    }

    /**
     * update org
     *
     * @param org org
     * @return org
     */
    @PutMapping
    @PreAuthorize("hasAuthority('org:edit')")
    public Org update(@RequestBody Org org) {
        return orgService.update(org);
    }

    /**
     * delete org
     *
     * @param orgId org id
     */
    @DeleteMapping("/{orgId}")
    @PreAuthorize("hasAuthority('org:delete')")
    public void delete(@PathVariable String orgId) {
        orgService.delete(orgId);
    }

    /**
     * get org user list - paged
     *
     * @param orgId    org id
     * @param page     page
     * @param pageSize page size
     * @param search   key work
     * @return user list
     */
    @GetMapping("/{orgId}/user/{page}/{pageSize}")
    @PreAuthorize("hasAuthority('org:read')")
    public Page<UserInfo> getOrgUser(@PathVariable String orgId,
                                     @PathVariable Integer page,
                                     @PathVariable Integer pageSize,
                                     @RequestParam String search) {
        return orgService.getOrgUser(orgId, PageRequest.of(page - 1, pageSize), search);
    }

    /**
     * get not exits org user
     *
     * @param orgId org id
     * @return user list
     */
    @GetMapping("/{orgId}/user/not_exits")
    @PreAuthorize("hasAuthority('org:edit')")
    public List<UserInfo> getNotExitsOrgUser(@PathVariable String orgId) {
        return orgService.getNotExitsOrgUser(orgId);
    }

    /**
     * add org user
     *
     * @param orgId   org id
     * @param userIds user ids
     */
    @PutMapping("/{orgId}/user")
    @PreAuthorize("hasAuthority('org:edit')")
    public void addOrgUser(@PathVariable String orgId, @RequestBody List<String> userIds) {
        orgService.addOrgUser(orgId, userIds);
    }

    /**
     * delete org user
     *
     * @param orgId  org id
     * @param userId user id
     */
    @DeleteMapping("/{orgId}/user/{userId}")
    @PreAuthorize("hasAuthority('org:edit')")
    public void deleteOrgUser(@PathVariable String orgId, @PathVariable String userId) {
        orgService.deleteOrgUser(orgId, userId);
    }

}
