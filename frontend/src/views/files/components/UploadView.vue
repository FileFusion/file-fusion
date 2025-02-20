<template>
  <div>
    <n-upload
      abstract
      :directory="isUploadDirectory"
      :multiple="true"
      :show-cancel-button="false"
      :custom-request="uploadFileRequest"
      @change="uploadFileChange">
      <n-flex
        v-if="route.name === 'files-personal'"
        v-permission="'personal_file:upload'"
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
      <n-flex
        v-if="fileList.length > 0"
        v-permission="'personal_file:upload'"
        class="fixed bottom-36 right-20 z-1">
        <n-popover trigger="hover" placement="left">
          <template #trigger>
            <n-progress
              class="custom-progress cursor-pointer"
              type="circle"
              :percentage="uploadPercentage"
              :color="themeVars.primaryColor"
              :indicator-text-color="themeVars.primaryColor" />
          </template>
          <div class="max-h-48 overflow-y-auto">
            <n-upload-file-list />
          </div>
        </n-popover>
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
  UploadCustomRequestOptions,
  UploadFileInfo
} from 'naive-ui';
import type { Progress } from 'alova';
import { computed, nextTick, ref } from 'vue';
import { useRequest } from 'alova/client';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { useThemeVars } from 'naive-ui';

const { t } = useI18n();
const http = window.$http;
const route = useRoute();
const themeVars = useThemeVars();

const isUploadDirectory = ref<boolean>(false);
const fileList = ref<UploadFileInfo[]>([]);

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

const uploadPercentage = computed<number>(() => {
  const files = fileList.value;
  const fileCount = files.length;
  if (fileCount === 0) {
    return 0;
  }
  let percentageCount = 0;
  for (const file of files) {
    percentageCount += file.percentage ? file.percentage : 0;
  }
  return Math.floor(percentageCount / fileCount);
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
      path: filePathPattern.value
        ? filePathPattern.value + '/' + createFolderForm.value.name
        : createFolderForm.value.name
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

function uploadFileChange(options: { fileList: UploadFileInfo[] }) {
  fileList.value = options.fileList;
}

const uploadFileRequest = ({
  file,
  onProgress,
  onFinish,
  onError
}: UploadCustomRequestOptions) => {
  const fileInfo: File = <File>file.file;
  let path;
  if (filePathPattern.value) {
    if (fileInfo.webkitRelativePath) {
      path = filePathPattern.value + '/' + fileInfo.webkitRelativePath;
    } else {
      path = filePathPattern.value;
    }
  } else {
    path = fileInfo.webkitRelativePath;
  }
  if (isUploadDirectory.value) {
    path = path.substring(0, path.lastIndexOf('/'));
  }
  const formData = new FormData();
  formData.append('file', fileInfo);
  formData.append('name', fileInfo.name);
  formData.append('path', path);
  formData.append('type', fileInfo.type);
  formData.append('lastModified', fileInfo.lastModified + '');
  const uploadMethod = http.Post('/file_data/_upload', formData, {
    meta: {
      loading: false
    }
  });
  uploadMethod.onUpload((progress: Progress) => {
    const percent = Math.round((progress.loaded / progress.total) * 100);
    onProgress({
      percent: percent
    });
  });
  uploadMethod
    .then(() => {
      onFinish();
    })
    .catch(() => {
      onError();
    })
    .finally(() => {
      emitFileChangeEvent();
    });
};
</script>

<style>
/* stylelint-disable-next-line */
.custom-progress.n-progress.n-progress--circle {
  width: 44px;
}

/* stylelint-disable-next-line */
.custom-progress.n-progress.n-progress--circle .n-progress-text {
  font-size: 14px;
}
</style>
