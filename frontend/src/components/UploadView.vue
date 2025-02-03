<template>
  <div>
    <n-upload
      abstract
      :directory="isUploadDirectory"
      :multiple="true"
      :custom-request="uploadFileRequest">
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
            <n-upload-trigger #="{ handleClick }" abstract>
              <n-float-button
                type="primary"
                @click="uploadFileClick(false, handleClick)">
                <n-tooltip trigger="hover" placement="left">
                  <template #trigger>
                    <n-icon>
                      <i-upload />
                    </n-icon>
                  </template>
                  {{ $t('files.personal.uploadFile') }}
                </n-tooltip>
              </n-float-button>
            </n-upload-trigger>
            <n-upload-trigger #="{ handleClick }" abstract>
              <n-float-button
                type="primary"
                @click="uploadFileClick(true, handleClick)">
                <n-tooltip trigger="hover" placement="left">
                  <template #trigger>
                    <n-icon>
                      <i-folder-upload />
                    </n-icon>
                  </template>
                  {{ $t('files.personal.uploadFolder') }}
                </n-tooltip>
              </n-float-button>
            </n-upload-trigger>
            <n-float-button type="primary" @click="createFolder">
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
    </n-upload>
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
import type {
  FormItemRule,
  FormRules,
  UploadCustomRequestOptions
} from 'naive-ui';
import { computed, ref, nextTick } from 'vue';
import { useRequest } from 'alova/client';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';

const { t } = useI18n();
const http = window.$http;
const route = useRoute();

const isUploadDirectory = ref<boolean>(false);

const createFolderFormRef = ref<HTMLFormElement>();
const showCreateFolderModal = ref(false);
const createFolderForm = ref({
  name: ''
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

const filePathPattern = computed(() => {
  const path = route.params.path;
  if (!path) {
    return '';
  }
  if (Array.isArray(path)) {
    return path.join('/');
  }
  return path;
});

function emitFileChangeEvent() {
  window.$event.emit('UploadView:FileChangeEvent');
}

function createFolder() {
  createFolderForm.value.name = '';
  showCreateFolderModal.value = true;
}

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
  () =>
    http.Post('/file_data/_create_folder', {
      name: createFolderForm.value.name,
      path: filePathPattern.value
    }),
  {
    immediate: false
  }
).onSuccess(() => {
  window.$msg.success(t('common.createSuccess'));
  showCreateFolderModal.value = false;
  emitFileChangeEvent();
});

async function uploadFileClick(
  directory: boolean,
  uploadFileEvent: () => void
) {
  isUploadDirectory.value = directory;
  await nextTick();
  uploadFileEvent();
}

const uploadFileRequest = ({
  file,
  onProgress,
  onFinish,
  onError
}: UploadCustomRequestOptions) => {
  const formData = new FormData();
  formData.append('files', file.file as File);
  const uploadMethod = http.Post('/file_data/_upload', formData);
  uploadMethod.onUpload((event) => {
    const percent = Math.round((event.loaded / event.total) * 100);
    onProgress({
      percent: percent
    });
  });
  uploadMethod.then(() => {
    onFinish();
  });
  uploadMethod.catch((err) => {
    console.log(err);
    onError();
  });
};
</script>
