<template>
  <div>
    <n-config-provider>
      <n-layout-header bordered class="h-16">
        <n-grid class="h-full items-center">
          <n-gi :span="6" class="h-8 pl-6">
            <img
              class="h-8 object-contain"
              :src="
                theme === SupportThemes.DARK ? logoTitleWhite : logoTitleBlack
              "
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
                trigger="hover"
                @select="switchUserOptions">
                <n-button text>
                  <n-icon :size="20">
                    <icon-people />
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
            :title="$t('navigationBar.about')"
            closable
            hoverable
            class="w-128"
            @close="handleClose()">
            <template #cover>
              <n-grid
                class="h-16 items-center border-0 border-b border-solid border-color">
                <n-gi :span="12" class="pl-6">
                  <img
                    class="h-8 object-contain !w-unset"
                    :src="
                      theme === SupportThemes.DARK
                        ? logoTitleWhite
                        : logoTitleBlack
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
                :label="$t('navigationBar.workflowQuantity')">
                {{ license.workflowQuantity }}
              </n-descriptions-item>
              <n-descriptions-item
                :label="$t('navigationBar.authorizationTime')">
                <n-time
                  v-if="license.startDate"
                  :time="new Date(license.startDate)"
                  format="yyyy-MM-dd"></n-time>
                <n-text v-else>{{ $t('common.empty') }}</n-text>
              </n-descriptions-item>
              <n-descriptions-item :label="$t('navigationBar.expirationTime')">
                <n-time
                  v-if="license.endDate"
                  :time="new Date(license.endDate)"
                  format="yyyy-MM-dd"></n-time>
                <n-text v-else>{{ $t('navigationBar.indefinitely') }}</n-text>
              </n-descriptions-item>
              <n-descriptions-item :label="$t('navigationBar.edition')">
                {{ license.edition }}
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
import logoTitleWhite from '@/assets/images/logo-title-white.png';
import logoTitleBlack from '@/assets/images/logo-title-black.png';
import packageInfo from '../../package.json';
import { SupportThemes } from '@/commons/theme.ts';

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
    icon: renderIconMethod(IconPeople)
  },
  {
    label: t('navigationBar.about'),
    key: 'about',
    icon: renderIconMethod(IconInfo)
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: t('navigationBar.logout'),
    key: 'logout',
    icon: renderIconMethod(IconBack)
  }
]);

const {
  loading: getLicenseLoading,
  data: license,
  send: doGetLicense
} = useRequest(() => http.Get<any>('/license/current'), {
  immediate: false,
  initialData: {
    authorizedTo: undefined,
    workflowQuantity: null,
    startDate: null,
    endDate: null,
    edition: null
  }
});

function switchUserOptions(option: string) {
  if (option === 'user') {
    router.push('/user-settings');
  } else if (option === 'logout') {
    window.$dialog.warning({
      title: t('common.info'),
      content: t('navigationBar.logoutConfirm'),
      positiveText: t('common.confirm'),
      negativeText: t('common.cancel'),
      onPositiveClick: () => {
        router.push('/login');
        mStore.setToken(null);
      }
    });
  } else if (option === 'about') {
    openAbout();
  }
}

function openAbout() {
  showAbout.value = true;
  doGetLicense();
}

function handleClose() {
  showAbout.value = false;
}
</script>
