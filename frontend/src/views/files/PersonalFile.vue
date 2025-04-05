<template>
  <div>
    <n-card hoverable>
      <n-flex justify="space-between">
        <n-flex align="center">
          <n-checkbox
            v-if="fileShowType === 'grid'"
            :checked="fileGridAllIsCheck"
            :indeterminate="fileGridAllIsIndeterminate"
            @update:checked="fileGridHandleCheck" />
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
          <n-dropdown
            :options="moreFileActionOptions"
            :show-arrow="true"
            trigger="hover">
            <n-button
              v-permission-or="['personal_file:share', 'personal_file:move']"
              :loading="moveFileLoading">
              {{ $t('common.more') }}
            </n-button>
          </n-dropdown>
        </n-flex>
        <n-flex :wrap="false" justify="end" align="center">
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
          <n-radio-group
            :value="fileShowType"
            @update:value="switchFileShowType">
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
          <n-flex class="mt-3" :size="[40, 20]">
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
                v-if="fileData.showOperate || fileGridIsCheck(fileData.id)"
                class="absolute left-2 top-2 z-1"
                :value="fileData.id"
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
                  <file-thumbnail
                    :id="fileData.id"
                    :key="fileData.id"
                    :thumbnail="fileData.hasThumbnail"
                    :mime-type="fileData.mimeType" />
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
                      format="PP HH:mm" />
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
        :row-key="(row: any) => row.id"
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
    <file-rename
      :id="renameFileId"
      v-model="showRenameFile"
      :name="renameFileName"
      @submit="fileTableReload" />
    <folder-select v-model="showFolderSelect" @submit="submitMoveFiles" />
    <image-preview
      v-model:show="showImageFile"
      v-model:id="imageFileId"
      @preview-prev="imagePreviewPrevNext(true)"
      @preview-next="imagePreviewPrevNext(false)" />
    <media-preview
      :id="mediaFileId"
      v-model:show="showMediaFile"
      :name="mediaFileName"
      :mime-type="mediaFileMimeType" />
    <office-preview
      :id="officeFileId"
      v-model:show="showOfficeFile"
      :mime-type="officeFileMimeType" />
  </div>
</template>

