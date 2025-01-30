<template>
  <n-breadcrumb>
    <n-breadcrumb-item
      v-for="(b, index) in breadcrumb"
      :key="b.path"
      @click="clickBreadcrumb(b, index)">
      <n-flex :size="4">
        <n-icon>
          <component :is="b.icon" />
        </n-icon>
        <n-text>{{ $t(b.label ? b.label : '') }}</n-text>
      </n-flex>
    </n-breadcrumb-item>
  </n-breadcrumb>
</template>

<script lang="ts" setup>
import { useRoute, useRouter } from 'vue-router';
import { computed } from 'vue';

const router = useRouter();
const route = useRoute();

const breadcrumb = computed(() => {
  let b = [];
  for (let i = 1; i < route.matched.length; i++) {
    b.push({
      path: route.matched[i].path,
      label: route.matched[i].meta.title,
      icon: route.matched[i].meta.icon
    });
  }
  return b;
});

function clickBreadcrumb(breadcrumb: any, index: number) {
  if (index === breadcrumb.length - 1) {
    return;
  }
  router.push(breadcrumb.path);
}
</script>
