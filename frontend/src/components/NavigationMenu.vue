<template>
  <n-menu
    :options="menus"
    :value="activeMenu"
    mode="horizontal"
    class="text-base" />
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { ref, computed, h, watchEffect } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { hasPermission } from '@/commons/permission';

const route = useRoute();
const router = useRouter();
const { t } = useI18n();

const menus = ref<any>([]);

const activeMenu = computed((): string => {
  if (route.matched.length >= 2) {
    return <string>route.matched[1].name;
  }
  return '';
});

watchEffect(async () => {
  if (route.matched.length === 1 || menus.value.length === 0) {
    menus.value = await getAllMenus();
  }
  if (route.matched.length === 1) {
    if (menus.value.length > 0) {
      await router.push({ name: <string>menus.value[0].key });
    } else {
      await router.push({ name: 'user-settings' });
    }
  }
});

async function getAllMenus() {
  let newMenus = [];
  for (const route of router.options.routes) {
    if (route.name !== 'home' || !route.children) {
      continue;
    }
    for (const ro of route.children) {
      if (ro.name === 'user-settings' || !ro.meta) {
        continue;
      }
      if (
        ro.meta.permission &&
        !hasPermission(ro.meta.permission, ro.meta.permissionOr)
      ) {
        continue;
      }
      if (ro.meta.show && !(await ro.meta.show())) {
        continue;
      }
      const roTitle = ro.meta.title;
      newMenus.push({
        key: <string>ro.name,
        label: () =>
          h(
            RouterLink,
            {
              to: { name: ro.name }
            },
            {
              default: () => (roTitle ? t(roTitle) : '')
            }
          )
      });
    }
    break;
  }
  return newMenus;
}
</script>
