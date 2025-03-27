<template>
  <n-modal v-model:show="show">
    <media-player
      :plays-inline="true"
      :cross-origin="true"
      :title="<string>name"
      :src="<string>fileUrl"
      @provider-change="onProviderChange">
      <media-provider></media-provider>
      <media-video-layout></media-video-layout>
    </media-player>
  </n-modal>
</template>

<script lang="ts" setup>
import type { MediaProviderChangeEvent } from 'vidstack';
import { computed, ref, watch } from 'vue';
import { isHLSProvider } from 'vidstack';
import 'vidstack/player/styles/default/theme.css';
import 'vidstack/player/styles/default/layouts/audio.css';
import 'vidstack/player/styles/default/layouts/video.css';
import 'vidstack/player';
import 'vidstack/player/layouts';
import 'vidstack/player/ui';
import { mainStore } from '@/store';

const http = window.$http;
const mStore = mainStore();

const show = defineModel<boolean>('show');
const id = defineModel<string | null>('id');
const name = defineModel<string | null>('name');

const token = computed(() => mStore.getToken);
const fileUrl = ref<string | null>(null);

function onProviderChange(event: MediaProviderChangeEvent) {
  const provider = event.detail;
  if (isHLSProvider(provider)) {
    provider.config = {
      xhrSetup(xhr) {
        xhr.setRequestHeader('Authorization', <string>token.value);
      }
    };
  }
}

watch(show, async (newShow) => {
  if (newShow) {
    fileUrl.value =
      http.options.baseURL + '/file_data/' + id.value + '/master.m3u8';
  } else {
    fileUrl.value = null;
  }
});
</script>
