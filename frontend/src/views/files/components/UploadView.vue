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
import type { Chunk } from '@/commons/file';
import { useThemeVars } from 'naive-ui';
import { computed, nextTick, ref } from 'vue';
import { useRequest } from 'alova/client';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import {
  getFileChunks,
  getFileRelativePath,
  getFileHash
} from '@/commons/file';
import PQueue from 'p-queue';

const { t } = useI18n();
const http = window.$http;
const route = useRoute();
const themeVars = useThemeVars();

const isUploadDirectory = ref<boolean>(false);
const fileList = ref<UploadFileInfo[]>([]);
const lastFileBatchId = ref<string | null>(null);

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
          if (!value || value.trim().length === 0) {
            return new Error(t('files.personal.folderNameEmpty'));
          }
          if (
            value.length > 255 ||
            value === '.' ||
            value === '..' ||
            /[\\/:*?"<>|]/.test(value)
          ) {
            return new Error(t('files.personal.folderNameError'));
          }
          return true;
        },
        trigger: ['input', 'blur']
      }
    ]
  };
});

const fileParentIdPattern = computed((): string => {
  const parentId = route.params.parentId;
  if (!parentId) {
    return '';
  }
  return <string>parentId;
});

const uploadPercentage = computed<number>(() => {
  if (!lastFileBatchId.value || fileList.value.length === 0) {
    return 0;
  }
  let count = 0;
  let percentageCount = 0;
  for (const file of fileList.value) {
    if (file.batchId === lastFileBatchId.value) {
      count++;
      percentageCount += file.percentage ? file.percentage : 0;
    }
  }
  if (percentageCount === 0 || count === 0) {
    return 0;
  }
  return Math.floor(percentageCount / count);
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
    http.Post('/file_data/folder', {
      parentId: fileParentIdPattern.value,
      name: createFolderForm.value.name
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

const uploadFileRequest = async ({
  file,
  onProgress,
  onFinish,
  onError
}: UploadCustomRequestOptions) => {
  try {
    lastFileBatchId.value = file.batchId;
    const fileInfo: File = <File>file.file;
    const formData = new FormData();
    formData.append('parentId', fileParentIdPattern.value);
    formData.append('name', fileInfo.name);
    formData.append('path', getFileRelativePath(fileInfo));
    formData.append('hashValue', await getFileHash(fileInfo));
    formData.append('mimeType', fileInfo.type);
    formData.append('size', fileInfo.size.toString());
    formData.append('fileLastModifiedDate', fileInfo.lastModified.toString());
    const fastUploadSuccess = await handleUploadMerge(formData, true);
    if (!fastUploadSuccess) {
      await normalChunkUpload(formData, fileInfo, onProgress);
      await handleUploadMerge(formData, false);
    }
    onProgress({ percent: 100 });
    onFinish();
    emitFileChangeEvent();
  } catch {
    onError();
  }
};

const handleUploadMerge = async (formData: FormData, fastUpload: boolean) => {
  formData.set('fastUpload', fastUpload.toString());
  return http.Post('/file_data/_upload_chunk_merge', formData, {
    meta: {
      loading: false
    }
  });
};

const CHUNK_SIZE = 5 * 1024 * 1024;
const uploadQueue = new PQueue({
  concurrency: 3,
  timeout: 60000,
  throwOnTimeout: true
});
const normalChunkUpload = async (
  formData: FormData,
  file: File,
  onProgress: (e: { percent: number }) => void
) => {
  const fileSize = parseInt(<string>formData.get('size'));
  const fileHash = <string>formData.get('hashValue');
  const chunks: Chunk[] = getFileChunks(fileSize, CHUNK_SIZE);
  let totalUploaded = 0;
  const tasks = [];
  for (const chunk of chunks) {
    tasks.push(async () => {
      const chunkFile = file.slice(chunk.start, chunk.end);
      const chunkHash = await getFileHash(chunkFile);
      const chunkFormData = new FormData();
      chunkFormData.append('file', chunkFile);
      chunkFormData.append('chunkIndex', chunk.index.toString());
      chunkFormData.append('chunkHashValue', chunkHash);
      chunkFormData.append('hashValue', fileHash);
      const uploadMethod = http.Post(
        '/file_data/_upload_chunk',
        chunkFormData,
        {
          meta: {
            loading: false
          }
        }
      );
      let chunkLoaded = 0;
      uploadMethod.onUpload((progress: Progress) => {
        const delta = progress.loaded - chunkLoaded;
        totalUploaded += delta;
        chunkLoaded = progress.loaded;
        onProgress({
          percent: Math.min(99, Math.round((totalUploaded / fileSize) * 100))
        });
      });
      await uploadMethod;
    });
  }
  await uploadQueue.addAll(tasks);
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
