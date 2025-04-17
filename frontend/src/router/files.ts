import IconLog from '~icons/icon-park-outline/log';
import IconDeleteThemes from '~icons/icon-park-outline/delete-themes';

export default [
  {
    path: 'personal/:parentId?',
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
    path: 'recycle-bin',
    name: 'files-recycle-bin',
    meta: {
      requiresAuth: true,
      title: 'files.recycleBinFile',
      icon: IconDeleteThemes,
      permission: 'recycle_bin_file:read',
      show: async (): Promise<boolean> => {
        const response = await window.$http.Get<any>(
          '/sys-config?configKey=RECYCLE_BIN'
        );
        return response.configValue === 'true';
      }
    },
    component: () => import('@/views/files/RecycleBinFile.vue')
  }
];
