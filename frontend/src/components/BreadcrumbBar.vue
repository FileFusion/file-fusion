<template>
  <n-breadcrumb>
    <n-breadcrumb-item
      v-for="(b, index) in breadcrumb"
      :key="b.name"
      @click="clickBreadcrumb(b, index)">
      <n-flex :size="4">
        <n-icon>
          <component :is="b.icon" />
        </n-icon>
        <n-text>{{ b.label ? (b.i18n ? $t(b.label) : b.label) : '' }}</n-text>
      </n-flex>
    </n-breadcrumb-item>
  </n-breadcrumb>
</template>

<script lang="ts" setup>
import { useRoute, useRouter } from 'vue-router';
import { computed } from 'vue';
import IconFolderClose from '~icons/icon-park-outline/folder-close';

const router = useRouter();
const route = useRoute();

const filePathPattern = computed(() => {
  const path = route.params.path;
  if (!path) {
    return [];
  }
  if (Array.isArray(path)) {
    return path;
  }
  return [path];
});

const breadcrumb = computed(() => {
  let b = [];
  for (let i = 1; i < route.matched.length; i++) {
    b.push({
      name: route.matched[i].name,
      label: route.matched[i].meta.title,
      icon: route.matched[i].meta.icon,
      i18n: true
    });
  }
  if (
    route.name === 'files-personal' ||
    route.name === 'files-org' ||
    route.name === 'files-recycle-bin'
  ) {
    const filePathList = filePathPattern.value;
    const filePathParamList = [];
    for (let i = 0; i < filePathList.length; i++) {
      filePathParamList.push(filePathList[i]);
      b.push({
        name: route.name,
        label: filePathList[i],
        icon: IconFolderClose,
        i18n: false,
        params: {
          path: [...filePathParamList]
        }
      });
    }
  }
  return b;
});

function clickBreadcrumb(breadcrumb: any, index: number) {
  if (index === breadcrumb.length - 1) {
    return;
  }
  router.push({
    name: breadcrumb.name,
    params: breadcrumb.params
  });
}
</script>
