<template>
  <n-menu
    :options="menus"
    :value="activeMenu"
    mode="horizontal"
    class="text-base" />
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { computed, h, watchEffect } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { hasPermission } from '@/commons/permission';

const route = useRoute();
const router = useRouter();
const { t } = useI18n();

const activeMenu = computed(() => {
  if (route.matched.length >= 2) {
    return route.matched[1].name;
  }
  return '';
});

const menus = computed(() => {
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
      const roTitle = ro.meta.title;
      newMenus.push({
        key: ro.name,
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
});

watchEffect(async () => {
  if (route.matched.length === 1) {
    if (menus.value.length > 0) {
      await router.push({ name: menus.value[0].key });
    } else {
      await router.push({ name: 'user-settings' });
    }
  }
});
</script>