<script lang="ts" setup>
import type {
  DataTableColumn,
  DataTableSortState,
  PaginationInfo
} from 'naive-ui';
import { NButton, NDropdown, NTime } from 'naive-ui';
import { computed, h, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import IconCheck from '~icons/icon-park-outline/check';
import IconDown from '~icons/icon-park-outline/down';
import IconDownload from '~icons/icon-park-outline/download';
import IconEditTwo from '~icons/icon-park-outline/edit-two';
import IconShareOne from '~icons/icon-park-outline/share-one';
import IconTransferData from '~icons/icon-park-outline/transfer-data';
import IconDelete from '~icons/icon-park-outline/delete';
import { useRequest, usePagination } from 'alova/client';
import { hasPermission } from '@/commons/permission';
import {
  formatFileSize,
  supportImagePreview,
  supportOfficePreview,
  supportAudioPreview
} from '@/commons/file';
import { renderIconMethod } from '@/commons/utils';
import { useRouter, useRoute } from 'vue-router';
import { mainStore } from '@/store';
import FileRename from '@/views/files/components/FileRename.vue';
import FileThumbnail from '@/views/files/components/FileThumbnail.vue';
import ImagePreview from '@/views/files/components/ImagePreview.vue';
import MediaPreview from '@/views/files/components/MediaPreview.vue';
import OfficePreview from '@/views/files/components/OfficePreview.vue';
import FolderSelect from '@/views/files/components/FolderSelect.vue';

const { t } = useI18n();
const http = window.$http;
const router = useRouter();
const route = useRoute();
const mStore = mainStore();

const permission = ref({
  personalFileDownload: hasPermission('personal_file:download'),
  personalFilePreview: hasPermission('personal_file:preview'),
  personalFileRename: hasPermission('personal_file:rename'),
  personalFileShare: hasPermission('personal_file:share'),
  personalFileMove: hasPermission('personal_file:move'),
  personalFileDelete: hasPermission('personal_file:delete')
});

watch(
  () => route.params.parentId,
  () => {
    fileTableReload();
  }
);

window.$event.on('UploadView:FileChangeEvent', () => {
  fileTableReload();
});

const fileShowType = computed(() => mStore.getFileShowType);
const fileNamePattern = ref<string>('');
const fileTableCheck = ref<string[]>([]);
const fileTableSorter = ref<any>({
  name: <any>'ascend',
  size: <any>false,
  lastModifiedDate: <any>false
});

const showRenameFile = ref<boolean>(false);
const renameFileId = ref<string>('');
const renameFileName = ref<string>('');

const showFolderSelect = ref<boolean>(false);
const moveFileIds = ref<string[]>([]);

const showImageFile = ref<boolean>(false);
const imageFileId = ref<string | null>(null);

const showMediaFile = ref<boolean>(false);
const mediaFileId = ref<string>('');
const mediaFileName = ref<string>('');
const mediaFileMimeType = ref<string>('');

const showOfficeFile = ref<boolean>(false);
const officeFileId = ref<string>('');
const officeFileMimeType = ref<string>('');

const moreFileActionOptions = computed(() => {
  return [
    {
      icon: renderIconMethod(IconShareOne),
      key: 'share',
      label: t('files.personal.share'),
      props: {
        onClick: () => {
          shareFiles(fileTableCheck.value);
        }
      },
      show: permission.value.personalFileShare
    },
    {
      icon: renderIconMethod(IconTransferData),
      key: 'move',
      label: t('files.personal.move'),
      props: {
        onClick: () => {
          moveFiles(fileTableCheck.value);
        }
      },
      show: permission.value.personalFileMove
    }
  ];
});

function switchFileShowType(value: string) {
  mStore.setFileShowType(value);
}

function getFileDropdownOptions(file: any) {
  return [
    {
      icon: renderIconMethod(IconDownload),
      key: 'download',
      label: t('files.personal.download'),
      props: {
        onClick: () => {
          file.showOperateMenu = false;
          downloadFiles([file.id]);
        }
      },
      show: permission.value.personalFileDownload
    },
    {
      icon: renderIconMethod(IconEditTwo),
      key: 'rename',
      label: t('files.personal.rename'),
      props: {
        onClick: () => {
          file.showOperateMenu = false;
          renameFile(file);
        }
      },
      show: permission.value.personalFileRename
    },
    {
      icon: renderIconMethod(IconShareOne),
      key: 'share',
      label: t('files.personal.share'),
      props: {
        onClick: () => {
          file.showOperateMenu = false;
          shareFiles([file.id]);
        }
      },
      show: permission.value.personalFileShare
    },
    {
      icon: renderIconMethod(IconTransferData),
      key: 'move',
      label: t('files.personal.move'),
      props: {
        onClick: () => {
          file.showOperateMenu = false;
          moveFiles([file.id]);
        }
      },
      show: permission.value.personalFileMove
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
              h(FileThumbnail, {
                id: row.id,
                key: row.id,
                thumbnail: row.hasThumbnail,
                mimeType: row.mimeType,
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
      width: 150,
      sorter: true,
      sortOrder: fileTableSorter.value.size,
      render: (row: any) => {
        if (row.mimeType === 'custom/folder') {
          return '-';
        }
        return formatFileSize(row.size);
      }
    },
    {
      title: t('files.personal.modifiedDate'),
      key: 'lastModifiedDate',
      resizable: true,
      width: 150,
      sorter: true,
      sortOrder: fileTableSorter.value.lastModifiedDate,
      render: (row: any) => {
        return h(NTime, {
          time: row.lastModifiedDate,
          format: 'PP HH:mm'
        });
      }
    }
  ];
  if (
    permission.value.personalFileDownload ||
    permission.value.personalFileRename ||
    permission.value.personalFileShare ||
    permission.value.personalFileMove ||
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

const fileParentIdPattern = computed((): string => {
  const parentId = route.params.parentId;
  if (!parentId) {
    return '';
  }
  return <string>parentId;
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
        '&parentId=' +
        fileParentIdPattern.value +
        (sorter ? '&' + sorter : '')
    );
  },
  {
    initialPageSize: 20,
    total: (res) => res.totalElements,
    data: (res) => res.content
  }
);

const {
  loading: downloadFileLoading,
  data: submitDownloadRes,
  send: doDownloadFile
} = useRequest(
  (fileIdList: string[]) =>
    http.Post('/file_data/_submit_download', fileIdList),
  { immediate: false }
).onSuccess(() => {
  window.location.href =
    http.options.baseURL + '/file_data/_download/' + submitDownloadRes.value;
});

const { loading: deleteFileLoading, send: doDeleteFile } = useRequest(
  (id: string) => http.Delete('/file_data/' + id),
  {
    immediate: false
  }
);

const { loading: moveFileLoading, send: doMoveFile } = useRequest(
  (moveFileModel: any) => http.Put('/file_data/_move', moveFileModel),
  {
    immediate: false
  }
);

const fileGridAllIsCheck = computed(() => {
  return (
    fileTableData.value.length !== 0 &&
    fileTableCheck.value.length === fileTableData.value.length
  );
});

const fileGridAllIsIndeterminate = computed(() => {
  return (
    fileTableCheck.value.length > 0 &&
    fileTableCheck.value.length !== fileTableData.value.length
  );
});

function fileGridHandleCheck(allIsCheck: boolean) {
  if (!allIsCheck) {
    fileTableCheck.value = [];
  } else {
    fileTableCheck.value = fileTableData.value.map((f: any) => f.id);
  }
}

function fileGridIsCheck(rowKey: string) {
  return fileTableCheck.value.includes(rowKey);
}

function fileTableHandleCheck(rowKeys: Array<string | number>) {
  fileTableCheck.value = <string[]>rowKeys;
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
  showRenameFile.value = true;
  renameFileId.value = file.id;
  renameFileName.value = file.name;
}

function downloadFiles(fileIdList: string[]) {
  if (!fileIdList || fileIdList.length === 0) {
    window.$msg.warning(t('files.personal.fileSelectCheck'));
    return;
  }
  doDownloadFile(fileIdList);
}

function deleteFile(file: any) {
  window.$dialog.warning({
    title: t('common.warning'),
    content: t('common.deleteConfirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => {
      deleteFiles([file.id]);
    }
  });
}

async function deleteFiles(fileIdList: string[]) {
  if (!fileIdList || fileIdList.length === 0) {
    window.$msg.warning(t('files.personal.fileSelectCheck'));
    return;
  }
  for (const fileId of fileIdList) {
    await doDeleteFile(fileId);
  }
  window.$msg.success(t('common.deleteSuccess'));
  fileTableReload();
}

function shareFiles(fileIdList: string[]) {
  if (!fileIdList || fileIdList.length === 0) {
    window.$msg.warning(t('files.personal.fileSelectCheck'));
    return;
  }
  console.log(fileIdList);
}

function moveFiles(fileIdList: string[]) {
  if (!fileIdList || fileIdList.length === 0) {
    window.$msg.warning(t('files.personal.fileSelectCheck'));
    return;
  }
  moveFileIds.value = fileIdList;
  showFolderSelect.value = true;
}

async function submitMoveFiles(targetFileId: string) {
  for (const sourceId of moveFileIds.value) {
    await doMoveFile({
      sourceId: sourceId,
      targetId: targetFileId
    });
  }
  window.$msg.success(t('files.personal.moveSuccess'));
  fileTableReload();
}

function clickFile(file: any) {
  if (file.mimeType === 'custom/folder') {
    router.push({
      name: 'files-personal',
      params: { parentId: file.id }
    });
    return;
  }
  if (!permission.value.personalFilePreview) {
    window.$msg.warning(t('files.personal.noPermissionPreviewFile'));
    return;
  }
  if (supportImagePreview(file.mimeType)) {
    showImageFile.value = true;
    imageFileId.value = file.id;
  } else if (file.canPlay || supportAudioPreview(file.mimeType)) {
    showMediaFile.value = true;
    mediaFileId.value = file.id;
    mediaFileName.value = file.name;
    mediaFileMimeType.value = file.mimeType;
  } else if (supportOfficePreview(file.mimeType)) {
    showOfficeFile.value = true;
    officeFileId.value = file.id;
    officeFileMimeType.value = file.mimeType;
  } else {
    window.$msg.info(t('files.personal.fileNotSupportPreview'));
  }
}

const supportImagePreviewFile = computed(() => {
  return fileTableData.value.filter((file: any) =>
    supportImagePreview(file.mimeType)
  );
});

function imagePreviewPrevNext(prev: boolean) {
  const fileIdIndexMap = new Map<any, number>(
    supportImagePreviewFile.value.map((file: any, index: number) => [
      file.id,
      index
    ])
  );
  const currentIndex = fileIdIndexMap.get(imageFileId.value) ?? -1;
  const targetIndex = Math.max(
    0,
    Math.min(currentIndex + (prev ? -1 : 1), fileIdIndexMap.size - 1)
  );
  imageFileId.value = supportImagePreviewFile.value[targetIndex].id;
}
</script>
