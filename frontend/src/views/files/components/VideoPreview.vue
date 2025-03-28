<template>
  <n-modal v-model:show="show" class="!h-80vh !w-80vw">
    <div id="player"></div>
  </n-modal>
</template>

<script lang="ts" setup>
import type { MediaProviderChangeEvent } from 'vidstack';
import type { MediaPlayerElement } from 'vidstack/elements';
import 'vidstack/player/styles/base.css';
import 'vidstack/player/styles/plyr/theme.css';
import HLS from 'hls.js';
import { VidstackPlayer, PlyrLayout } from 'vidstack/global/player';
import { isHLSProvider } from 'vidstack';
import { computed, nextTick, ref, watch } from 'vue';
import { mainStore } from '@/store';

const http = window.$http;
const mStore = mainStore();

const show = defineModel<boolean>('show');
const props = defineProps({
  id: { type: String, required: true },
  name: { type: String, required: true }
});

const token = computed(() => mStore.getToken);
const player = ref<MediaPlayerElement>();

function onProviderChange(event: MediaProviderChangeEvent) {
  const provider = event.detail;
  if (isHLSProvider(provider)) {
    provider.library = HLS;
    provider.config = {
      xhrSetup(xhr) {
        xhr.setRequestHeader('Authorization', <string>token.value);
      }
    };
  }
}

watch(show, async (newShow) => {
  if (newShow) {
    await nextTick();
    player.value = await VidstackPlayer.create({
      target: '#player',
      title: props.name,
      src: http.options.baseURL + '/file_data/' + props.id + '/master.m3u8',
      layout: new PlyrLayout(),
      playsInline: true,
      crossOrigin: true,
      viewType: 'video',
      streamType: 'on-demand',
      storage: 'media-player-config'
    });
    player.value.addEventListener('provider-change', onProviderChange);
  }
});
</script>
