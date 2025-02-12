<template>
  <n-image
    :src="fileIcon"
    width="70"
    height="70"
    :preview-disabled="true"
    object-fit="contain" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { mainStore } from '@/store';
import { SupportThemes } from '@/commons/theme.ts';

import FolderIconWhite from '@/assets/images/file-icons/white/folder.png';
import FolderIconBlack from '@/assets/images/file-icons/black/folder.png';
import FileIconWhite from '@/assets/images/file-icons/white/file.png';
import FileIconBlack from '@/assets/images/file-icons/black/file.png';

const props = defineProps({
  type: {
    type: String,
    default: 'default',
    required: false
  }
});

const mStore = mainStore();
const theme = computed(() => mStore.getTheme);

const fileIconsWhite = ref<any>({
  default: FileIconWhite,
  'custom/folder': FolderIconWhite
});

const fileIconsBlack = ref<any>({
  default: FileIconBlack,
  'custom/folder': FolderIconBlack
});

const fileIcon = computed(() => {
  const fileIcons =
    theme.value === SupportThemes.DARK
      ? fileIconsBlack.value
      : fileIconsWhite.value;
  const icon = fileIcons[props.type];
  if (icon) {
    return icon;
  }
  return fileIcons['default'];
});
</script>
