<template>
  <n-image
    :src="thumbnailFileUrl ? thumbnailFileUrl : fileIcon"
    :width="props.size"
    :height="props.size"
    :preview-disabled="true"
    object-fit="contain" />
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { mainStore } from '@/store';
import { useRequest } from 'alova/client';

const props = defineProps({
  id: {
    type: String,
    default: null,
    required: false
  },
  thumbnail: {
    type: Boolean,
    default: false,
    required: false
  },
  mimeType: {
    type: String,
    default: 'default',
    required: false
  },
  size: {
    type: Number,
    default: 70,
    required: false
  }
});

const http = window.$http;
const mStore = mainStore();
const theme = computed(() => mStore.getTheme);

const fileIcons = ['audio', 'font', 'image', 'text', 'video'];

const fileCategories = {
  archive: [
    'application/x-freearc',
    'application/x-bzip',
    'application/x-bzip2',
    'application/gzip',
    'application/vnd.rar',
    'application/x-tar',
    'application/zip',
    'application/x-7z-compressed'
  ],
  code: [
    'application/x-csh',
    'application/json',
    'application/ld+json',
    'application/x-httpd-php',
    'application/rtf',
    'application/x-sh',
    'application/xhtml+xml',
    'application/xml',
    'application/atom+xml',
    'application/vnd.mozilla.xul+xml',
    'text/css',
    'text/html',
    'text/javascript',
    'text/xml'
  ],
  word: [
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  ],
  ppt: [
    'application/vnd.ms-powerpoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation'
  ],
  excel: [
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/csv'
  ],
  audio: ['application/ogg'],
  font: ['application/vnd.ms-fontobject'],
  pdf: ['application/pdf'],
  folder: ['custom/folder']
};

const fileIcon = computed(() => {
  let iconType;
  for (const [category, types] of Object.entries(fileCategories)) {
    if (types.includes(props.mimeType)) {
      iconType = category;
    }
  }
  if (!iconType) {
    const category = props.mimeType.split('/')[0];
    iconType = fileIcons.includes(category) ? category : 'default';
  }
  return new URL(
    `/src/assets/images/file-icons/${theme.value}/${iconType}.webp`,
    import.meta.url
  ).href;
});

const thumbnailFileUrl = ref<string>('');
const { data: thumbnailFile, send: doGetThumbnailFile } = useRequest(
  () =>
    http.Get<any>('/file_data/' + props.id + '/thumbnail.webp', {
      responseType: 'blob'
    }),
  {
    immediate: false
  }
).onSuccess(() => {
  revokeThumbnailFile();
  thumbnailFileUrl.value = URL.createObjectURL(thumbnailFile.value);
});

onBeforeUnmount(() => {
  revokeThumbnailFile();
});

function revokeThumbnailFile() {
  if (thumbnailFileUrl.value) {
    URL.revokeObjectURL(thumbnailFileUrl.value);
    thumbnailFileUrl.value = '';
  }
}

watch(
  props,
  (newProps) => {
    if (newProps.thumbnail) {
      doGetThumbnailFile();
    } else {
      revokeThumbnailFile();
    }
  },
  { immediate: true }
);
</script>
