import IconBuildingOne from '~icons/icon-park-outline/building-one';
import IconPermissions from '~icons/icon-park-outline/permissions';
import IconPeoples from '~icons/icon-park-outline/peoples';

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
  }
];
