import { defineStore } from 'pinia';
import { DefaultLanguage, SupportLanguages } from '@/commons/i18n';
import { DefaultTheme, getTheme, SupportThemes } from '@/commons/theme';
import route, { getRouteTitle } from '@/router';

interface MainState {
  language: SupportLanguages;
  theme: SupportThemes;
  sideMenuCollapsed: string | null;
  token: string | null;
  user: any | null;
  permissions: any[] | null;
}

export const mainStore = defineStore('main', {
  state: (): MainState => ({
    language: DefaultLanguage,
    theme: DefaultTheme,
    sideMenuCollapsed: localStorage.getItem('sideMenuCollapsed'),
    token: localStorage.getItem('token') || sessionStorage.getItem('token'),
    user: null,
    permissions: null
  }),
  getters: {
    getLanguage(state): SupportLanguages {
      return state.language;
    },
    getTheme(state): SupportThemes {
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
    getPermissions(state): any[] | null {
      return state.permissions;
    }
  },
  actions: {
    setLanguage(language: SupportLanguages) {
      this.language = language;
      localStorage.setItem('language', language);
      document.documentElement.setAttribute('lang', language);
      document.title = getRouteTitle(route.currentRoute.value);
    },
    setTheme(theme: SupportThemes) {
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
        this.permissions = null;
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
      }
    },
    setUser(user: any) {
      this.permissions = user.permissions;
      user.permissions = null;
      this.user = user;
    }
  }
});
