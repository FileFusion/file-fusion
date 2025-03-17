<template>
  <n-spin :show="saveProfileLoading || getCurrentUserLoading">
    <n-form ref="profileFormRef" :model="profileForm" :rules="profileRules">
      <n-grid :cols="24" :x-gap="24">
        <n-form-item-gi
          :label="$t('userSettings.profile.username')"
          :span="12"
          path="username">
          <n-input
            v-model:value="profileForm.username"
            :disabled="true"
            :placeholder="$t('userSettings.profile.username')"
            clearable
            maxlength="100"
            show-count />
        </n-form-item-gi>
        <n-form-item-gi
          :label="$t('userSettings.profile.name')"
          :span="12"
          path="name">
          <n-input
            v-model:value="profileForm.name"
            :placeholder="$t('userSettings.profile.name')"
            clearable
            maxlength="50"
            show-count />
        </n-form-item-gi>
        <n-form-item-gi
          :label="$t('userSettings.profile.email')"
          :span="12"
          path="email">
          <n-auto-complete
            v-model:value="profileForm.email"
            :options="emailAutoCompleteOptions">
            <template
              #default="{
                handleInput,
                handleBlur,
                handleFocus,
                value: slotValue
              }">
              <n-input
                :placeholder="$t('userSettings.profile.email')"
                :status="emailAutoCompleteStatus"
                :value="slotValue"
                clearable
                maxlength="100"
                show-count
                @blur="handleBlur"
                @focus="handleFocus"
                @input="handleInput" />
            </template>
          </n-auto-complete>
        </n-form-item-gi>
        <n-form-item-gi
          :label="$t('userSettings.profile.phone')"
          :span="12"
          path="phone">
          <n-input-group>
            <n-select
              v-model:value="profileForm.areaCode"
              :options="areaCodes(language)"
              :placeholder="$t('userSettings.profile.areaCode')"
              clearable
              filterable />
            <n-input
              v-model:value="profileForm.phone"
              :placeholder="$t('userSettings.profile.phone')"
              clearable
              maxlength="40"
              show-count />
          </n-input-group>
        </n-form-item-gi>
      </n-grid>
      <div v-permission="'user:edit'" class="text-right">
        <n-button type="primary" @click="validateProfileForm()">{{
          $t('common.save')
        }}</n-button>
      </div>
    </n-form>
  </n-spin>
</template>

<script lang="ts" setup>
import type { FormRules, FormItemRule } from 'naive-ui';
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRequest } from 'alova/client';
import areaCodes from '@/commons/area-codes';
import emailSuffix from '@/commons/email-suffix';
import { mainStore } from '@/store';

const http = window.$http;
const mStore = mainStore();
const { t } = useI18n();
const profileFormRef = ref<HTMLFormElement>();

const language = computed(() => mStore.getLanguage);

const emailAutoCompleteStatus = ref<'success' | 'warning' | 'error'>('success');

const profileRules = computed<FormRules>(() => {
  return {
    name: [
      {
        required: true,
        message: t('userSettings.profile.validator.nameEmpty'),
        trigger: ['input', 'blur']
      }
    ],
    email: [
      {
        validator(_rule: FormItemRule, value: string) {
          if (value) {
            const regular = new RegExp(
              /^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]+$/
            );
            if (!regular.test(value)) {
              emailAutoCompleteStatus.value = 'error';
              return new Error(t('userSettings.profile.validator.emailError'));
            }
          }
          emailAutoCompleteStatus.value = 'success';
          return true;
        },
        trigger: ['input', 'blur']
      }
    ],
    phone: [
      {
        validator(_rule: FormItemRule, value: string) {
          if (profileForm.value.areaCode || value) {
            if (!value) {
              return new Error(t('userSettings.profile.validator.phoneEmpty'));
            }
            const regular = new RegExp(/^\d+$/);
            if (!regular.test(value)) {
              return new Error(t('userSettings.profile.validator.phoneError'));
            }
            if (!profileForm.value.areaCode) {
              return new Error(
                t('userSettings.profile.validator.areaCodeEmpty')
              );
            }
          }
          return true;
        },
        trigger: ['input', 'blur']
      }
    ]
  };
});

const emailAutoCompleteOptions = computed(() => {
  const email = profileForm.value.email;
  if (!email) {
    return [];
  }
  let mailsFilter = [];
  let suffix = email.split('@')[1];
  suffix = '@' + (suffix ? suffix : '');
  for (const es of emailSuffix) {
    if (es.startsWith(suffix)) {
      mailsFilter.push(es);
    }
  }
  return mailsFilter.map((mail) => {
    const prefix = email.split('@')[0];
    return {
      label: prefix + mail,
      value: prefix + mail
    };
  });
});

const { loading: getCurrentUserLoading, data: profileForm } = useRequest(
  () => http.Get<any>('/user/current'),
  {
    initialData: {}
  }
).onSuccess(() => {
  mStore.setUser(profileForm.value);
});

const { loading: saveProfileLoading, send: doSaveProfile } = useRequest(
  () => http.Put('/user/current', profileForm.value),
  {
    immediate: false
  }
).onSuccess(() => {
  mStore.setUser(profileForm.value);
  window.$msg.success(t('common.saveSuccess'));
});

function validateProfileForm() {
  if (profileFormRef.value) {
    profileFormRef.value.validate((errors: any) => {
      if (!errors) {
        doSaveProfile();
      }
    });
  }
}
</script>
