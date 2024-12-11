<template>
  <n-menu
    :options="menus"
    :value="activeMenu"
    mode="horizontal"
    class="text-base" />
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { computed, h } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { hasPermission } from '@/commons/permission';

const route = useRoute();
const router = useRouter();
const { t } = useI18n();

const menus = computed(() => {
  let newMenus = [];
  for (const routerOption of router.options.routes) {
    if (routerOption.path === '/' && routerOption.children) {
      for (const ro of routerOption.children) {
        if (ro.path !== '/user-settings' && ro.meta) {
          let hasP = false;
          if (ro.meta.permission) {
            if (hasPermission(ro.meta.permission, ro.meta.permissionOr)) {
              hasP = true;
            }
          } else {
            hasP = true;
          }
          if (hasP) {
            const title = ro.meta.title;
            newMenus.push({
              key: ro.path,
              label: () =>
                h(
                  RouterLink,
                  {
                    to: ro.path
                  },
                  {
                    default: () => t(title ? title : '')
                  }
                )
            });
          }
        }
      }
      break;
    }
  }
  return newMenus;
});

const activeMenu = computed(() => {
  if (route.matched.length >= 2) {
    return route.matched[1].path;
  }
  return '';
});
</script>
