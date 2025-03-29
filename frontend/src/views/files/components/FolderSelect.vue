<template>
  <n-modal
    v-model:show="model"
    :auto-focus="false"
    :show-icon="false"
    :title="$t('files.personal.folderSelectCheck')"
    preset="dialog">
    <div class="h-80 overflow-auto whitespace-pre">
      <n-tree
        :data="folderList"
        :on-load="getChildFolder"
        key-field="id"
        label-field="name"
        @update:selected-keys="selectedKeys" />
    </div>
    <div class="text-right">
      <n-button size="small" type="primary" @click="submit()">
        {{ $t('common.confirm') }}
      </n-button>
    </div>
  </n-modal>
</template>

<script setup lang="ts">
import type { TreeOption } from 'naive-ui';
import { NImage } from 'naive-ui';
import { ref, watch, h, computed } from 'vue';
import { useRequest } from 'alova/client';
import folderIconLight from '@/assets/images/file-icons/light/folder.webp';
import folderIconDark from '@/assets/images/file-icons/dark/folder.webp';
import { mainStore } from '@/store';
import { SUPPORT_THEMES } from '@/commons/theme.ts';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const http = window.$http;
const mStore = mainStore();
const theme = computed(() => mStore.getTheme);

const emit = defineEmits(['submit']);
const model = defineModel<boolean>();

const folderIcon = () =>
  h(NImage, {
    width: 18,
    height: 18,
    previewDisabled: true,
    objectFit: 'contain',
    src: theme.value === SUPPORT_THEMES.DARK ? folderIconDark : folderIconLight
  });
const folderList = ref<TreeOption[]>([]);
let expandedFolder: TreeOption | null = null;
const selectFolder = ref<string>('');

const { send: doGetFolder } = useRequest(
  () =>
    http.Get(
      '/file_data/folder?parentId=' + (expandedFolder ? expandedFolder.id : '')
    ),
  {
    immediate: false
  }
).onSuccess((res) => {
  const childFolderList = <TreeOption[]>res.data;
  for (const childFolder of childFolderList) {
    childFolder.isLeaf = false;
    childFolder.prefix = folderIcon;
  }
  if (expandedFolder) {
    if (childFolderList.length > 0) {
      expandedFolder.children = childFolderList;
    } else {
      expandedFolder.isLeaf = true;
    }
  } else {
    folderList.value = [
      {
        id: 'root',
        name: t('files.personal.rootDirectory'),
        isLeaf: false,
        prefix: folderIcon
      }
    ];
    folderList.value[0].children = childFolderList;
  }
});

function getChildFolder(node: TreeOption) {
  return new Promise<void>((resolve) => {
    expandedFolder = node;
    (async () => {
      await doGetFolder();
      resolve();
    })();
  });
}

function selectedKeys(node: string[]) {
  selectFolder.value = node[0];
}

function submit() {
  if (!selectFolder.value) {
    window.$msg.warning(t('files.personal.folderSelectCheck'));
    return;
  }
  model.value = false;
  emit('submit', selectFolder.value);
}

watch(model, (newValue) => {
  if (newValue) {
    doGetFolder();
  } else {
    folderList.value = [];
    expandedFolder = null;
    selectFolder.value = '';
  }
});
</script>
