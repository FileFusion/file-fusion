package com.github.filefusion.user.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.user.entity.Org;
import com.github.filefusion.user.entity.OrgUser;
import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.user.repository.OrgRepository;
import com.github.filefusion.user.repository.OrgUserRepository;
import com.github.filefusion.user.repository.UserInfoRepository;
import com.github.filefusion.util.I18n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * OrgService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class OrgService {

    private static final String ORG_ROOT = "root";

    private final OrgRepository orgRepository;
    private final OrgUserRepository orgUserRepository;
    private final UserInfoRepository userRepository;

    @Autowired
    public OrgService(OrgRepository orgRepository,
                      OrgUserRepository orgUserRepository,
                      UserInfoRepository userRepository) {
        this.orgRepository = orgRepository;
        this.orgUserRepository = orgUserRepository;
        this.userRepository = userRepository;
    }

    public List<Org> get() {
        return orgRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Org add(Org org) {
        if (orgRepository.existsByNameAndParentId(org.getName(), org.getParentId())) {
            throw new HttpException(I18n.get("orgNameExits"));
        }
        org.setId(null);
        return orgRepository.save(org);
    }

    public Org update(Org org) {
        if (orgRepository.existsByNameAndParentIdAndIdNot(org.getName(), org.getParentId(), org.getId())) {
            throw new HttpException(I18n.get("orgNameExits"));
        }
        Org oldOrg = orgRepository.findById(org.getId()).orElseThrow();
        oldOrg.setName(org.getName());
        if (!StringUtils.hasLength(org.getDescription())) {
            oldOrg.setDescription(null);
        } else {
            oldOrg.setDescription(org.getDescription());
        }
        return orgRepository.save(oldOrg);
    }

    public void delete(String orgId) {
        if (!StringUtils.hasLength(orgId)) {
            return;
        }
        if (orgRepository.existsByParentId(orgId)) {
            throw new HttpException(I18n.get("existSubOrganizations"));
        }
        if (orgUserRepository.existsByOrgId(orgId)) {
            throw new HttpException(I18n.get("existOrgUser"));
        }
        // todo Other items that prevent deletion
        orgRepository.deleteById(orgId);
    }

    public Page<UserInfo> getOrgUser(String orgId, PageRequest page, String search) {
        return userRepository.findAllByIdInOrderByUsernameAsc(orgId, search, page);
    }

    public List<UserInfo> getNotExitsOrgUser(String orgId) {
        List<String> orgAllParentIds = new ArrayList<>();
        getOrgAllParentId(orgAllParentIds, orgId);
        List<String> orgAllChildIds = new ArrayList<>();
        getOrgAllChildId(orgAllChildIds, orgId);
        orgAllParentIds.addAll(orgAllChildIds);
        List<OrgUser> orgUsers = orgUserRepository.findAllByOrgIdIn(orgAllParentIds);
        List<String> userIds = orgUsers.stream().map(OrgUser::getUserId).distinct().toList();
        if (CollectionUtils.isEmpty(userIds)) {
            return userRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));
        }
        return userRepository.findAllByIdNotInOrderByUsernameAsc(userIds);
    }

    private void getOrgAllParentId(List<String> orgAllParentIds, String orgId) {
        Org org = orgRepository.findById(orgId).orElseThrow();
        orgAllParentIds.add(orgId);
        if (ORG_ROOT.equals(org.getParentId())) {
            return;
        }
        getOrgAllParentId(orgAllParentIds, org.getParentId());
    }

    private void getOrgAllChildId(List<String> orgAllChildIds, String orgId) {
        List<Org> childOrgList = orgRepository.findAllByParentId(orgId);
        if (CollectionUtils.isEmpty(childOrgList)) {
            return;
        }
        for (Org org : childOrgList) {
            orgAllChildIds.add(org.getId());
            getOrgAllChildId(orgAllChildIds, org.getId());
        }
    }

    public void addOrgUser(String orgId, List<String> userIds) {
        List<UserInfo> users = getNotExitsOrgUser(orgId);
        List<String> uIds = users.stream().map(UserInfo::getId).toList();
        for (String userId : userIds) {
            if (!uIds.contains(userId)) {
                throw new HttpException(I18n.get("existUserAnotherOrg"));
            }
        }
        List<OrgUser> orgUsers = new ArrayList<>(userIds.size());
        for (String userId : userIds) {
            OrgUser orgUser = new OrgUser();
            orgUser.setOrgId(orgId);
            orgUser.setUserId(userId);
            orgUsers.add(orgUser);
        }
        orgUserRepository.saveAll(orgUsers);
    }

    public void deleteOrgUser(String orgId, String userId) {
        if (!StringUtils.hasLength(orgId) || !StringUtils.hasLength(userId)) {
            return;
        }
        OrgUser orgUser = new OrgUser();
        orgUser.setUserId(userId);
        orgUser.setOrgId(orgId);
        orgUserRepository.delete(orgUser);
    }

}
