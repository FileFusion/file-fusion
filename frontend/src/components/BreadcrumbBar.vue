<template>
  <n-breadcrumb class="overflow-y-auto">
    <n-breadcrumb-item
      v-for="(b, index) in breadcrumb"
      :key="index"
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
import { computed, watch } from 'vue';
import IconFolderClose from '~icons/icon-park-outline/folder-close';
import { useRequest } from 'alova/client';

const router = useRouter();
const route = useRoute();
const http = window.$http;

const { data: allParentList, send: doGetAllParent } = useRequest(
  (id: string) => http.Get<any>('/file_data/' + id + '/parent'),
  { immediate: false }
);

watch(
  route,
  () => {
    if (route.name === 'files-personal') {
      const parentId = <string>route.params.parentId;
      if (parentId) {
        doGetAllParent(parentId);
      }
    }
  },
  { immediate: true }
);

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
    route.name === 'files-personal' &&
    route.params.parentId &&
    allParentList.value
  ) {
    for (let i = allParentList.value.length - 1; i >= 0; i--) {
      const parent = allParentList.value[i];
      b.push({
        name: route.name,
        label: parent.name,
        icon: IconFolderClose,
        i18n: false,
        params: {
          parentId: parent.id
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
    params: breadcrumb.params,
    query: breadcrumb.query
  });
}
</script>
