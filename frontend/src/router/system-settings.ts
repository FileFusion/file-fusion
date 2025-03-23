import IconBuildingOne from '~icons/icon-park-outline/building-one';
import IconPermissions from '~icons/icon-park-outline/permissions';
import IconPeoples from '~icons/icon-park-outline/peoples';
import IconSettingConfig from '~icons/icon-park-outline/setting-config';
import IconDeleteThemes from '~icons/icon-park-outline/delete-themes';

export default [
  {
    path: 'org',
    name: 'system-settings-org',
    meta: {
      requiresAuth: true,
      title: 'common.org',
      icon: IconBuildingOne,
      permission: 'org:read'
    },
    component: () => import('@/views/system-settings/OrgManagement.vue')
  },
  {
    path: 'role',
    name: 'system-settings-role',
    meta: {
      requiresAuth: true,
      title: 'common.role',
      icon: IconPermissions,
      permission: 'role:read'
    },
    component: () => import('@/views/system-settings/RoleManagement.vue')
  },
  {
    path: 'user',
    name: 'system-settings-user',
    meta: {
      requiresAuth: true,
      title: 'common.user',
      icon: IconPeoples,
      permission: 'user_management:read'
    },
    component: () => import('@/views/system-settings/UserManagement.vue')
  },
  {
    path: 'sys-config',
    name: 'system-settings-sys-config',
    meta: {
      requiresAuth: true,
      title: 'common.config',
      icon: IconSettingConfig,
      permission: ['sys_config:recycle_bin_read'],
      permissionOr: true
    },
    component: () => import('@/views/system-settings/SysConfigTabs.vue'),
    children: [
      {
        path: 'recycle-bin',
        name: 'system-settings-sys-config-recycle-bin',
        meta: {
          requiresAuth: true,
          title: 'files.recycleBinFile',
          icon: IconDeleteThemes,
          permission: 'sys_config:recycle_bin_read'
        },
        component: () =>
          import('@/views/system-settings/sys-config/RecycleBin.vue')
      }
    ]
  }
];
