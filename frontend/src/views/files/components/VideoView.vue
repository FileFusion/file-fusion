<template>
  <n-modal
    v-model:show="model"
    class="h-80vh w-80vw"
    preset="card"
    :title="props.file.name"
    :bordered="false"
    content-style="padding: 0;"
    @after-enter="initPlayer"
    @after-leave="destroyPlayer">
    <div ref="playerContainer"></div>
  </n-modal>
</template>

<script lang="ts" setup>
import type PresetPlayer from 'xgplayer';
import Player from 'xgplayer';
import Mp4Plugin from 'xgplayer-mp4';
import 'xgplayer/dist/index.min.css';
import { ref, onBeforeUnmount } from 'vue';
import { useThemeVars } from 'naive-ui';
import { mainStore } from '@/store';

const themeVars = useThemeVars();
const http = window.$http;
const mStore = mainStore();

const model = defineModel<boolean>();
const props = defineProps({
  file: { type: Object, required: true }
});

const playerContainer = ref<HTMLElement | undefined>(undefined);
const playerInstance = ref<PresetPlayer | null>(null);

function initPlayer() {
  if (!playerContainer.value) {
    return;
  }
  if (playerInstance.value) {
    destroyPlayer();
  }
  playerInstance.value = new Player({
    el: playerContainer.value,
    url: http.options.baseURL + '/file_data/_download_chunked',
    height: '100%',
    width: '100%',
    videoFillMode: 'contain',
    lang: 'zh-cn',
    pip: true,
    commonStyle: {
      playedColor: themeVars.value.primaryColor,
      volumeColor: themeVars.value.primaryColor
    },
    plugins: [Mp4Plugin],
    mp4plugin: {
      reqOptions: {
        mode: 'cors',
        method: 'POST',
        headers: {
          'Accept-Language': mStore.getLanguage,
          Authorization: mStore.getToken,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          path: props.file.path
        })
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

onBeforeUnmount(() => {
  destroyPlayer();
});
</script>

<style>
.xgplayer .xg-options-list li.selected {
  color: var(--primary-color);
}
</style>
