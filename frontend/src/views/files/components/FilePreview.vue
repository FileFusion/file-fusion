<template>
  <n-image
    :src="fileIcon"
    :width="props.size"
    :height="props.size"
    :preview-disabled="true"
    object-fit="contain" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { mainStore } from '@/store';
import { SupportThemes } from '@/commons/theme.ts';

import FileIconWhite from '@/assets/images/file-icons/white/file.png';
import FolderIconWhite from '@/assets/images/file-icons/white/folder.png';
import AudioIconWhite from '@/assets/images/file-icons/white/audio.png';
import FontIconWhite from '@/assets/images/file-icons/white/font.png';
import ImageIconWhite from '@/assets/images/file-icons/white/image.png';
import TextIconWhite from '@/assets/images/file-icons/white/text.png';
import VideoIconWhite from '@/assets/images/file-icons/white/video.png';
import ArchiveIconWhite from '@/assets/images/file-icons/white/archive.png';
import CodeIconWhite from '@/assets/images/file-icons/white/code.png';
import PdfIconWhite from '@/assets/images/file-icons/white/pdf.png';
import WordIconWhite from '@/assets/images/file-icons/white/word.png';
import PptIconWhite from '@/assets/images/file-icons/white/ppt.png';
import ExcelIconWhite from '@/assets/images/file-icons/white/excel.png';

import FileIconBlack from '@/assets/images/file-icons/black/file.png';
import FolderIconBlack from '@/assets/images/file-icons/black/folder.png';
import AudioIconBlack from '@/assets/images/file-icons/black/audio.png';
import FontIconBlack from '@/assets/images/file-icons/black/font.png';
import ImageIconBlack from '@/assets/images/file-icons/black/image.png';
import TextIconBlack from '@/assets/images/file-icons/black/text.png';
import VideoIconBlack from '@/assets/images/file-icons/black/video.png';
import ArchiveIconBlack from '@/assets/images/file-icons/black/archive.png';
import CodeIconBlack from '@/assets/images/file-icons/black/code.png';
import PdfIconBlack from '@/assets/images/file-icons/black/pdf.png';
import WordIconBlack from '@/assets/images/file-icons/black/word.png';
import PptIconBlack from '@/assets/images/file-icons/black/ppt.png';
import ExcelIconBlack from '@/assets/images/file-icons/black/excel.png';

const props = defineProps({
  type: {
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

const mStore = mainStore();
const theme = computed(() => mStore.getTheme);

const fileIconsWhite = ref<any>({
  //default
  default: FileIconWhite,
  audio: AudioIconWhite,
  font: FontIconWhite,
  image: ImageIconWhite,
  text: TextIconWhite,
  video: VideoIconWhite,

  //archive
  'application/x-freearc': ArchiveIconWhite,
  'application/x-bzip': ArchiveIconWhite,
  'application/x-bzip2': ArchiveIconWhite,
  'application/gzip': ArchiveIconWhite,
  'application/vnd.rar': ArchiveIconWhite,
  'application/x-tar': ArchiveIconWhite,
  'application/zip': ArchiveIconWhite,
  'application/x-7z-compressed': ArchiveIconWhite,

  //code
  'application/x-csh': CodeIconWhite,
  'application/json': CodeIconWhite,
  'application/ld+json': CodeIconWhite,
  'application/x-httpd-php': CodeIconWhite,
  'application/rtf': CodeIconWhite,
  'application/x-sh': CodeIconWhite,
  'application/xhtml+xml': CodeIconWhite,
  'application/xml': CodeIconWhite,
  'application/atom+xml': CodeIconWhite,
  'application/vnd.mozilla.xul+xml': CodeIconWhite,
  'text/css': CodeIconWhite,
  'text/html': CodeIconWhite,
  'text/javascript': CodeIconWhite,
  'text/xml': CodeIconWhite,

  //office
  'application/msword': WordIconWhite,
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
    WordIconWhite,
  'application/vnd.ms-powerpoint': PptIconWhite,
  'application/vnd.openxmlformats-officedocument.presentationml.presentation':
    PptIconWhite,
  'application/vnd.ms-excel': ExcelIconWhite,
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet':
    ExcelIconWhite,
  'text/csv': ExcelIconWhite,

  //other
  'application/ogg': AudioIconWhite,
  'application/vnd.ms-fontobject': FontIconWhite,
  'application/pdf': PdfIconWhite,

  //custom
  'custom/folder': FolderIconWhite
});

const fileIconsBlack = ref<any>({
  //default
  default: FileIconBlack,
  audio: AudioIconBlack,
  font: FontIconBlack,
  image: ImageIconBlack,
  text: TextIconBlack,
  video: VideoIconBlack,

  //archive
  'application/x-freearc': ArchiveIconBlack,
  'application/x-bzip': ArchiveIconBlack,
  'application/x-bzip2': ArchiveIconBlack,
  'application/gzip': ArchiveIconBlack,
  'application/vnd.rar': ArchiveIconBlack,
  'application/x-tar': ArchiveIconBlack,
  'application/zip': ArchiveIconBlack,
  'application/x-7z-compressed': ArchiveIconBlack,

  //code
  'application/x-csh': CodeIconBlack,
  'application/json': CodeIconBlack,
  'application/ld+json': CodeIconBlack,
  'application/x-httpd-php': CodeIconBlack,
  'application/rtf': CodeIconBlack,
  'application/x-sh': CodeIconBlack,
  'application/xhtml+xml': CodeIconBlack,
  'application/xml': CodeIconBlack,
  'application/atom+xml': CodeIconBlack,
  'application/vnd.mozilla.xul+xml': CodeIconBlack,
  'text/css': CodeIconBlack,
  'text/html': CodeIconBlack,
  'text/javascript': CodeIconBlack,
  'text/xml': CodeIconBlack,

  //office
  'application/msword': WordIconBlack,
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
    WordIconBlack,
  'application/vnd.ms-powerpoint': PptIconBlack,
  'application/vnd.openxmlformats-officedocument.presentationml.presentation':
    PptIconBlack,
  'application/vnd.ms-excel': ExcelIconBlack,
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet':
    ExcelIconBlack,
  'text/csv': ExcelIconBlack,

  //other
  'application/ogg': AudioIconBlack,
  'application/vnd.ms-fontobject': FontIconBlack,
  'application/pdf': PdfIconBlack,

  //custom
  'custom/folder': FolderIconBlack
});

const fileIcon = computed(() => {
  const fileIcons =
    theme.value === SupportThemes.DARK
      ? fileIconsBlack.value
      : fileIconsWhite.value;
  let icon = fileIcons[props.type];
  if (icon) {
    return icon;
  }
  icon = fileIcons[props.type.split('/')[0]];
  if (icon) {
    return icon;
  }
  return fileIcons['default'];
});
</script>
