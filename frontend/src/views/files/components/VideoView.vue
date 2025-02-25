<template>
  <n-modal
    v-model:show="model"
    style="width: 80vw; height: 80vh"
    preset="card"
    :title="props.file.name"
    :bordered="false"
    content-style="padding: 0;"
    @after-enter="doDownloadFile"
    @after-leave="destroyPlayer">
    <n-spin :show="downloadFileLoading" class="h-full w-full">
      <div ref="playerContainer"></div>
    </n-spin>
  </n-modal>
</template>

<script lang="ts" setup>
import type PresetPlayer from 'xgplayer';
import Player from 'xgplayer';
import 'xgplayer/dist/index.min.css';
import { ref, onBeforeUnmount } from 'vue';
import { useThemeVars } from 'naive-ui';
import { useRequest } from 'alova/client';

const themeVars = useThemeVars();
const http = window.$http;

const model = defineModel<boolean>();
const props = defineProps({
  file: { type: Object, required: true }
});

const playerContainer = ref<HTMLElement | undefined>(undefined);
const playerInstance = ref<PresetPlayer | null>(null);

const { loading: downloadFileLoading, send: doDownloadFile } = useRequest(
  () => http.Post('/file_data/_submit_download', [props.file.path]),
  { immediate: false }
).onSuccess((response: any) => {
  initPlayer(
    http.options.baseURL + '/file_data/_download/' + response.data.downloadId
  );
});

function initPlayer(url: string) {
  if (!playerContainer.value) {
    return;
  }
  if (playerInstance.value) {
    destroyPlayer();
  }
  playerInstance.value = new Player({
    el: playerContainer.value,
    url: url,
    height: '100%',
    width: '100%',
    videoFillMode: 'contain',
    lang: 'zh-cn',
    pip: true,
    commonStyle: {
      playedColor: themeVars.value.primaryColor,
      volumeColor: themeVars.value.primaryColor
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
