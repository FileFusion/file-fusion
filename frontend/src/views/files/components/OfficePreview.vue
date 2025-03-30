<template>
  <n-modal v-model:show="show">
    <n-spin :show="fileLoading">
      <component
        :is="currentComponent"
        v-if="fileUrl && currentComponent"
        :src="fileUrl"
        class="!h-80vh !w-80vw"
        @rendered="renderedHandler"
        @error="errorHandler" />
    </n-spin>
  </n-modal>
</template>

<script lang="ts" setup>
import { ref, watch, computed, defineAsyncComponent } from 'vue';
import { useRequest } from 'alova/client';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const http = window.$http;

const componentsMap: any = {
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
    defineAsyncComponent({
      loader: () =>
        import('@vue-office/docx').then((m) => {
          import('@vue-office/docx/lib/index.css');
          return m.default;
        })
    }),
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet':
    defineAsyncComponent({
      loader: () =>
        import('@vue-office/excel').then((m) => {
          import('@vue-office/excel/lib/index.css');
          return m.default;
        })
    }),
  'application/vnd.openxmlformats-officedocument.presentationml.presentation':
    defineAsyncComponent(() => import('@vue-office/pptx')),
  'application/pdf': defineAsyncComponent(() => import('@vue-office/pdf'))
};

const show = defineModel<boolean>('show');
const props = defineProps({
  id: { type: String, required: true },
  mimeType: { type: String, required: true }
});
const currentComponent = computed(() =>
  props.mimeType ? componentsMap[props.mimeType] : null
);

const fileUrl = ref<string | null>(null);
const fileLoading = ref<boolean>(true);

const { data: downloadId, send: doGetPreviewFile } = useRequest(
  () => http.Post<any>('/file_data/_submit_download', [props.id]),
  { immediate: false }
);

const renderedHandler = () => {
  fileLoading.value = false;
};

const errorHandler = () => {
  fileLoading.value = false;
  window.$msg.error(t('files.personal.fileReadFailure'));
};

watch(show, async (newShow) => {
  if (newShow) {
    fileLoading.value = true;
    try {
      await doGetPreviewFile();
      fileUrl.value =
        http.options.baseURL + '/file_data/_download/' + downloadId.value;
    } catch {
      fileLoading.value = false;
      window.$msg.error(t('files.personal.fileReadFailure'));
    }
  } else {
    fileUrl.value = null;
    fileLoading.value = false;
    downloadId.value = null;
  }
});
</script>
