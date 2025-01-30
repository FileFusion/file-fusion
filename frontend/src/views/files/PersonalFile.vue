<template>
  <div>
    <n-card hoverable>
      <n-grid :cols="24">
        <n-gi :span="14">
          <n-dropdown
            :options="uploadOptions"
            :show-arrow="true"
            trigger="hover">
            <n-button
              v-permission="'personal_file:add'"
              type="primary"
              class="mr-3"
              >{{ $t('files.personal.upload') }}</n-button
            >
          </n-dropdown>
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
        </n-gi>
        <n-gi :span="10">
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
                  <icon-search />
                </n-icon>
              </template>
            </n-input>
            <n-button ghost type="primary" @click="fileTableReload()">
              {{ $t('common.search') }}
            </n-button>
          </n-input-group>
        </n-gi>
      </n-grid>
      <n-data-table
        :bordered="false"
        :checked-row-keys="fileTableCheck"
        :columns="fileTableColumns"
        :data="fileTableData"
        :loading="fileTableLoading"
        :pagination="{
          page: fileTablePage,
          pageSize: fileTablePageSize,
          pageSizes: [5, 10, 50],
          itemCount: fileTableTotal,
          showSizePicker: true,
          showQuickJumper: true,
          prefix: (pagination: PaginationInfo) => {
            return t('common.total') + ': ' + pagination.itemCount;
          }
        }"
        :row-key="(row: any) => row.path"
        remote
        class="mt-3"
        @update:sorter="fileTableHandleSorter"
        @update:page="fileTablePageChange"
        @update:page-size="fileTablePageSizeChange"
        @update:checked-row-keys="fileTableHandleCheck" />
    </n-card>
  </div>
</template>

<script lang="ts" setup>
import type {
  DataTableColumn,
  DataTableSortState,
  DropdownOption,
  PaginationInfo
} from 'naive-ui';
import { NButton, NDropdown } from 'naive-ui';
import { computed, h, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import IconDelete from '~icons/icon-park-outline/delete';
import IconEditTwo from '~icons/icon-park-outline/edit-two';
import IconDown from '~icons/icon-park-outline/down';
import { useRequest, usePagination } from 'alova/client';
import { hasPermission } from '@/commons/permission';
import { renderIconMethod } from '@/commons/utils';
import { format } from 'date-fns';

const { t } = useI18n();
const http = window.$http;

const uploadOptions = computed<DropdownOption[]>(() => {
  return [
    {
      label: t('files.personal.uploadFile'),
      key: 'uploadFile'
    },
    {
      label: t('files.personal.uploadFolder'),
      key: 'uploadFolder'
    },
    {
      label: t('files.personal.createFolder'),
      key: 'createFolder',
      props: {
        onClick: createFolder
      }
    }
  ];
});
const fileNamePattern = ref<string>('');
const fileTableCheck = ref<string[]>([]);
const fileTableSorter = ref<DataTableSortState | null>(null);
const showFileEdit = ref(false);
const currentOptionFile = ref({
  id: null,
  name: ''
});

const permission = ref({
  personalFileEdit: hasPermission('personal_file:edit'),
  personalFileDelete: hasPermission('personal_file:delete')
});

const fileTableColumns = computed<DataTableColumn[]>(() => {
  const tableColumn: DataTableColumn[] = [
    {
      type: 'selection'
    },
    {
      title: t('files.personal.fileName'),
      key: 'name',
      resizable: true,
      sorter: true
    },
    {
      title: t('files.personal.size'),
      key: 'size',
      resizable: true,
      sorter: true
    },
    {
      title: t('files.personal.createdDate'),
      key: 'createdDate',
      resizable: true,
      width: 170,
      sorter: true,
      render: (row: any) => {
        if (row.createdDate) {
          return format(row.createdDate, 'yyyy-MM-dd HH:mm:ss');
        }
        return '';
      }
    },
    {
      title: t('files.personal.modifiedDate'),
      key: 'lastModifiedDate',
      resizable: true,
      width: 170,
      sorter: true,
      render: (row: any) => {
        if (row.lastModifiedDate) {
          return format(row.lastModifiedDate, 'yyyy-MM-dd HH:mm:ss');
        }
        return '';
      }
    }
  ];
  if (
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
            options: [
              {
                icon: renderIconMethod(IconEditTwo),
                key: 'edit',
                label: t('files.personal.rename'),
                props: {
                  onClick: () => {
                    editFile(row);
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
                    deleteFile(row);
                  }
                },
                show: permission.value.personalFileDelete
              }
            ],
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
  if (!fileTableSorter.value || !fileTableSorter.value.order) {
    return false;
  }
  return (
    'sorter=' +
    fileTableSorter.value.columnKey +
    '&sorterOrder=' +
    fileTableSorter.value.order
  );
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
        (sorter ? '&' + sorter : '')
    );
  },
  {
    total: (res) => res.totalElements,
    data: (res) => res.content
  }
);

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

function fileTableHandleCheck(rowKeys: string[]) {
  fileTableCheck.value = rowKeys;
}

function fileTableHandleSorter(params: DataTableSortState | null) {
  fileTableSorter.value = params;
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

function createFolder() {}

function editFile(file: any) {
  currentOptionFile.value = JSON.parse(JSON.stringify(file));
  showFileEdit.value = true;
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
</script>
