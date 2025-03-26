<template>
  <n-modal v-model:show="show">
    <media-player :title="<string>name" :src="<string>fileUrl" playsInline>
      <media-provider></media-provider>
      <media-video-layout></media-video-layout>
    </media-player>
  </n-modal>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRequest } from 'alova/client';
import 'vidstack/bundle';

const http = window.$http;

const show = defineModel<boolean>('show');
const id = defineModel<string | null>('id');
const name = defineModel<string | null>('name');

const fileUrl = ref<string | null>(null);

const { data: previewUrl, send: doGetPreviewFile } = useRequest(
  () => http.Post<any>('/file_data/_submit_download', [id.value]),
  { immediate: false }
);

watch(show, async (newShow) => {
  if (newShow) {
    await doGetPreviewFile();
    fileUrl.value =
      http.options.baseURL + '/file_data/_download/' + previewUrl.value;
  } else {
    fileUrl.value = null;
  }
});
</script>
