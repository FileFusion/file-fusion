<template>
  <div>
    <n-config-provider>
      <n-layout-header bordered class="h-16">
        <n-grid class="h-full items-center">
          <n-gi :span="6" class="h-8 pl-6">
            <img
              class="h-8 object-contain"
              :src="theme === SUPPORT_THEMES.DARK ? logoTitleWhite : logoTitle"
              alt="title logo" />
          </n-gi>
          <n-gi :span="14">
            <navigation-menu></navigation-menu>
          </n-gi>
          <n-gi :span="4" class="pr-6 text-right">
            <language-and-theme>
              <n-dropdown
                :options="userOptions"
                :show-arrow="true"
                trigger="hover">
                <n-button text>
                  <n-icon :size="20">
                    <i-people />
                  </n-icon>
                </n-button>
              </n-dropdown>
            </language-and-theme>
          </n-gi>
        </n-grid>
      </n-layout-header>
    </n-config-provider>
    <n-modal v-model:show="showAbout" :auto-focus="false">
      <n-watermark
        :content="license.authorizedTo"
        :height="128"
        :rotate="-15"
        :width="192"
        :x-offset="12"
        :y-offset="28"
        cross
        selectable>
        <n-spin :show="getLicenseLoading">
          <n-card
            class="w-120"
            closable
            hoverable
            :title="$t('navigationBar.about')"
            @close="handleClose()">
            <template #cover>
              <n-grid
                class="h-16 items-center border-0 border-b border-solid border-color">
                <n-gi :span="12" class="pl-6">
                  <img
                    class="h-8 object-contain !w-unset"
                    :src="
                      theme === SUPPORT_THEMES.DARK ? logoTitleWhite : logoTitle
                    "
                    alt="title logo" />
                </n-gi>
                <n-gi :span="12" class="pr-6 text-right">
                  <n-text class="text-sm">
                    {{ $t('common.slogans') }}
                  </n-text>
                </n-gi>
              </n-grid>
            </template>
            <n-descriptions :column="2" label-placement="left">
              <n-descriptions-item :label="$t('navigationBar.authorizedTo')">
                {{ license.authorizedTo }}
              </n-descriptions-item>
              <n-descriptions-item
                :label="$t('navigationBar.authorizationTime')">
                <n-time
                  v-if="license.startDate"
                  :time="license.startDate"
                  format="PP"></n-time>
                <n-text v-else>{{ $t('common.empty') }}</n-text>
              </n-descriptions-item>
              <n-descriptions-item :label="$t('navigationBar.expirationTime')">
                <n-time
                  v-if="license.endDate"
                  :time="license.endDate"
                  format="PP"></n-time>
                <n-text v-else>{{ $t('navigationBar.indefinitely') }}</n-text>
              </n-descriptions-item>
              <n-descriptions-item :label="$t('navigationBar.edition')">
                {{ license.edition }}
              </n-descriptions-item>
              <n-descriptions-item :label="$t('navigationBar.license')">
                GPL-3.0
              </n-descriptions-item>
              <n-descriptions-item :label="$t('navigationBar.version')">
                V{{ packageInfo.version }}
              </n-descriptions-item>
            </n-descriptions>
          </n-card>
        </n-spin>
      </n-watermark>
    </n-modal>
  </div>
</template>

<script lang="ts" setup>
import IconPeople from '~icons/icon-park-outline/people';
import IconBack from '~icons/icon-park-outline/back';
import IconInfo from '~icons/icon-park-outline/info';
import { NIcon } from 'naive-ui';
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { useRequest } from 'alova/client';
import { mainStore } from '@/store';
import { renderIconMethod } from '@/commons/utils';
import logoTitle from '@/assets/images/logo-title.webp';
import logoTitleWhite from '@/assets/images/logo-title-white.webp';
import packageInfo from '../../package.json';
import { SUPPORT_THEMES } from '@/commons/theme.ts';

const mStore = mainStore();
const router = useRouter();
const { t } = useI18n();
const http = window.$http;

const theme = computed(() => mStore.getTheme);
const user = computed(() => mStore.getUser);

const showAbout = ref<boolean>(false);

const userOptions = computed(() => [
  {
    label: user.value ? user.value.name : '',
    key: 'user',
    icon: renderIconMethod(IconPeople),
    props: {
      onClick: goUserSettings
    }
  },
  {
    label: t('navigationBar.about'),
    key: 'about',
    icon: renderIconMethod(IconInfo),
    props: {
      onClick: openAbout
    }
  },
  {
    type: 'divider',
    key: 'divider'
  },
  {
    label: t('navigationBar.logout'),
    key: 'logout',
    icon: renderIconMethod(IconBack),
    props: {
      onClick: logout
    }
  }
]);

const {
  loading: getLicenseLoading,
  data: license,
  send: doGetLicense
} = useRequest(() => http.Get<any>('/license/current'), {
  immediate: false,
  initialData: {
    authorizedTo: null,
    startDate: null,
    endDate: null,
    edition: null
  }
});

function goUserSettings() {
  router.push({
    name: 'user-settings'
  });
}

function logout() {
  window.$dialog.warning({
    title: t('common.info'),
    content: t('navigationBar.logoutConfirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => {
      router.push({
        name: 'login'
      });
      mStore.setToken(null);
    }
  });
}

function openAbout() {
  doGetLicense();
  showAbout.value = true;
}

function handleClose() {
  showAbout.value = false;
}
</script>
