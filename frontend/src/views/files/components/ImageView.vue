<template>
  <n-modal v-model:show="model">
    <n-spin :show="imagePreviewFileLoading">
      <n-image-group
        :show-toolbar-tooltip="true"
        :render-toolbar="renderToolbar">
        <n-image
          ref="imagePreview"
          width="0"
          height="0"
          :preview-src="imagePreviewUrl" />
      </n-image-group>
    </n-spin>
  </n-modal>
</template>

<script lang="ts" setup>
import type { ImageRenderToolbarProps } from 'naive-ui';
import { h, ref, nextTick, watch, computed } from 'vue';
import { useRequest } from 'alova/client';

const http = window.$http;

const model = defineModel<boolean>();
const props = defineProps({
  file: { type: Object, required: true }
});

const imagePreview = ref<any>(null);
const imagePreviewUrl = computed(() => {
  if (!imagePreviewFile.value) {
    return null;
  }
  return (
    http.options.baseURL +
    '/file_data/_download/' +
    imagePreviewFile.value.downloadId
  );
});

const downloadMethod = () =>
  http.Post<any>('/file_data/_submit_download', [props.file.path]);

const {
  loading: imagePreviewFileLoading,
  data: imagePreviewFile,
  send: doGetImagePreviewFile
} = useRequest(downloadMethod, { immediate: false });

const { send: doDownloadImagePreviewFile } = useRequest(downloadMethod, {
  immediate: false
}).onSuccess((response: any) => {
  window.location.href =
    http.options.baseURL + '/file_data/_download/' + response.data.downloadId;
});

async function initImage() {
  await doGetImagePreviewFile();
  imagePreview.value.$el.firstChild.click();
  await nextTick();
  (<any>document.getElementsByClassName('n-image-preview-overlay')[0]).onclick =
    destroyImage;
}

function destroyImage() {
  model.value = false;
}

function renderToolbar({ nodes }: ImageRenderToolbarProps) {
  return [
    nodes.prev,
    nodes.next,
    nodes.rotateCounterclockwise,
    nodes.rotateClockwise,
    nodes.resizeToOriginalSize,
    nodes.zoomOut,
    nodes.zoomIn,
    h(
      'div',
      {
        style: {
          'line-height': '1em'
        },
        onClickCapture: (e: MouseEvent) => {
          e.stopPropagation();
          doDownloadImagePreviewFile();
        }
      },
      nodes.download
    ),
    h(
      'div',
      {
        style: {
          'line-height': '1em'
        },
        onClick: destroyImage
      },
      nodes.close
    )
  ];
}

watch(model, async (newValue) => {
  if (newValue) {
    await initImage();
  }
});
</script>
