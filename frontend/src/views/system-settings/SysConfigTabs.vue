<template>
  <n-card hoverable>
    <n-tabs :value="routeName" animated type="line" @update:value="switchTab">
      <n-tab
        :tab="$t('files.recycleBinFile')"
        name="system-settings-sys-config-recycle-bin"></n-tab>
    </n-tabs>
    <div class="mt-6">
      <router-view-content></router-view-content>
    </div>
  </n-card>
</template>

<script lang="ts" setup>
import { useRoute, useRouter } from 'vue-router';
import { computed, ref, watch } from 'vue';
import { hasPermission } from '@/commons/permission.ts';

const router = useRouter();
const route = useRoute();

const permission = ref({
  sysConfigRecycleBinRead: hasPermission('sys_config:recycle_bin_read')
});

const routeName = computed((): string => {
  return <string>route.name;
});

watch(
  () => route.name,
  (newRouteName) => {
    if (newRouteName === 'system-settings-sys-config') {
      if (permission.value.sysConfigRecycleBinRead) {
        switchTab('system-settings-sys-config-recycle-bin');
      }
    }
  },
  { immediate: true }
);

function switchTab(value: string) {
  router.push({ name: value });
}
</script>
