import type { SUPPORT_LANGUAGES } from '@/commons/i18n';
import type { SUPPORT_THEMES } from '@/commons/theme';
import { defineStore } from 'pinia';
import { defaultLanguage } from '@/commons/i18n';
import { defaultTheme, getTheme } from '@/commons/theme';

interface MainState {
  language: SUPPORT_LANGUAGES;
  theme: SUPPORT_THEMES;
  sideMenuCollapsed: string | null;
  token: string | null;
  user: any | null;
  permissionIds: string[] | null;
  fileShowType: string | null;
}

export const mainStore = defineStore('main', {
  state: (): MainState => ({
    language: defaultLanguage,
    theme: defaultTheme,
    sideMenuCollapsed: localStorage.getItem('sideMenuCollapsed'),
    token: localStorage.getItem('token') || sessionStorage.getItem('token'),
    user: null,
    permissionIds: null,
    fileShowType: localStorage.getItem('fileShowType')
  }),
  getters: {
    getLanguage(state): SUPPORT_LANGUAGES {
      return state.language;
    },
    getTheme(state): SUPPORT_THEMES {
      return getTheme(state.theme);
    },
    getSideMenuCollapsed(state): boolean {
      return state.sideMenuCollapsed === 'true';
    },
    getToken(state): string | null {
      return state.token;
    },
    getUser(state): any | null {
      return state.user;
    },
    getPermissionIds(state): string[] | null {
      return state.permissionIds;
    },
    getFileShowType(state): string {
      return state.fileShowType ? state.fileShowType : 'grid';
    }
  },
  actions: {
    setLanguage(language: SUPPORT_LANGUAGES) {
      this.language = language;
      localStorage.setItem('language', language);
    },
    setTheme(theme: SUPPORT_THEMES) {
      this.theme = theme;
      localStorage.setItem('theme', theme);
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
      this.permissionIds = user.permissionIds;
      user.permissionIds = null;
      this.user = user;
    },
    setFileShowType(fileShowType: string) {
      this.fileShowType = fileShowType;
      localStorage.setItem('fileShowType', this.fileShowType);
    }
  }
});
