import { useOsTheme } from 'naive-ui';

export enum SUPPORT_THEMES {
  SYNC_SYSTEM = 'syncSystem',
  LIGHT = 'light',
  DARK = 'dark'
}

function getDefaultTheme(): SUPPORT_THEMES {
  const theme = localStorage.getItem('theme');
  if (theme) {
    for (const supportTheme of Object.values(SUPPORT_THEMES)) {
      if (theme === supportTheme) {
        return supportTheme;
      }
    }
  }
  return SUPPORT_THEMES.SYNC_SYSTEM;
}

export function getTheme(theme: SUPPORT_THEMES): SUPPORT_THEMES {
  if (theme === SUPPORT_THEMES.LIGHT || theme === SUPPORT_THEMES.DARK) {
    return theme;
  } else if (theme === SUPPORT_THEMES.SYNC_SYSTEM) {
    const osThemeRef = useOsTheme();
    return osThemeRef.value === SUPPORT_THEMES.DARK
      ? SUPPORT_THEMES.DARK
      : SUPPORT_THEMES.LIGHT;
  }
  return SUPPORT_THEMES.LIGHT;
}

export function osThemeChange(theme: string | null) {
  const at =
    theme === SUPPORT_THEMES.DARK ? SUPPORT_THEMES.DARK : SUPPORT_THEMES.LIGHT;
  const icon = document.querySelector('link[rel="icon"]');
  if (icon) {
    icon.setAttribute(
      'href',
      at === SUPPORT_THEMES.DARK ? '/favicon-white.svg' : '/favicon.svg'
    );
  }
  const shortcutIcon = document.querySelector('link[rel="shortcut icon"]');
  if (shortcutIcon) {
    shortcutIcon.setAttribute(
      'href',
      at === SUPPORT_THEMES.DARK ? '/favicon-white.ico' : '/favicon.ico'
    );
  }
  const appleTouchIconPrecomposed = document.querySelector(
    'link[rel="apple-touch-icon-precomposed"]'
  );
  if (appleTouchIconPrecomposed) {
    appleTouchIconPrecomposed.setAttribute(
      'href',
      at === SUPPORT_THEMES.DARK
        ? '/icon72x72@2x-white.png'
        : '/icon72x72@2x.png'
    );
  }
}

export const DefaultTheme = getDefaultTheme();
