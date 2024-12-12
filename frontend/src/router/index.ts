import type { Component } from 'vue';
import type { RouteLocationNormalized } from 'vue-router';
import { createRouter, createWebHistory } from 'vue-router';
import IconDocumentFolder from '~icons/icon-park-outline/document-folder';
import IconPeople from '~icons/icon-park-outline/people';
import IconSettingTwo from '~icons/icon-park-outline/setting-two';
import { mainStore } from '@/store';
import { hasPermission } from '@/commons/permission';
import routerViewContent from '@/components/RouterViewContent.vue';
import files from '@/router/files';
import systemSettings from '@/router/system-settings';
import userSettings from '@/router/user-settings';

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth: boolean;
    title?: string;
    icon?: Component;
    permission?: string[] | string;
    permissionOr?: boolean;
  }
}

const routes = [
  {
    path: '/',
    name: 'home',
    meta: { requiresAuth: true },
    component: () => import('@/views/HomeView.vue'),
    redirect: '/files',
    children: [
      {
        path: '/files',
        name: 'files',
        meta: {
          requiresAuth: true,
          title: 'common.files',
          icon: IconDocumentFolder
        },
        component: routerViewContent,
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
        component: routerViewContent,
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
        component: routerViewContent,
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

router.beforeEach((to: RouteLocationNormalized) => {
  const mStore = mainStore(window.$pinia);
  const token = mStore.getToken;
  if (to.meta.requiresAuth) {
    if (!token) {
      return {
        path: '/login',
        query: { redirect: to.path }
      };
    }
    if (to.meta.permission) {
      if (!hasPermission(to.meta.permission, to.meta.permissionOr)) {
        return { path: '/' };
      }
    }
  } else {
    if (to.name === 'login' && token) {
      window.$msg.success(window.$t('router.isLogin'));
      return { path: '/' };
    }
  }
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
