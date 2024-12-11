<template>
  <n-grid v-if="user" :cols="2">
    <n-gi :span="1">
      <n-h6
        :type="user.systemdUser ? 'warning' : 'success'"
        align-text
        prefix="bar">
        <n-text :type="user.systemdUser ? 'warning' : 'success'">
          {{
            $t('userSettings.profile.accountStatus.systemdUser') +
            ': ' +
            (user.systemdUser ? $t('common.yes') : $t('common.no'))
          }}
        </n-text>
      </n-h6>
    </n-gi>
    <n-gi :span="1">
      <n-h6 :type="user.enabled ? 'success' : 'error'" align-text prefix="bar">
        <n-text :type="user.enabled ? 'success' : 'error'">
          {{
            $t('userSettings.profile.accountStatus.enabled') +
            ': ' +
            (user.enabled ? $t('common.yes') : $t('common.no'))
          }}
        </n-text>
      </n-h6>
    </n-gi>
    <n-gi :span="1">
      <n-h6
        :type="user.nonExpired ? 'success' : 'error'"
        align-text
        prefix="bar">
        <n-text :type="user.nonExpired ? 'success' : 'error'">
          {{
            $t('userSettings.profile.accountStatus.nonExpired') +
            ': ' +
            (user.nonExpired ? $t('common.yes') : $t('common.no'))
          }}
        </n-text>
      </n-h6>
    </n-gi>
    <n-gi :span="1">
      <n-h6
        :type="user.credentialsNonExpired ? 'success' : 'error'"
        align-text
        prefix="bar">
        <n-text :type="user.credentialsNonExpired ? 'success' : 'error'">
          {{
            $t('userSettings.profile.accountStatus.credentialsNonExpired') +
            ': ' +
            (user.credentialsNonExpired ? $t('common.yes') : $t('common.no'))
          }}
        </n-text>
      </n-h6>
    </n-gi>
    <n-gi :span="1">
      <n-h6
        :type="user.nonLocked ? 'success' : 'error'"
        align-text
        prefix="bar">
        <n-text :type="user.nonLocked ? 'success' : 'error'">
          {{
            $t('userSettings.profile.accountStatus.nonLocked') +
            ': ' +
            (user.nonLocked ? $t('common.yes') : $t('common.no'))
          }}
        </n-text>
      </n-h6>
    </n-gi>
  </n-grid>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { mainStore } from '@/store';

const mStore = mainStore();

const user = computed(() => mStore.getUser);
</script>
