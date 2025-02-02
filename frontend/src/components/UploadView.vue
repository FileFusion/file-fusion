<template>
  <div>
    <n-flex
      v-permission="'personal_file:add'"
      class="fixed bottom-36 right-6 z-1">
      <n-float-button
        position="relative"
        type="primary"
        menu-trigger="hover"
        height="44"
        width="44">
        <n-icon>
          <i-plus />
        </n-icon>
        <template #menu>
          <n-float-button type="primary">
            <n-tooltip trigger="hover" placement="left">
              <template #trigger>
                <n-icon>
                  <i-upload />
                </n-icon>
              </template>
              {{ $t('files.personal.uploadFile') }}
            </n-tooltip>
          </n-float-button>
          <n-float-button type="primary">
            <n-tooltip trigger="hover" placement="left">
              <template #trigger>
                <n-icon>
                  <i-folder-upload />
                </n-icon>
              </template>
              {{ $t('files.personal.uploadFolder') }}
            </n-tooltip>
          </n-float-button>
          <n-float-button type="primary">
            <n-tooltip trigger="hover" placement="left">
              <template #trigger>
                <n-icon>
                  <i-folder-plus />
                </n-icon>
              </template>
              {{ $t('files.personal.createFolder') }}
            </n-tooltip>
          </n-float-button>
        </template>
      </n-float-button>
    </n-flex>
    <n-modal
      v-model:show="showCreateFolderModal"
      :auto-focus="false"
      :show-icon="false"
      :title="$t('files.personal.createFolder')"
      preset="dialog">
      <n-spin :show="createFolderLoading">
        <n-form
          ref="createFolderFormRef"
          :model="createFolderForm"
          :rules="createFolderFormRules">
          <n-form-item path="name">
            <n-input
              v-model:value="createFolderForm.name"
              :placeholder="$t('files.personal.folderName')"
              clearable
              maxlength="255"
              show-count />
          </n-form-item>
        </n-form>
        <div class="text-right">
          <n-button
            size="small"
            type="primary"
            @click="validateCreateFolderForm()">
            {{ $t('common.confirm') }}
          </n-button>
        </div>
      </n-spin>
    </n-modal>
  </div>
</template>

<script lang="ts" setup>
import type { FormItemRule, FormRules } from 'naive-ui';
import { computed, ref } from 'vue';
import { useRequest } from 'alova/client';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const http = window.$http;

const createFolderFormRef = ref<HTMLFormElement>();

const showCreateFolderModal = ref(false);
const createFolderForm = ref({
  name: '',
  path: ''
});
const createFolderFormRules = computed<FormRules>(() => {
  return {
    name: [
      {
        required: true,
        validator(_rule: FormItemRule, value: string) {
          if (!value || value.length === 0) {
            return new Error(t('files.personal.folderNameEmpty'));
          }
          if (value.length > 255 || value.indexOf('/') !== -1) {
            return new Error(t('files.personal.folderNameError'));
          }
          return true;
        },
        trigger: ['input', 'blur']
      }
    ]
  };
});

// function createFolder(path: string) {
//   createFolderForm.value.name = '';
//   createFolderForm.value.path = path;
//   showCreateFolderModal.value = true;
// }

function validateCreateFolderForm() {
  if (createFolderFormRef.value) {
    createFolderFormRef.value.validate((errors: any) => {
      if (!errors) {
        doCreateFolder();
      }
    });
  }
}

const { loading: createFolderLoading, send: doCreateFolder } = useRequest(
  () => http.Post('/file_data/_create_folder', createFolderForm.value),
  {
    immediate: false
  }
).onSuccess(() => {
  window.$msg.success(t('common.createSuccess'));
  showCreateFolderModal.value = false;
});
</script>
