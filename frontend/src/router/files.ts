import IconBuildingOne from '~icons/icon-park-outline/building-one';
import IconPermissions from '~icons/icon-park-outline/permissions';
import IconPeoples from '~icons/icon-park-outline/peoples';

export default [
  {
    path: 'personal',
    name: 'files-personal',
    meta: {
      requiresAuth: true,
      title: 'common.org',
      icon: IconBuildingOne,
      permission: 'personal_file:read'
    },
    component: () => import('@/views/files/PersonalFile.vue')
  },
  {
    path: 'org',
    name: 'files-org',
    meta: {
      requiresAuth: true,
      title: 'common.role',
      icon: IconPermissions,
      permission: 'org_file:read'
    },
    component: () => import('@/views/files/OrgFile.vue')
  },
  {
    path: 'recycle-bin',
    name: 'files-recycle-bin',
    meta: {
      requiresAuth: true,
      title: 'common.user',
      icon: IconPeoples,
      permission: 'recycle_bin_file:read'
    },
    component: () => import('@/views/files/RecycleBinFile.vue')
  }
];
