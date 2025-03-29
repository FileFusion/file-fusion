<template>
  <n-modal v-model:show="show" class="!h-80vh !w-80vw">
    <div id="player"></div>
  </n-modal>
</template>

<script lang="ts" setup>
import type { MediaPlayerElement } from 'vidstack/elements';
import { ref, watch } from 'vue';
import { useRequest } from 'alova/client';

const http = window.$http;

const show = defineModel<boolean>('show');
const props = defineProps({
  id: { type: String, required: true },
  name: { type: String, required: true }
});

const player = ref<MediaPlayerElement | null>(null);

const { data: fileUrl, send: doGetFile } = useRequest(
  () => http.Post<any>('/file_data/_submit_download', [props.id]),
  { immediate: false }
);

watch(show, async (newShow) => {
  if (newShow) {
    await doGetFile();
    const { VidstackPlayer, VidstackPlayerLayout } = await import(
      'vidstack/global/player'
    );
    await import('vidstack/player/styles/default/theme.css');
    await import('vidstack/player/styles/default/layouts/video.css');
    player.value = await VidstackPlayer.create({
      target: '#player',
      title: props.name,
      src:
        http.options.baseURL +
        '/file_data/_download_chunked/' +
        fileUrl.value +
        '/' +
        props.name,
      layout: new VidstackPlayerLayout(),
      playsInline: true,
      crossOrigin: true,
      viewType: 'video',
      storage: 'media-player-config'
    });
  } else {
    if (player.value) {
      player.value.destroy();
      player.value = null;
      fileUrl.value = null;
    }
  }
});
</script>
