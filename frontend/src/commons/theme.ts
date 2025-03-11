import { useOsTheme } from 'naive-ui';

export enum SUPPORT_THEMES {
  SYNC_SYSTEM = 'syncSystem',
  LIGHT = 'light',
  DARK = 'dark'
}

function getDefaultTheme(): SUPPORT_THEMES {
  const theme = <SUPPORT_THEMES>localStorage.getItem('theme');
  return Object.values(SUPPORT_THEMES).includes(theme)
    ? theme
    : SUPPORT_THEMES.SYNC_SYSTEM;
}

export function getTheme(theme: SUPPORT_THEMES): SUPPORT_THEMES {
  if (theme !== SUPPORT_THEMES.SYNC_SYSTEM) {
    return theme;
  }
  const osTheme = useOsTheme().value;
  return osTheme === SUPPORT_THEMES.DARK
    ? SUPPORT_THEMES.DARK
    : SUPPORT_THEMES.LIGHT;
}

const ICON_MAP = {
  icon: {
    selector: 'link[rel="icon"]',
    paths: {
      [SUPPORT_THEMES.DARK]: '/favicon-white.svg',
      [SUPPORT_THEMES.LIGHT]: '/favicon.svg'
    }
  },
  shortcutIcon: {
    selector: 'link[rel="shortcut icon"]',
    paths: {
      [SUPPORT_THEMES.DARK]: '/favicon-white.ico',
      [SUPPORT_THEMES.LIGHT]: '/favicon.ico'
    }
  },
  appleTouchIconPrecomposed: {
    selector: 'link[rel="apple-touch-icon-precomposed"]',
    paths: {
      [SUPPORT_THEMES.DARK]: '/icon72x72@2x-white.png',
      [SUPPORT_THEMES.LIGHT]: '/icon72x72@2x.png'
    }
  }
};

export function osThemeChange(theme: string | null) {
  const targetTheme =
    theme === SUPPORT_THEMES.DARK ? SUPPORT_THEMES.DARK : SUPPORT_THEMES.LIGHT;
  Object.values(ICON_MAP).forEach(({ selector, paths }) => {
    const icon = <HTMLLinkElement>document.querySelector(selector);
    if (icon) {
      icon.href = paths[targetTheme];
    }
  });
}

export function themeChange(theme: SUPPORT_THEMES) {
  document.documentElement.setAttribute('class', theme + '-theme');
}

export const defaultTheme: SUPPORT_THEMES = getDefaultTheme();
