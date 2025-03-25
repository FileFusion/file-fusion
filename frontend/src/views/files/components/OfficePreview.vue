<template>
  <n-modal v-model:show="show">
    <n-spin :show="fileLoading">
      <vue-office-docx
        v-if="
          fileUrl &&
          mimeType ===
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        "
        :src="fileUrl"
        class="!h-80vh !w-80vw"
        @rendered="renderedHandler"
        @error="errorHandler" />
      <vue-office-excel
        v-if="
          fileUrl &&
          mimeType ===
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        "
        :src="fileUrl"
        class="!h-80vh !w-80vw"
        @rendered="renderedHandler"
        @error="errorHandler" />
      <vue-office-pptx
        v-if="
          fileUrl &&
          mimeType ===
            'application/vnd.openxmlformats-officedocument.presentationml.presentation'
        "
        :src="fileUrl"
        class="!h-80vh !w-80vw"
        @rendered="renderedHandler"
        @error="errorHandler" />
      <vue-office-pdf
        v-if="fileUrl && mimeType === 'application/pdf'"
        :src="fileUrl"
        class="!h-80vh !w-80vw"
        @rendered="renderedHandler"
        @error="errorHandler" />
    </n-spin>
  </n-modal>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRequest } from 'alova/client';
import VueOfficeDocx from '@vue-office/docx';
import '@vue-office/docx/lib/index.css';
import VueOfficeExcel from '@vue-office/excel';
import '@vue-office/excel/lib/index.css';
import VueOfficePdf from '@vue-office/pdf';
import VueOfficePptx from '@vue-office/pptx';

const http = window.$http;

const show = defineModel<boolean>('show');
const id = defineModel<string | null>('id');
const mimeType = defineModel<string | null>('mimeType');

const fileUrl = ref<string | null>(null);
const fileLoading = ref<boolean>(true);

const { data: imagePreviewUrl, send: doGetImagePreviewFile } = useRequest(
  () => http.Post<any>('/file_data/_submit_download', [id.value]),
  { immediate: false }
);

const renderedHandler = () => {
  fileLoading.value = false;
};

const errorHandler = () => {
  fileLoading.value = false;
};

watch(show, async (newShow) => {
  if (newShow) {
    fileLoading.value = true;
    try {
      await doGetImagePreviewFile();
      fileUrl.value =
        http.options.baseURL + '/file_data/_download/' + imagePreviewUrl.value;
    } catch {
      fileLoading.value = false;
    }
  } else {
    fileUrl.value = null;
  }
});
</script>
