import { useOsTheme } from 'naive-ui';

export enum SupportThemes {
  SYNC_SYSTEM = 'syncSystem',
  LIGHT = 'light',
  DARK = 'dark'
}

function getDefaultTheme(): SupportThemes {
  const theme = localStorage.getItem('theme');
  if (theme) {
    for (const supportTheme of Object.values(SupportThemes)) {
      if (theme === supportTheme) {
        return supportTheme;
      }
    }
  }
  return SupportThemes.SYNC_SYSTEM;
}

export function getTheme(theme: SupportThemes): SupportThemes {
  if (theme === SupportThemes.LIGHT || theme === SupportThemes.DARK) {
    return theme;
  } else if (theme === SupportThemes.SYNC_SYSTEM) {
    const osThemeRef = useOsTheme();
    return osThemeRef.value === SupportThemes.DARK
      ? SupportThemes.DARK
      : SupportThemes.LIGHT;
  }
  return SupportThemes.LIGHT;
}

export function osThemeChange(theme: string | null) {
  const at =
    theme === SupportThemes.DARK ? SupportThemes.DARK : SupportThemes.LIGHT;
  const icon = document.querySelector('link[rel="icon"]');
  if (icon) {
    icon.setAttribute(
      'href',
      at === SupportThemes.LIGHT ? '/favicon.svg' : '/favicon-white.svg'
    );
  }
  const shortcutIcon = document.querySelector('link[rel="shortcut icon"]');
  if (shortcutIcon) {
    shortcutIcon.setAttribute(
      'href',
      at === SupportThemes.LIGHT ? '/favicon.ico' : '/favicon-white.ico'
    );
  }
  const appleTouchIconPrecomposed = document.querySelector(
    'link[rel="apple-touch-icon-precomposed"]'
  );
  if (appleTouchIconPrecomposed) {
    appleTouchIconPrecomposed.setAttribute(
      'href',
      at === SupportThemes.LIGHT
        ? '/icon72x72@2x.png'
        : '/icon72x72@2x-white.png'
    );
  }
}

export const DefaultTheme = getDefaultTheme();
