<template>
  <n-card hoverable>
    <n-tabs :value="routeName" animated type="line" @update:value="switchTab">
      <n-tab
        :tab="$t('userSettings.profile.accountInfo')"
        name="/user-settings/profile/account-info"></n-tab>
      <n-tab
        v-permission="'user:change_password'"
        :tab="$t('userSettings.profile.changePassword')"
        name="/user-settings/profile/change-password"></n-tab>
      <n-tab
        :tab="$t('userSettings.profile.accountStatus.title')"
        name="/user-settings/profile/account-status"></n-tab>
    </n-tabs>
    <div class="pt-6">
      <router-view-content></router-view-content>
    </div>
  </n-card>
</template>

<script lang="ts" setup>
import { useRoute, useRouter } from 'vue-router';
import { computed } from 'vue';

const router = useRouter();
const route = useRoute();

const routeName = computed((): string => {
  return route.path;
});

function switchTab(value: string) {
  router.push(value);
}
</script>
