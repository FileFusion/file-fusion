<template>
  <n-modal v-model:show="show" :class="isAudio ? 'w-80' : '!h-80vh !w-80vw'">
    <div id="player"></div>
  </n-modal>
</template>

<script lang="ts" setup>
import type { MediaPlayerElement } from 'vidstack/elements';
import { computed, ref, watch } from 'vue';
import { useRequest } from 'alova/client';
import { mainStore } from '@/store';
import { supportAudioPreview } from '@/commons/file.ts';
import { SUPPORT_LANGUAGES } from '@/commons/i18n.ts';
import enUS from '@/assets/languages/media-player/en-US.ts';
import zhCN from '@/assets/languages/media-player/zh-CN.ts';

const mStore = mainStore();
const http = window.$http;
const language = computed(() => mStore.getLanguage);

const show = defineModel<boolean>('show');
const props = defineProps({
  id: { type: String, required: true },
  name: { type: String, required: true },
  mimeType: { type: String, required: true }
});

const player = ref<MediaPlayerElement | null>(null);
const playerLanguage = computed(() => {
  return language.value === SUPPORT_LANGUAGES.ZH_CN ? zhCN : enUS;
});
const isAudio = computed(() => supportAudioPreview(props.mimeType));
const playUrl = computed(() => {
  if (isAudio.value) {
    return (
      http.options.baseURL +
      '/file_data/_download_chunked/' +
      downloadId.value +
      '/' +
      props.name
    );
  } else {
    return (
      http.options.baseURL +
      '/file_data/video/' +
      videoDownloadId.value +
      '/stream.mpd'
    );
  }
});

const { data: downloadId, send: doGetFile } = useRequest(
  () => http.Post<any>('/file_data/_submit_download', [props.id]),
  { immediate: false }
);

const { data: videoDownloadId, send: doGetVideoFile } = useRequest(
  () => http.Post<any>('/file_data/video/' + props.id + '/_submit_preview'),
  { immediate: false }
);

watch(show, async (newShow) => {
  if (newShow) {
    if (isAudio.value) {
      await doGetFile();
    } else {
      await doGetVideoFile();
    }
    const { VidstackPlayer, VidstackPlayerLayout } = await import(
      'vidstack/global/player'
    );
    await import('vidstack/player/styles/default/theme.css');
    if (isAudio.value) {
      await import('vidstack/player/styles/default/layouts/audio.css');
    } else {
      await import('vidstack/player/styles/default/layouts/video.css');
    }
    player.value = await VidstackPlayer.create({
      target: '#player',
      title: props.name,
      src: playUrl.value,
      viewType: isAudio.value ? 'audio' : 'video',
      streamType: 'on-demand',
      layout: new VidstackPlayerLayout({
        translations: playerLanguage.value
      }),
      crossOrigin: true,
      playsInline: true,
      storage: 'media-player-config'
    });
  } else {
    if (player.value) {
      player.value.destroy();
      player.value = null;
      downloadId.value = null;
      videoDownloadId.value = null;
    }
  }
});
</script>
