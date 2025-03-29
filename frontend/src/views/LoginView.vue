<template>
  <div class="h-full">
    <div class="absolute inset-0">
      <n-grid :cols="2" class="h-full">
        <n-gi :span="1" class="text-center filter bg-primary">
          <img
            :src="loginSide"
            alt="login side image"
            class="relative top-1/2 w-7/12 -translate-y-2/4" />
        </n-gi>
        <n-gi :span="1" class="relative h-full">
          <div class="h-16 flex items-center justify-end pr-6">
            <language-and-theme></language-and-theme>
          </div>
          <div
            class="absolute left-1/2 top-1/2 w-80 -translate-x-2/4 -translate-y-2/4">
            <div class="text-center">
              <img
                class="h-12 object-contain"
                :src="
                  theme === SUPPORT_THEMES.DARK ? logoTitleWhite : logoTitle
                "
                alt="title logo" />
            </div>
            <div class="mt-1 text-center">
              <n-text depth="3">{{ $t('common.slogans') }}</n-text>
            </div>
            <n-spin :show="loginLoading" class="mt-6">
              <n-form
                ref="loginFormRef"
                :model="loginForm"
                :rules="loginRules"
                :show-require-mark="false"
                label-placement="left">
                <n-form-item path="username">
                  <n-input
                    v-model:value="loginForm.username"
                    :placeholder="$t('login.username')"
                    clearable
                    size="large"
                    @keyup.enter="validateLoginForm()">
                    <template #prefix>
                      <n-icon>
                        <i-people />
                      </n-icon>
                    </template>
                  </n-input>
                </n-form-item>
                <n-form-item path="password">
                  <n-input
                    v-model:value="loginForm.password"
                    :placeholder="$t('login.password')"
                    clearable
                    show-password-on="mousedown"
                    size="large"
                    type="password"
                    @keyup.enter="validateLoginForm()">
                    <template #prefix>
                      <n-icon>
                        <i-lock />
                      </n-icon>
                    </template>
                  </n-input>
                </n-form-item>
                <n-form-item path="remember">
                  <n-grid :cols="2">
                    <n-gi :span="1" class="flex items-center">
                      <n-checkbox
                        v-model:checked="loginForm.remember"
                        size="large">
                        {{ $t('login.remember') }}
                      </n-checkbox>
                    </n-gi>
                    <n-gi :span="1" class="text-right">
                      <n-button
                        size="large"
                        type="primary"
                        @click="validateLoginForm()">
                        {{ $t('login.login') }}
                      </n-button>
                    </n-gi>
                  </n-grid>
                </n-form-item>
              </n-form>
            </n-spin>
          </div>
        </n-gi>
      </n-grid>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { FormRules } from 'naive-ui';
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useRequest } from 'alova/client';
import { mainStore } from '@/store';
import loginSide from '@/assets/images/login-side.webp';
import logoTitle from '@/assets/images/logo-title.webp';
import logoTitleWhite from '@/assets/images/logo-title-white.webp';
import { SUPPORT_THEMES } from '@/commons/theme.ts';

const mStore = mainStore();
const router = useRouter();
const route = useRoute();
const { t } = useI18n();
const http = window.$http;
const loginFormRef = ref<HTMLFormElement>();

const theme = computed(() => mStore.getTheme);

const loginForm = ref({
  username: '',
  password: '',
  remember: true
});

const loginRules = computed<FormRules>(() => {
  return {
    username: [
      {
        required: true,
        message: t('login.validator.username'),
        trigger: ['input', 'blur']
      }
    ],
    password: [
      {
        required: true,
        message: t('login.validator.password'),
        trigger: ['input', 'blur']
      }
    ]
  };
});

const {
  loading: loginLoading,
  data: loginRes,
  send: doLogin
} = useRequest(() => http.Post<any>('/user/_login', loginForm.value), {
  immediate: false
}).onSuccess(() => {
  mStore.setToken(loginRes.value, loginForm.value.remember);
  window.$msg.success(t('login.success'), t('login.welcomeBack'));
  const redirect = <string>route.query['redirect'];
  if (redirect && !redirect.startsWith('/login')) {
    router.push(redirect);
  } else {
    router.push({ name: 'home' });
  }
});

function validateLoginForm() {
  if (loginFormRef.value) {
    loginFormRef.value.validate((errors: any) => {
      if (!errors) {
        doLogin();
      }
    });
  }
}
</script>
