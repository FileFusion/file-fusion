<template>
  <n-spin
    :show="
      saveSysConfigRecycleBinRetentionDaysLoading ||
      saveSysConfigRecycleBinLoading ||
      getSysConfigRecycleBinLoading ||
      getSysConfigRecycleBinRetentionDaysLoading
    ">
    <n-form
      ref="sysConfigFormRef"
      :model="sysConfigForm"
      :rules="sysConfigRules">
      <n-grid :cols="24" :x-gap="24">
        <n-form-item-gi
          :label="$t('systemSettings.sysConfig.enableRecycleBin')"
          :span="12"
          path="recycleBin">
          <n-switch
            v-model:value="sysConfigForm.recycleBin"
            :checked-value="'true'"
            :unchecked-value="'false'" />
        </n-form-item-gi>
        <n-form-item-gi
          v-if="sysConfigForm.recycleBin === 'true'"
          :label="$t('systemSettings.sysConfig.recycleBinRetentionDays')"
          :span="12"
          path="recycleBinRetentionDays">
          <n-input-number
            v-model:value="sysConfigForm.recycleBinRetentionDays"
            class="w-full"
            :min="0"
            :precision="0"
            :placeholder="
              $t('systemSettings.sysConfig.recycleBinRetentionDaysPlaceholder')
            ">
            <template #suffix> 天 </template>
          </n-input-number>
        </n-form-item-gi>
      </n-grid>
      <div v-permission="'sys_config:recycle_bin_edit'" class="text-right">
        <n-button type="primary" @click="validateSysConfigForm()">{{
          $t('common.save')
        }}</n-button>
      </div>
    </n-form>
  </n-spin>
</template>

<script lang="ts" setup>
import type { FormItemRule, FormRules } from 'naive-ui';
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRequest } from 'alova/client';

const http = window.$http;
const { t } = useI18n();

const sysConfigForm = ref<any>({
  recycleBin: 'true',
  recycleBinRetentionDays: null
});
const sysConfigFormRef = ref<HTMLFormElement>();

const sysConfigRules = computed<FormRules>(() => {
  return {
    recycleBinRetentionDays: [
      {
        validator(_rule: FormItemRule, value: number | null | undefined) {
          if (value === undefined || value === null) {
            return new Error(
              t(
                'systemSettings.sysConfig.validator.recycleBinRetentionDaysEmpty'
              )
            );
          }
          return true;
        },
        trigger: ['input', 'blur']
      }
    ]
  };
});

const { loading: getSysConfigRecycleBinLoading } = useRequest(() =>
  http.Get<any>('/sys-config?configKey=RECYCLE_BIN')
).onSuccess((response: any) => {
  sysConfigForm.value.recycleBin = response.data.configValue;
});

const { loading: getSysConfigRecycleBinRetentionDaysLoading } = useRequest(() =>
  http.Get<any>('/sys-config?configKey=RECYCLE_BIN_RETENTION_DAYS')
).onSuccess((response: any) => {
  sysConfigForm.value.recycleBinRetentionDays = parseInt(
    response.data.configValue
  );
});

const {
  loading: saveSysConfigRecycleBinRetentionDaysLoading,
  send: doSaveSysConfigRecycleBinRetentionDays
} = useRequest(
  () =>
    http.Put('/sys-config', {
      configKey: 'RECYCLE_BIN_RETENTION_DAYS',
      configValue: sysConfigForm.value.recycleBinRetentionDays
    }),
  {
    immediate: false
  }
);

const {
  loading: saveSysConfigRecycleBinLoading,
  send: doSaveSysConfigRecycleBin
} = useRequest(
  () =>
    http.Put('/sys-config', {
      configKey: 'RECYCLE_BIN',
      configValue: sysConfigForm.value.recycleBin
    }),
  {
    immediate: false
  }
);

function validateSysConfigForm() {
  if (sysConfigFormRef.value) {
    sysConfigFormRef.value.validate(async (errors: any) => {
      if (!errors) {
        if (sysConfigForm.value.recycleBin === 'true') {
          await saveSysConfig();
        } else {
          window.$dialog.warning({
            title: t('common.info'),
            content: t('systemSettings.sysConfig.closeRecycleBinTips'),
            positiveText: t('common.confirm'),
            negativeText: t('common.cancel'),
            onPositiveClick: async () => {
              await saveSysConfig();
            }
          });
        }
      }
    });
  }
}

async function saveSysConfig() {
  await doSaveSysConfigRecycleBinRetentionDays();
  await doSaveSysConfigRecycleBin();
  window.$msg.success(t('common.saveSuccess'));
}
</script>
