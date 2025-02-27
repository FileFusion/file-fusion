import IconLog from '~icons/icon-park-outline/log';
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
    path: 'recycle-bin/:path*',
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
