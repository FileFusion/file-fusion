<template>
  <div>
    <n-card hoverable>
      <n-flex justify="space-between">
        <n-flex>
          <n-button
            v-permission="'personal_file:download'"
            :loading="downloadFileLoading"
            type="primary"
            @click="downloadFiles(fileTableCheck)">
            {{ $t('files.personal.download') }}
          </n-button>
          <n-popconfirm
            :positive-button-props="{ type: 'error' }"
            @positive-click="deleteFiles(fileTableCheck)">
            <template #trigger>
              <n-button
                v-permission="'personal_file:delete'"
                :loading="deleteFileLoading"
                type="error">
                {{ $t('common.delete') }}
              </n-button>
            </template>
            {{ $t('common.batchDeleteConfirm') }}
          </n-popconfirm>
        </n-flex>
        <n-flex :wrap="false" justify="end">
          <n-dropdown
            v-if="fileShowType === 'grid'"
            :options="getFileTableSorterOptions"
            :show-arrow="true"
            trigger="click">
            <n-button>
              <template #icon>
                <n-icon>
                  <i-sort-two />
                </n-icon>
              </template>
              {{ t('common.sort') }}
            </n-button>
          </n-dropdown>
          <n-input-group>
            <n-input
              v-model:value="fileNamePattern"
              :placeholder="
                $t('common.search') + ' ' + $t('files.personal.fileName')
              "
              clearable
              @keyup.enter="fileTableReload()">
              <template #prefix>
                <n-icon>
                  <i-search />
                </n-icon>
              </template>
            </n-input>
            <n-button ghost type="primary" @click="fileTableReload()">
              {{ $t('common.search') }}
            </n-button>
          </n-input-group>
          <n-radio-group v-model:value="fileShowType">
            <n-radio-button value="grid">
              <n-icon>
                <i-grid-four />
              </n-icon>
            </n-radio-button>
            <n-radio-button value="table">
              <n-icon>
                <i-list-two />
              </n-icon>
            </n-radio-button>
          </n-radio-group>
        </n-flex>
      </n-flex>
      <n-spin v-if="fileShowType === 'grid'" :show="fileTableLoading">
        <n-checkbox-group
          v-if="fileTableData.length > 0"
          v-model:value="fileTableCheck">
          <n-flex class="mt-3" :size="[50, 20]">
            <n-card
              v-for="(fileData, index) in fileTableData"
              :key="index"
              :hoverable="true"
              :bordered="false"
              class="w-32 cursor-pointer"
              content-style="position: relative;padding: 0;"
              @click="clickFile(fileData)"
              @mouseenter="fileData.showOperate = true"
              @mouseleave="fileData.showOperate = false">
              <n-checkbox
                v-if="fileData.showOperate || fileGridIsCheck(fileData.path)"
                class="absolute left-2 top-2 z-1"
                :value="fileData.path"
                @click.stop="" />
              <n-dropdown
                v-if="fileData.showOperate || fileData.showOperateMenu"
                :show-arrow="true"
                trigger="manual"
                :options="getFileDropdownOptions(fileData)"
                :show="fileData.showOperateMenu">
                <n-button
                  text
                  type="primary"
                  class="absolute right-2 top-2 z-1"
                  @click.stop="
                    fileData.showOperateMenu = !fileData.showOperateMenu
                  ">
                  <n-icon :size="18">
                    <i-more-two />
                  </n-icon>
                </n-button>
              </n-dropdown>
              <div class="relative pb-2 pl-1 pr-1 pt-4 text-center">
                <div>
                  <file-preview
                    :path="fileData.path"
                    :thumbnail="fileData.hasThumbnail"
                    :type="fileData.mimeType" />
                </div>
                <div class="mt-3">
                  <n-ellipsis :line-clamp="2">
                    {{ fileData.name }}
                  </n-ellipsis>
                </div>
                <div>
                  <n-text depth="3">
                    <n-time
                      :time="fileData.lastModifiedDate"
                      format="yyyy-MM-dd HH:mm" />
                  </n-text>
                </div>
              </div>
            </n-card>
          </n-flex>
        </n-checkbox-group>
        <n-empty
          v-if="fileTableData.length === 0"
          :description="t('common.noData')"
          class="mb-12 mt-12"></n-empty>
      </n-spin>
      <n-data-table
        v-if="fileShowType === 'table'"
        :bordered="false"
        :checked-row-keys="fileTableCheck"
        :columns="fileTableColumns"
        :data="fileTableData"
        :loading="fileTableLoading"
        :row-key="(row: any) => row.path"
        remote
        class="mt-3"
        @update:sorter="fileTableHandleSorter"
        @update:checked-row-keys="fileTableHandleCheck" />
      <n-pagination
        class="mt-3 justify-end"
        :page="fileTablePage"
        :page-size="fileTablePageSize"
        :page-sizes="[10, 20, 50]"
        :item-count="fileTableTotal"
        :show-size-picker="true"
        :show-quick-jumper="true"
        :prefix="
          (pagination: PaginationInfo) => {
            return t('common.total') + ': ' + pagination.itemCount;
          }
        "
        @update:page="fileTablePageChange"
        @update:page-size="fileTablePageSizeChange" />
    </n-card>
    <n-modal
      v-model:show="showRenameFileModal"
      :auto-focus="false"
      :show-icon="false"
      :title="$t('files.personal.renameFile')"
      preset="dialog">
      <n-spin :show="renameFileLoading">
        <n-form
          ref="renameFileFormRef"
          :model="renameFileForm"
          :rules="renameFileFormRules">
          <n-form-item path="name">
            <n-input
              v-model:value="renameFileForm.targetName"
              :placeholder="$t('files.personal.fileName')"
              clearable
              maxlength="255"
              show-count />
          </n-form-item>
        </n-form>
        <div class="text-right">
          <n-button
            size="small"
            type="primary"
            @click="validateRenameFileForm()">
            {{ $t('common.save') }}
          </n-button>
        </div>
      </n-spin>
    </n-modal>
  </div>
