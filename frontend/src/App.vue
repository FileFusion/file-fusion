<template>
  <suspense>
    <n-config-provider
      :date-locale="uiLanguage.date"
      :locale="uiLanguage.language"
      :theme="uiTheme"
      :theme-overrides="uiTheme === null ? lightThemeParams : darkThemeParams"
      class="h-full">
      <n-notification-provider :max="3">
        <notice-view></notice-view>
      </n-notification-provider>
      <n-dialog-provider>
        <dialog-view></dialog-view>
      </n-dialog-provider>
      <n-loading-bar-provider>
        <loading-bar-view></loading-bar-view>
      </n-loading-bar-provider>
      <router-view-content></router-view-content>
      <n-global-style></n-global-style>
    </n-config-provider>
  </suspense>
</template>

<script lang="ts" setup>
import {
  darkTheme,
  dateEnUS,
  dateZhCN,
  enUS,
  zhCN,
  useOsTheme
} from 'naive-ui';
import { computed, nextTick, onMounted, watch } from 'vue';
import { mainStore } from '@/store';
import lightThemeParams from '@/assets/themes/light.json';
import darkThemeParams from '@/assets/themes/dark.json';
import { languageChange, SUPPORT_LANGUAGES } from '@/commons/i18n.ts';
import { themeChange, osThemeChange, SUPPORT_THEMES } from '@/commons/theme.ts';

const mStore = mainStore();
const actualTheme = useOsTheme();
const theme = computed(() => mStore.getTheme);
const language = computed(() => mStore.getLanguage);

const uiLanguage = computed(() => {
  if (language.value === SUPPORT_LANGUAGES.ZH_CN) {
    return {
      language: zhCN,
      date: dateZhCN
    };
  } else {
    return {
      language: enUS,
      date: dateEnUS
    };
  }
});

const uiTheme = computed(() => {
  if (theme.value === SUPPORT_THEMES.DARK) {
    return darkTheme;
  } else {
    return null;
  }
});

onMounted(async () => {
  await nextTick();
  const splash = document.getElementById('splash');
  if (splash) {
    splash.remove();
  }
});

watch(actualTheme, osThemeChange, { immediate: true });
watch(theme, themeChange, { immediate: true });
watch(language, languageChange, { immediate: true });
</script>
