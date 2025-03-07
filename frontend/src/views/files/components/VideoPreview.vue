<template>
  <n-modal v-model:show="model" :bordered="false" content-style="padding: 0;">
    <div ref="playerContainer"></div>
  </n-modal>
</template>

<script lang="ts" setup>
import type PresetPlayer from 'xgplayer';
import Player from 'xgplayer';
import Mp4Plugin from 'xgplayer-mp4';
import 'xgplayer/dist/index.min.css';
import { ref, onBeforeUnmount, computed, watch, nextTick } from 'vue';
import { useThemeVars } from 'naive-ui';
import { mainStore } from '@/store';
import { SUPPORT_LANGUAGES } from '@/commons/i18n.ts';

const themeVars = useThemeVars();
const http = window.$http;
const mStore = mainStore();
const language = computed(() => mStore.getLanguage);
const token = computed(() => mStore.getToken);

const model = defineModel<boolean>();
const id = defineModel<string | null>('id');

const playerContainer = ref<HTMLElement | null>(null);
const playerInstance = ref<PresetPlayer | null>(null);
const playerLanguage = computed(() => {
  if (language.value === SUPPORT_LANGUAGES.ZH_CN) {
    return 'zh-cn';
  }
  return 'en';
});

function initPlayer() {
  if (!playerContainer.value) {
    return;
  }
  if (playerInstance.value) {
    destroyPlayer();
  }
  playerInstance.value = new Player({
    el: playerContainer.value,
    url: http.options.baseURL + '/file_data/_download_chunked/' + id.value,
    height: '75vh',
    width: '75vw',
    videoFillMode: 'contain',
    lang: playerLanguage.value,
    pip: true,
    commonStyle: {
      playedColor: themeVars.value.primaryColor,
      volumeColor: themeVars.value.primaryColor
    },
    plugins: [Mp4Plugin],
    mp4plugin: {
      reqOptions: {
        mode: 'cors',
        method: 'GET',
        headers: {
          'Accept-Language': language.value,
          Authorization: token.value,
          'Content-Type': 'application/json'
        }
      }
    }
  });
}

function destroyPlayer() {
  if (playerInstance.value) {
    playerInstance.value.destroy();
    playerInstance.value = null;
  }
}

watch(model, async (newValue) => {
  if (newValue) {
    await nextTick();
    initPlayer();
  } else {
    destroyPlayer();
  }
});

watch(language, (newValue) => {
  if (playerInstance.value) {
    playerInstance.value.lang = newValue;
  }
});

onBeforeUnmount(() => {
  destroyPlayer();
});
</script>

<style>
.xgplayer .xg-options-list li.selected {
  color: var(--primary-color);
}
</style>
