<template>
  <n-spin :show="changePasswordLoading">
    <n-form
      ref="changePasswordFormRef"
      :model="changePasswordForm"
      :rules="changePasswordRules">
      <n-grid :cols="24" :x-gap="24">
        <n-form-item-gi
          :label="$t('userSettings.profile.originalPassword')"
          :span="12"
          path="originalPassword">
          <n-input
            v-model:value="changePasswordForm.originalPassword"
            :placeholder="$t('userSettings.profile.originalPassword')"
            clearable
            maxlength="64"
            show-count
            show-password-on="mousedown"
            type="password">
            <template #prefix>
              <n-icon>
                <i-lock />
              </n-icon>
            </template>
          </n-input>
        </n-form-item-gi>
        <n-form-item-gi
          :label="$t('userSettings.profile.newPassword')"
          :span="12"
          path="newPassword">
          <n-input
            v-model:value="changePasswordForm.newPassword"
            :placeholder="$t('userSettings.profile.newPassword')"
            clearable
            maxlength="64"
            minlength="6"
            show-count
            show-password-on="mousedown"
            type="password">
            <template #prefix>
              <n-icon>
                <i-lock />
              </n-icon>
            </template>
          </n-input>
        </n-form-item-gi>
      </n-grid>
      <div class="text-right">
        <n-button type="primary" @click="validateChangePasswordForm()">
          {{ $t('common.save') }}
        </n-button>
      </div>
    </n-form>
  </n-spin>
</template>

<script lang="ts" setup>
import type { FormItemRule, FormRules } from 'naive-ui';
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { useRequest } from 'alova/client';
import { mainStore } from '@/store';

const router = useRouter();
const http = window.$http;
const mStore = mainStore();
const { t } = useI18n();

const changePasswordFormRef = ref<HTMLFormElement>();

const changePasswordForm = ref({
  originalPassword: '',
  newPassword: ''
});

const changePasswordRules = computed<FormRules>(() => {
  return {
    originalPassword: [
      {
        required: true,
        validator(_rule: FormItemRule, value: string) {
          if (!value || value.trim() === '') {
            return new Error(t('userSettings.profile.validator.passwordEmpty'));
          }
          return true;
        },
        trigger: ['input', 'blur']
      }
    ],
    newPassword: [
      {
        required: true,
        validator(_rule: FormItemRule, value: string) {
          if (!value || value.trim() === '') {
            return new Error(t('userSettings.profile.validator.passwordEmpty'));
          }
          const regular = new RegExp(
            /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{6,64}$/
          );
          if (!regular.test(value)) {
            return new Error(t('userSettings.profile.validator.passwordError'));
          }
          return true;
        },
        trigger: ['input', 'blur']
      }
    ]
  };
});

const { loading: changePasswordLoading, send: doChangePassword } = useRequest(
  () => http.Put('/user/current/password', changePasswordForm.value),
  {
    immediate: false
  }
).onSuccess(() => {
  window.$msg.success(t('common.saveSuccess'));
  window.$msg.info(t('router.reLogin'));
  router.push({ name: 'login' });
  mStore.setToken(null);
});

function validateChangePasswordForm() {
  if (changePasswordFormRef.value) {
    changePasswordFormRef.value.validate((errors: any) => {
      if (!errors) {
        doChangePassword();
      }
    });
  }
}
</script>
