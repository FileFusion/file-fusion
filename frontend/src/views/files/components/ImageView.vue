<template>
  <n-modal v-model:show="show">
    <n-spin :show="imagePreviewFileLoading">
      <n-image-group
        :show-toolbar-tooltip="true"
        :render-toolbar="renderToolbar"
        @preview-prev="emit('preview-prev')"
        @preview-next="emit('preview-next')">
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

const emit = defineEmits(['preview-prev', 'preview-next']);
const show = defineModel<boolean>('show');
const path = defineModel<string | null>('path');

const imagePreview = ref<any>(null);
const imagePreviewUrl = ref<string | null>(null);

const downloadMethod = () =>
  http.Post<any>('/file_data/_submit_download', [path.value]);

const { loading: imagePreviewFileLoading, send: doGetImagePreviewFile } =
  useRequest(downloadMethod, { immediate: false }).onSuccess(
    (response: any) => {
      imagePreviewUrl.value =
        http.options.baseURL +
        '/file_data/_download/' +
        response.data.downloadId;
    }
  );

const { send: doDownloadImagePreviewFile } = useRequest(downloadMethod, {
  immediate: false
}).onSuccess((response: any) => {
  window.location.href =
    http.options.baseURL + '/file_data/_download/' + response.data.downloadId;
});

const renderToolbar = computed(() => ({ nodes }: ImageRenderToolbarProps) => [
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
]);

function destroyImage() {
  show.value = false;
  path.value = null;
}

watch([show, path], async ([newShow, newPath], [oldShow, oldPath]) => {
  if (!newShow) {
    return;
  }
  if (newPath !== oldPath) {
    await doGetImagePreviewFile();
  }
  if (newShow !== oldShow) {
    imagePreview.value.$el.firstChild.click();
    await nextTick();
    (<any>(
      document.getElementsByClassName('n-image-preview-overlay')[0]
    )).onclick = destroyImage;
  }
});
</script>
