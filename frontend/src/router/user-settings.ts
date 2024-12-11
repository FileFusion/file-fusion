import IconPersonalPrivacy from '~icons/icon-park-outline/personal-privacy';
import IconWrongUser from '~icons/icon-park-outline/wrong-user';
import IconUser from '~icons/icon-park-outline/user';
import IconEditName from '~icons/icon-park-outline/edit-name';

export default [
  {
    path: 'profile',
    name: 'profile',
    meta: {
      requiresAuth: true,
      title: 'userSettings.profile.title',
      icon: IconUser
    },
    component: () => import('@/views/user-settings/ProfileTabs.vue'),
    redirect: '/user-settings/profile/user-info',
    children: [
      {
        path: 'user-info',
        name: 'user-info',
        meta: {
          requiresAuth: true,
          title: 'userSettings.profile.accountInfo',
          icon: IconEditName
        },
        component: () => import('@/views/user-settings/profile/UserInfo.vue')
      },
      {
        path: 'change-password',
        name: 'change-password',
        meta: {
          requiresAuth: true,
          title: 'userSettings.profile.changePassword',
          icon: IconPersonalPrivacy,
          permission: 'user:change_password'
        },
        component: () =>
          import('@/views/user-settings/profile/ChangePassword.vue')
      },
      {
        path: 'account-status',
        name: 'account-status',
        meta: {
          requiresAuth: true,
          title: 'userSettings.profile.accountStatus.title',
          icon: IconWrongUser
        },
        component: () =>
          import('@/views/user-settings/profile/AccountStatus.vue')
      }
    ]
  }
];
