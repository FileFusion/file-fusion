<template>
  <div class="flex items-center justify-end gap-3">
    <n-dropdown
      :options="languagesOptions"
      :show-arrow="true"
      trigger="hover"
      @select="switchLanguage">
      <n-button text>
        <n-icon :size="20">
          <i-text />
        </n-icon>
      </n-button>
    </n-dropdown>
    <n-dropdown
      :options="themeOptions"
      :show-arrow="true"
      trigger="hover"
      @select="switchTheme">
      <n-button text>
        <n-icon :size="20">
          <i-contrast />
        </n-icon>
      </n-button>
    </n-dropdown>
    <slot></slot>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import IconEnglish from '~icons/icon-park-outline/english';
import IconChinese from '~icons/icon-park-outline/chinese';
import IconSun from '~icons/icon-park-outline/sun';
import IconMoon from '~icons/icon-park-outline/moon';
import IconIntermediateMode from '~icons/icon-park-outline/intermediate-mode';
import { mainStore } from '@/store';
import { renderIconMethod } from '@/commons/utils.ts';
import { SUPPORT_LANGUAGES } from '@/commons/i18n.ts';
import { SUPPORT_THEMES } from '@/commons/theme.ts';

const mStore = mainStore();
const { t } = useI18n();

const languagesOptions = [
  {
    label: 'English',
    key: SUPPORT_LANGUAGES.EN_US,
    icon: renderIconMethod(IconEnglish)
  },
  {
    label: '中文',
    key: SUPPORT_LANGUAGES.ZH_CN,
    icon: renderIconMethod(IconChinese)
  }
];

const themeOptions = computed(() => [
  {
    label: t('theme.syncSystem'),
    key: SUPPORT_THEMES.SYNC_SYSTEM,
    icon: renderIconMethod(IconIntermediateMode)
  },
  {
    label: t('theme.light'),
    key: SUPPORT_THEMES.LIGHT,
    icon: renderIconMethod(IconSun)
  },
  {
    label: t('theme.dark'),
    key: SUPPORT_THEMES.DARK,
    icon: renderIconMethod(IconMoon)
  }
]);

function switchLanguage(language: SUPPORT_LANGUAGES) {
  mStore.setLanguage(language);
}

function switchTheme(theme: SUPPORT_THEMES) {
  mStore.setTheme(theme);
}
</script>