</template>

<script lang="ts" setup>
import type {
  DataTableColumn,
  DataTableSortState,
  FormItemRule,
  FormRules,
  PaginationInfo
} from 'naive-ui';
import { NButton, NDropdown, NTime } from 'naive-ui';
import { computed, h, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import IconCheck from '~icons/icon-park-outline/check';
import IconDown from '~icons/icon-park-outline/down';
import IconDownload from '~icons/icon-park-outline/download';
import IconEditTwo from '~icons/icon-park-outline/edit-two';
import IconDelete from '~icons/icon-park-outline/delete';
import { useRequest, usePagination } from 'alova/client';
import { hasPermission } from '@/commons/permission';
import { formatFileSize, renderIconMethod } from '@/commons/utils';
import { useRouter, useRoute } from 'vue-router';
import FilePreview from '@/views/files/components/FilePreview.vue';

const { t } = useI18n();
const http = window.$http;
const router = useRouter();
const route = useRoute();

const permission = ref({
  personalFileDownload: hasPermission('personal_file:download'),
  personalFileEdit: hasPermission('personal_file:edit'),
  personalFileDelete: hasPermission('personal_file:delete')
});

watch(
  () => route.params.path,
  () => {
    fileTableReload();
  }
);

window.$event.on('UploadView:FileChangeEvent', () => {
  fileTableReload();
});

const fileShowType = ref<string>('grid');
const fileNamePattern = ref<string>('');
const fileTableCheck = ref<string[]>([]);
const fileTableSorter = ref<any>({
  name: <any>'ascend',
  size: <any>false,
  lastModifiedDate: <any>false
});

const renameFileFormRef = ref<HTMLFormElement>();
const showRenameFileModal = ref(false);
const renameFileForm = ref({
  originalName: '',
  targetName: ''
});
const renameFileFormRules = computed<FormRules>(() => {
  return {
    targetName: [
      {
        required: true,
        validator(_rule: FormItemRule, value: string) {
          if (!value || value.length === 0) {
            return new Error(t('files.personal.fileNameEmpty'));
          }
          if (value.length > 255 || value.indexOf('/') !== -1) {
            return new Error(t('files.personal.fileNameError'));
          }
          return true;
        },
        trigger: ['input', 'blur']
      }
    ]
  };
});

function getFileDropdownOptions(file: any) {
  return [
    {
      icon: renderIconMethod(IconDownload),
      key: 'download',
      label: t('files.personal.download'),
      props: {
        onClick: () => {
          file.showOperateMenu = false;
          downloadFiles([file.path]);
        }
      },
      show: permission.value.personalFileDownload
    },
    {
      icon: renderIconMethod(IconEditTwo),
      key: 'edit',
      label: t('files.personal.rename'),
      props: {
        onClick: () => {
          file.showOperateMenu = false;
          renameFile(file);
        }
      },
      show: permission.value.personalFileEdit
    },
    {
      icon: renderIconMethod(IconDelete),
      key: 'delete',
      label: t('common.delete'),
      props: {
        onClick: () => {
          file.showOperateMenu = false;
          deleteFile(file);
        }
      },
      show: permission.value.personalFileDelete
    }
  ];
}

const fileTableColumns = computed<DataTableColumn[]>(() => {
  const tableColumn: DataTableColumn[] = [
    {
      type: 'selection'
    },
    {
      title: t('files.personal.fileName'),
      key: 'name',
      resizable: true,
      sorter: true,
      sortOrder: fileTableSorter.value.name,
      render: (row: any) => {
        return h(
          NButton,
          {
            text: true,
            type: 'primary',
            onClick: () => clickFile(row)
          },
          {
            icon: () =>
              h(FilePreview, {
                path: row.path,
                thumbnail: row.hasThumbnail,
                type: row.mimeType,
                size: 18
              }),
            default: () => row.name
          }
        );
      }
    },
    {
      title: t('files.personal.size'),
      key: 'size',
      resizable: true,
      width: 170,
      sorter: true,
      sortOrder: fileTableSorter.value.size,
      render: (row: any) => {
        if (row.type === 'FOLDER') {
          return '-';
        }
        return formatFileSize(row.size);
      }
    },
    {
      title: t('files.personal.modifiedDate'),
      key: 'lastModifiedDate',
      resizable: true,
      width: 170,
      sorter: true,
      sortOrder: fileTableSorter.value.lastModifiedDate,
      render: (row: any) => {
        return h(NTime, {
          time: row.lastModifiedDate,
          format: 'yyyy-MM-dd HH:mm'
        });
      }
    }
  ];
  if (
    permission.value.personalFileDownload ||
    permission.value.personalFileEdit ||
    permission.value.personalFileDelete
  ) {
    tableColumn.push({
      title: t('common.options'),
      key: 'options',
      width: 100,
      render: (row: any) => {
        return h(
          NDropdown,
          {
            options: getFileDropdownOptions(row),
            showArrow: true,
            trigger: 'click'
          },
          {
            default: () => {
              return h(
                NButton,
                {
                  strong: true,
                  secondary: true,
                  iconPlacement: 'right',
                  size: 'small'
                },
                {
                  icon: renderIconMethod(IconDown),
                  default: () => t('common.options')
                }
              );
            }
          }
        );
      }
    });
  }
  return tableColumn;
});

const getFileTableSorter = computed(() => {
  let columnKey = null;
  let order = null;
  for (const key in fileTableSorter.value) {
    const sort = fileTableSorter.value[key];
    if (sort) {
      columnKey = key;
      order = sort;
      break;
    }
  }
  if (!columnKey || !order) {
    return false;
  }
  return 'sorter=' + columnKey + '&sorterOrder=' + order;
});

const getFileTableSorterOptions = computed(() => {
  const icon = renderIconMethod(IconCheck);
  let columnKey: any = null;
  let order: any = null;
  for (const key in fileTableSorter.value) {
    const sort = fileTableSorter.value[key];
    if (sort) {
      columnKey = key;
      order = sort;
      break;
    }
  }
  return [
    {
      icon: columnKey === 'name' ? icon : undefined,
      key: 'name',
      label: t('files.personal.fileName'),
      props: {
        onClick: () => {
          fileTableHandleSorter({
            columnKey: 'name',
            order: order,
            sorter: true
          });
        }
      }
    },
    {
      icon: columnKey === 'size' ? icon : undefined,
      key: 'size',
      label: t('files.personal.size'),
      props: {
        onClick: () => {
          fileTableHandleSorter({
            columnKey: 'size',
            order: order,
            sorter: true
          });
        }
      }
    },
    {
      icon: columnKey === 'lastModifiedDate' ? icon : undefined,
      key: 'lastModifiedDate',
      label: t('files.personal.modifiedDate'),
      props: {
        onClick: () => {
          fileTableHandleSorter({
            columnKey: 'lastModifiedDate',
            order: order,
            sorter: true
          });
        }
      }
    },
    {
      type: 'divider',
      key: 'divider'
    },
    {
      icon: order === 'ascend' ? icon : undefined,
      key: 'ascend',
      label: t('common.asc'),
      props: {
        onClick: () => {
          fileTableHandleSorter({
            columnKey: columnKey,
            order: 'ascend',
            sorter: true
          });
        }
      }
    },
    {
      icon: order === 'descend' ? icon : undefined,
      key: 'descend',
      label: t('common.desc'),
      props: {
        onClick: () => {
          fileTableHandleSorter({
            columnKey: columnKey,
            order: 'descend',
            sorter: true
          });
        }
      }
    }
  ];
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

const {
  loading: fileTableLoading,
  data: fileTableData,
  page: fileTablePage,
  total: fileTableTotal,
  pageSize: fileTablePageSize,
  reload: fileTableReloadEvent
} = usePagination(
  (page, pageSize) => {
    const sorter = getFileTableSorter.value;
    return http.Get<any>(
      '/file_data/' +
        page +
        '/' +
        pageSize +
        '?name=' +
        fileNamePattern.value +
        '&path=' +
        filePathPattern.value +
        (sorter ? '&' + sorter : '')
    );
  },
  {
    initialPageSize: 20,
    total: (res) => res.totalElements,
    data: (res) => res.content
  }
);

const { loading: downloadFileLoading, send: doDownloadFile } = useRequest(
  (filePathList: string[]) =>
    http.Post('/file_data/_submit_download', filePathList),
  { immediate: false }
).onSuccess((response: any) => {
  window.location.href =
    http.options.baseURL + '/file_data/_download/' + response.data.downloadId;
});

const { loading: deleteFileLoading, send: doDeleteFile } = useRequest(
  (filePathList: string[]) =>
    http.Post('/file_data/_batch_delete', filePathList),
  {
    immediate: false
  }
).onSuccess(() => {
  window.$msg.success(t('common.deleteSuccess'));
  fileTableReload();
});

const { loading: renameFileLoading, send: doRenameFile } = useRequest(
  () =>
    http.Post('/file_data/_rename', {
      path: filePathPattern.value,
      originalName: renameFileForm.value.originalName,
      targetName: renameFileForm.value.targetName
    }),
  {
    immediate: false
  }
).onSuccess(() => {
  window.$msg.success(t('common.saveSuccess'));
  showRenameFileModal.value = false;
  fileTableReload();
});

function fileGridIsCheck(rowKey: string) {
  return fileTableCheck.value.includes(rowKey);
}

function fileTableHandleCheck(rowKeys: string[]) {
  fileTableCheck.value = rowKeys;
}

function fileTableHandleSorter(params: DataTableSortState | null) {
  for (const key in fileTableSorter.value) {
    fileTableSorter.value[key] = false;
  }
  if (params) {
    fileTableSorter.value[params.columnKey] = params.order;
  }
  fileTableReload();
}

function fileTablePageSizeChange(pageSize: number) {
  fileTableCheck.value = [];
  fileTablePageSize.value = pageSize;
}

function fileTablePageChange(page: number) {
  fileTableCheck.value = [];
  fileTablePage.value = page;
}

function fileTableReload() {
  fileTableCheck.value = [];
  fileTableReloadEvent();
}

function renameFile(file: any) {
  renameFileForm.value.originalName = file.name;
  renameFileForm.value.targetName = file.name;
  showRenameFileModal.value = true;
}

function validateRenameFileForm() {
  if (renameFileFormRef.value) {
    renameFileFormRef.value.validate((errors: any) => {
      if (!errors) {
        doRenameFile();
      }
    });
  }
}

function downloadFiles(filePathList: string[]) {
  if (!filePathList || filePathList.length === 0) {
    window.$msg.warning(t('files.personal.fileDownloadSelectCheck'));
    return;
  }
  doDownloadFile(filePathList);
}

function deleteFile(file: any) {
  window.$dialog.warning({
    title: t('common.warning'),
    content: t('common.deleteConfirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => {
      deleteFiles([file.path]);
    }
  });
}

function deleteFiles(filePathList: string[]) {
  if (!filePathList || filePathList.length === 0) {
    window.$msg.warning(t('files.personal.fileDeleteSelectCheck'));
    return;
  }
  doDeleteFile(filePathList);
}

function clickFile(file: any) {
  if (file.type === 'FOLDER') {
    let path = '';
    if (filePathPattern.value) {
      path += filePathPattern.value + '/';
    }
    path += file.name;
    router.push({
      name: 'files-personal',
      params: { path: path.split('/') }
    });
  }
}
</script>
