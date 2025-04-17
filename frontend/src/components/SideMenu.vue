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
      :render-label="renderMenuLabel"
      :render-icon="renderMenuIcon"
      :root-indent="24"
      :value="activeMenu"
      accordion />
  </n-layout-sider>
</template>

<script lang="ts" setup>
import type { MenuOption } from 'naive-ui';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { Component, computed, h, ref, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { mainStore } from '@/store';
import { hasPermission } from '@/commons/permission';
import { renderIcon } from '@/commons/utils';

const mStore = mainStore();
const router = useRouter();
const route = useRoute();
const { t } = useI18n();

const sideMenuCollapsed = computed(() => mStore.getSideMenuCollapsed);

const activeMenu = computed((): string => {
  if (route.matched.length >= 3) {
    return <string>route.matched[2].name;
  }
  return '';
});

const currentSideMenus = ref<any[]>([]);

const renderMenuLabel = (option: MenuOption) => {
  const key = <string>option.key;
  const label = <string>option.label;
  return h(
    RouterLink,
    { to: { name: key } },
    { default: () => (label ? t(label) : '') }
  );
};

const renderMenuIcon = (option: MenuOption) => {
  const icon = <Component>option.customIcon;
  return renderIcon(icon);
};

watchEffect(async () => {
  if (route.matched.length === 2 || currentSideMenus.value.length === 0) {
    if (!route.matched || route.matched.length < 2) {
      currentSideMenus.value = [];
    } else {
      const parentName = <string>route.matched[1].name;
      const allSideMenus = await getAllSideMenus();
      if (!Object.prototype.hasOwnProperty.call(allSideMenus, parentName)) {
        currentSideMenus.value = [];
      } else {
        currentSideMenus.value = allSideMenus[parentName];
      }
    }
  }
  if (route.matched.length === 2 && currentSideMenus.value.length > 0) {
    await router.push({ name: currentSideMenus.value[0].key });
  }
});

async function getAllSideMenus() {
  let newAllSideMenus: any = {};
  for (const route of router.options.routes) {
    if (route.name !== 'home' || !route.children) {
      continue;
    }
    for (const ro of route.children) {
      if (!ro.children) {
        continue;
      }
      for (const r of ro.children) {
        if (!r.meta) {
          continue;
        }
        if (
          r.meta.permission &&
          !hasPermission(r.meta.permission, r.meta.permissionOr)
        ) {
          continue;
        }
        if (r.meta.show && !(await r.meta.show())) {
          continue;
        }
        const roName = <string>ro.name;
        if (!newAllSideMenus[roName]) {
          newAllSideMenus[roName] = [];
        }
        const rTitle = r.meta.title;
        newAllSideMenus[roName].push({
          key: r.name,
          label: rTitle,
          customIcon: r.meta.icon
        });
      }
    }
    break;
  }
  return newAllSideMenus;
}

function switchSideMenuCollapsed(value: boolean) {
  mStore.setSideMenuCollapsed(value);
}
</script>
