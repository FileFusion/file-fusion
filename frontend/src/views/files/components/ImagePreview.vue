<template>
  <n-modal v-model:show="show">
    <n-spin :show="imagePreviewFileLoading">
      <n-image-group
        :show-toolbar-tooltip="true"
        :render-toolbar="renderToolbar">
        <n-image ref="imagePreview" width="0" height="0" />
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
const id = defineModel<string | null>('id');

const imagePreview = ref<any>(null);

const downloadMethod = () =>
  http.Post<any>('/file_data/_submit_download', [id.value]);

const {
  loading: imagePreviewFileLoading,
  data: downloadId,
  send: doGetImagePreviewFile
} = useRequest(downloadMethod, { immediate: false });

const { data: submitDownloadRes, send: doDownloadImagePreviewFile } =
  useRequest(downloadMethod, {
    immediate: false
  }).onSuccess(() => {
    window.location.href =
      http.options.baseURL + '/file_data/_download/' + submitDownloadRes.value;
  });

const renderToolbar = computed(() => ({ nodes }: ImageRenderToolbarProps) => [
  h(
    'div',
    {
      style: {
        'line-height': '1em'
      },
      onClickCapture: (e: MouseEvent) => {
        e.stopPropagation();
        emit('preview-prev');
      }
    },
    nodes.prev
  ),
  h(
    'div',
    {
      style: {
        'line-height': '1em'
      },
      onClickCapture: (e: MouseEvent) => {
        e.stopPropagation();
        emit('preview-next');
      }
    },
    nodes.next
  ),
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
  id.value = null;
  downloadId.value = null;
  submitDownloadRes.value = null;
}

watch([show, id], async ([newShow, newId], [oldShow, oldId]) => {
  if (!newShow) {
    return;
  }
  if (!oldShow) {
    await nextTick();
    imagePreview.value.$el.firstChild.click();
    await nextTick();
    (<any>(
      document.getElementsByClassName('n-image-preview-overlay')[0]
    )).onclick = destroyImage;
  }
  if (newId != oldId) {
    await doGetImagePreviewFile();
    (<any>(
      document.getElementsByClassName('n-image-preview-wrapper')[0].firstChild
    )).src = http.options.baseURL + '/file_data/_download/' + downloadId.value;
  }
});
</script>
