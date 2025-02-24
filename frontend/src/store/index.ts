import type { SUPPORT_LANGUAGES } from '@/commons/i18n';
import type { SUPPORT_THEMES } from '@/commons/theme';
import { defineStore } from 'pinia';
import { defaultLanguage } from '@/commons/i18n';
import { defaultTheme, getTheme } from '@/commons/theme';
import route, { getRouteTitle } from '@/router';

interface MainState {
  language: SUPPORT_LANGUAGES;
  theme: SUPPORT_THEMES;
  sideMenuCollapsed: string | null;
  token: string | null;
  user: any | null;
  permissionIds: string[] | null;
}

export const mainStore = defineStore('main', {
  state: (): MainState => ({
    language: defaultLanguage,
    theme: defaultTheme,
    sideMenuCollapsed: localStorage.getItem('sideMenuCollapsed'),
    token: localStorage.getItem('token') || sessionStorage.getItem('token'),
    user: null,
    permissionIds: null
  }),
  getters: {
    getLanguage(state): SUPPORT_LANGUAGES {
      return state.language;
    },
    getTheme(state): SUPPORT_THEMES {
      return getTheme(state.theme);
    },
    getSideMenuCollapsed(state): boolean {
      if (state.sideMenuCollapsed === null) {
        return false;
      }
      return state.sideMenuCollapsed === 'true';
    },
    getToken(state): string | null {
      return state.token;
    },
    getUser(state): any | null {
      return state.user;
    },
    getPermissionIds(state): any[] | null {
      return state.permissionIds;
    }
  },
  actions: {
    setLanguage(language: SUPPORT_LANGUAGES) {
      this.language = language;
      localStorage.setItem('language', language);
      document.documentElement.setAttribute('lang', language);
      document.title = getRouteTitle(route.currentRoute.value);
    },
    setTheme(theme: SUPPORT_THEMES) {
      this.theme = theme;
      localStorage.setItem('theme', theme);
      document.documentElement.setAttribute(
        'class',
        getTheme(theme) + '-theme'
      );
    },
    setSideMenuCollapsed(sideMenuCollapsed: boolean) {
      this.sideMenuCollapsed = '' + sideMenuCollapsed;
      localStorage.setItem('sideMenuCollapsed', this.sideMenuCollapsed);
    },
    setToken(token: string | null, remember?: boolean) {
      this.token = token;
      if (token) {
        if (remember) {
          localStorage.setItem('token', token);
        } else {
          sessionStorage.setItem('token', token);
        }
      } else {
        this.user = null;
        this.permissionIds = null;
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
      }
    },
    setUser(user: any) {
      this.permissionIds = user.permissions.map((permission: any) => {
        return permission.id;
      });
      user.permissions = null;
      this.user = user;
    }
  }
});
