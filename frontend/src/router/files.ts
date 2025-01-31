import IconLog from '~icons/icon-park-outline/log';
import IconTableReport from '~icons/icon-park-outline/table-report';
import IconRecycleBin from '~icons/icon-park-outline/recycle-bin';

export default [
  {
    path: 'personal/:path*',
    name: 'files-personal',
    meta: {
      requiresAuth: true,
      title: 'files.personalFile',
      icon: IconLog,
      permission: 'personal_file:read'
    },
    component: () => import('@/views/files/PersonalFile.vue')
  },
  {
    path: 'org',
    name: 'files-org',
    meta: {
      requiresAuth: true,
      title: 'files.orgFile',
      icon: IconTableReport,
      permission: 'org_file:read'
    },
    component: () => import('@/views/files/OrgFile.vue')
  },
  {
    path: 'recycle-bin',
    name: 'files-recycle-bin',
    meta: {
      requiresAuth: true,
      title: 'files.recycleBin',
      icon: IconRecycleBin,
      permission: 'recycle_bin_file:read'
    },
    component: () => import('@/views/files/RecycleBinFile.vue')
  }
];
