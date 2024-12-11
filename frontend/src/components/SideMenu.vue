<template>
  <n-layout-sider
    :collapsed="sideMenuCollapsed"
    :collapsed-width="64"
    :native-scrollbar="false"
    :width="192"
    bordered
    collapse-mode="width"
    show-trigger="arrow-circle"
    @collapse="switchSideMenuCollapsed(true)"
    @expand="switchSideMenuCollapsed(false)">
    <n-menu
      :collapsed="sideMenuCollapsed"
      :collapsed-icon-size="24"
      :collapsed-width="64"
      :icon-size="24"
      :indent="12"
      :options="currentSideMenus"
      :root-indent="24"
      :value="activeMenu"
      accordion />
  </n-layout-sider>
</template>

<script lang="ts" setup>
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { computed, h, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { mainStore } from '@/store';
import { hasPermission } from '@/commons/permission';
import { renderIconMethod } from '@/commons/utils';

const mStore = mainStore();
const router = useRouter();
const route = useRoute();
const { t } = useI18n();

const sideMenuCollapsed = computed(() => mStore.getSideMenuCollapsed);

const activeMenu = computed(() => {
  if (route.matched.length >= 3) {
    return route.matched[2].path;
  }
  return '';
});

const currentSideMenus = ref<any[]>([]);

watch(
  () => route.matched,
  (matched: any) => {
    if (matched.length === 2 || currentSideMenus.value.length === 0) {
      currentSideMenus.value = getCurrentSideMenus();
    }
    if (matched.length === 2 && currentSideMenus.value.length > 0) {
      router.push(currentSideMenus.value[0].key).then();
    }
  },
  { immediate: true }
);

function getCurrentSideMenus() {
  const parentPath = route.matched[1].path;
  const asm = getAllSideMenus();
  if (Object.prototype.hasOwnProperty.call(asm, parentPath)) {
    return asm[parentPath];
  }
  return [];
}

function getAllSideMenus() {
  let newAllSideMenus: any = {};
  for (const routerOption of router.options.routes) {
    if (routerOption.path === '/' && routerOption.children) {
      for (const ro of routerOption.children) {
        if (ro.children) {
          for (const r of ro.children) {
            if (r.meta) {
              let hasP = false;
              if (r.meta.permission) {
                if (hasPermission(r.meta.permission, r.meta.permissionOr)) {
                  hasP = true;
                }
              } else {
                hasP = true;
              }
              if (hasP) {
                if (!newAllSideMenus[ro.path]) {
                  newAllSideMenus[ro.path] = [];
                }
                const title = r.meta.title;
                newAllSideMenus[ro.path].push({
                  key: ro.path + '/' + r.path,
                  label: () =>
                    h(
                      RouterLink,
                      {
                        to: ro.path + '/' + r.path
                      },
                      {
                        default: () => t(title ? title : '')
                      }
                    ),
                  icon: renderIconMethod(r.meta.icon, '#666666', 20)
                });
              }
            }
          }
        }
      }
      break;
    }
  }
  return newAllSideMenus;
}

function switchSideMenuCollapsed(value: boolean) {
  mStore.setSideMenuCollapsed(value);
}
</script>
