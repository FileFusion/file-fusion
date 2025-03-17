import type { Component } from 'vue';
import type { RouteLocationNormalized } from 'vue-router';
import type { PermissionType } from '@/commons/permission';
import { createRouter, createWebHistory } from 'vue-router';
import IconDocumentFolder from '~icons/icon-park-outline/document-folder';
import IconPeople from '~icons/icon-park-outline/people';
import IconSettingTwo from '~icons/icon-park-outline/setting-two';
import { mainStore } from '@/store';
import { hasPermission } from '@/commons/permission';
import files from '@/router/files';
import systemSettings from '@/router/system-settings';
import userSettings from '@/router/user-settings';

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth: boolean;
    title?: string;
    icon?: Component;
    permission?: PermissionType;
    permissionOr?: boolean;
  }
}

const routes = [
  {
    path: '/',
    name: 'home',
    meta: { requiresAuth: true },
    component: () => import('@/views/HomeView.vue'),
    children: [
      {
        path: '/files',
        name: 'files',
        meta: {
          requiresAuth: true,
          title: 'common.files',
          icon: IconDocumentFolder,
          permission: ['personal_file:read', 'recycle_bin_file:read'],
          permissionOr: true
        },
        children: files
      },
      {
        path: '/system-settings',
        name: 'system-settings',
        meta: {
          requiresAuth: true,
          title: 'common.setting',
          icon: IconSettingTwo,
          permission: ['org:read', 'role:read', 'user_management:read'],
          permissionOr: true
        },
        children: systemSettings
      },
      {
        path: '/user-settings',
        name: 'user-settings',
        meta: {
          requiresAuth: true,
          title: 'userSettings.title',
          icon: IconPeople
        },
        children: userSettings
      }
    ]
  },
  {
    path: '/login',
    name: 'login',
    meta: { requiresAuth: false },
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/404',
    name: '404',
    meta: {
      requiresAuth: false,
      title: 'noFound.title'
    },
    component: () => import('@/views/NoFoundView.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: {
      requiresAuth: false
    }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes: routes
});

router.beforeEach(async (to: RouteLocationNormalized) => {
  const mStore = mainStore(window.$pinia);
  const token = mStore.getToken;
  if (!to.meta.requiresAuth) {
    if (to.name === 'login' && token) {
      window.$msg.success(window.$t('router.isLogin'));
      return { path: '/' };
    }
    return true;
  }
  if (!token) {
    return {
      path: '/login',
      query: { redirect: to.path }
    };
  }
  if (!mStore.getUser) {
    const user: any = await window.$http.Get<any>('/user/current');
    mStore.setUser(user);
    const permissionIds: any = await window.$http.Get<any>(
      '/user/current/permission'
    );
    mStore.setPermissionIds(permissionIds);
  }
  if (
    to.meta.permission &&
    !hasPermission(to.meta.permission, to.meta.permissionOr)
  ) {
    return { path: '/' };
  }
  return true;
});

router.afterEach((to: RouteLocationNormalized) => {
  document.title = getRouteTitle(to);
});

export function getRouteTitle(to: RouteLocationNormalized) {
  for (let i = to.matched.length; i--; i >= 0) {
    const title = to.matched[i].meta.title;
    if (title) {
      return window.$t(title) + ' - File Fusion';
    }
  }
  return 'File Fusion';
}

export default router;
