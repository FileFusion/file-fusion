<template>
  <n-modal v-model:show="show">
    <n-spin :show="previewFileLoading">
      <audio v-if="fileUrl && mimeType" controls preload="metadata">
        <source :src="fileUrl" :type="mimeType" />
      </audio>
    </n-spin>
  </n-modal>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRequest } from 'alova/client';

const http = window.$http;

const show = defineModel<boolean>('show');
const id = defineModel<string | null>('id');
const mimeType = defineModel<string | null>('mimeType');

const fileUrl = ref<string | null>(null);

const {
  loading: previewFileLoading,
  data: previewUrl,
  send: doGetPreviewFile
} = useRequest(
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
