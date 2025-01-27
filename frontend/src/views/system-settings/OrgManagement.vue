<template>
  <div>
    <n-card hoverable>
      <n-grid :cols="4" x-gap="24">
        <n-gi :span="1">
          <n-space :size="12" vertical>
            <n-input
              v-model:value="orgPattern"
              :placeholder="$t('common.search')"
              clearable>
              <template #prefix>
                <n-icon>
                  <icon-search />
                </n-icon>
              </template>
            </n-input>
            <n-spin :show="getAllOrgLoading">
              <n-tree
                :cancelable="false"
                :data="orgList"
                :expanded-keys="expandedOrg"
                :pattern="orgPattern"
                :selected-keys="[currentOrgId]"
                :virtual-scroll="true"
                block-line
                key-field="id"
                label-field="name"
                @update:expanded-keys="expandOrg"
                @update:selected-keys="selectedOrg" />
            </n-spin>
          </n-space>
        </n-gi>
        <n-gi v-if="currentOrg" :span="3">
          <n-spin :show="addOrgLoading || updateOrgLoading || deleteOrgLoading">
            <n-form
              ref="orgFormRef"
              :model="currentOrg"
              :rules="orgFormRules"
              inline>
              <n-grid :cols="24" :x-gap="24">
                <n-form-item-gi
                  :label="$t('systemSettings.org.orgName')"
                  :span="9"
                  path="name">
                  <n-input
                    v-model:value="currentOrg.name"
                    :placeholder="$t('systemSettings.org.orgName')"
                    clearable
                    maxlength="100"
                    show-count />
                </n-form-item-gi>
                <n-form-item-gi
                  :label="$t('common.description')"
                  :span="10"
                  path="description">
                  <n-input
                    v-model:value="currentOrg.description"
                    :placeholder="$t('common.description')"
                    clearable
                    maxlength="500"
                    show-count />
                </n-form-item-gi>
                <n-form-item-gi v-permission="'org:edit'" :span="2">
                  <n-button type="primary" @click="validateOrgForm()">
                    {{ $t('common.save') }}
                  </n-button>
                </n-form-item-gi>
                <n-form-item-gi v-permission="'org:delete'" :span="3">
                  <n-popconfirm
                    :positive-button-props="{
                      type: 'error'
                    }"
                    @positive-click="deleteOrg()">
                    <template #trigger>
                      <n-button type="error">
                        {{ $t('common.delete') }}
                      </n-button>
                    </template>
                    {{ $t('common.deleteConfirm') }}
                  </n-popconfirm>
                </n-form-item-gi>
              </n-grid>
            </n-form>
          </n-spin>
          <n-spin v-if="!currentOrg.new" :show="removeUsersFromOrgLoading">
            <n-grid :cols="24" class="items-center">
              <n-gi :span="14" class="flex items-center gap-2">
                <n-text strong>
                  {{ $t('systemSettings.org.orgUsers') }}
                </n-text>
                <n-button
                  v-permission="'org:edit'"
                  text
                  type="primary"
                  @click="addUsersToOrg()">
                  <n-icon :size="20">
                    <icon-add-user />
                  </n-icon>
                </n-button>
                <n-popconfirm
                  :positive-button-props="{
                    type: 'error'
                  }"
                  @positive-click="deleteOrgUsers()">
                  <template #trigger>
                    <n-button v-permission="'org:edit'" text type="error">
                      <n-icon :size="20">
                        <icon-reduce-user />
                      </n-icon>
                    </n-button>
                  </template>
                  {{ $t('systemSettings.org.orgUsersDeleteConfirm') }}
                </n-popconfirm>
              </n-gi>
              <n-gi :span="10">
                <n-input-group>
                  <n-input
                    v-model:value="userPattern"
                    :placeholder="
                      $t('common.search') +
                      $t('userSettings.profile.username') +
                      '/' +
                      $t('userSettings.profile.name') +
                      '/' +
                      $t('userSettings.profile.email') +
                      '/' +
                      $t('userSettings.profile.phone')
                    "
                    clearable
                    @keyup.enter="userTableReload()">
                    <template #prefix>
                      <n-icon>
                        <icon-search />
                      </n-icon>
                    </template>
                  </n-input>
                  <n-button ghost type="primary" @click="userTableReload()">
                    {{ $t('common.search') }}
                  </n-button>
                </n-input-group>
              </n-gi>
            </n-grid>
            <n-data-table
              :bordered="false"
              :checked-row-keys="userTableCheck"
              :columns="userTableColumns"
              :data="userTableData"
              :loading="userTableLoading"
              :pagination="{
                page: userTablePage,
                pageSize: userTablePageSize,
                pageSizes: [5, 10, 50],
                itemCount: userTableTotal,
                showSizePicker: true,
                showQuickJumper: true,
                prefix: (pagination: any) => {
                  return t('common.total') + ': ' + pagination.itemCount;
                }
              }"
              :row-key="(row: any) => row.id"
              class="mt-3"
              remote
              @update:page="userTablePageChange"
              @update:page-size="userTablePageSizeChange"
              @update:checked-row-keys="userTableHandleCheck" />
          </n-spin>
        </n-gi>
        <n-gi v-if="!currentOrg" :span="3" class="mt-6">
          <n-empty
            :description="$t('systemSettings.org.chooseOrgFirst')"></n-empty>
        </n-gi>
      </n-grid>
    </n-card>
    <n-modal
      v-model:show="showAddOrgUsersModal"
      :auto-focus="false"
      :show-icon="false"
      :title="$t('systemSettings.org.addUserToOrg')"
      preset="dialog">
      <n-spin :show="getNotExistOrgUsersLoading || addUsersToOrgLoading">
        <n-form
          ref="addOrgUsersFormRef"
          :model="addOrgUsers"
          :rules="addOrgUsersFormRules">
          <n-form-item path="users">
            <n-select
              v-model:value="addOrgUsers.users"
              :options="notExistOrgUsers"
              label-field="username"
              value-field="id"
              :placeholder="$t('systemSettings.org.selectAddUser')"
              clearable
              filterable
              multiple />
          </n-form-item>
        </n-form>
        <div class="text-right">
          <n-button
            size="small"
            type="primary"
            @click="validateAddOrgUsersForm()">
            {{ $t('common.add') }}
          </n-button>
        </div>
      </n-spin>
    </n-modal>
  </div>
</template>

<script lang="ts" setup>
import type { DataTableColumn, FormRules, FormItemRule } from 'naive-ui';
import { NButton } from 'naive-ui';
import { computed, h, ref } from 'vue';
import IconAdd from '~icons/icon-park-outline/add';
import { useI18n } from 'vue-i18n';
import { ulid } from 'ulidx';
import { useRequest, usePagination } from 'alova/client';
import { hasPermission } from '@/commons/permission';
import { arrayToTree, renderIconMethod, treeForeach } from '@/commons/utils';

const { t } = useI18n();
const http = window.$http;
const orgFormRef = ref<HTMLFormElement>();
const addOrgUsersFormRef = ref<HTMLFormElement>();

const orgList = ref<any[]>([]);
const orgPattern = ref<string>('');
const currentOrgId = ref<string>('');
const currentOrg = ref<any>(null);
const expandedOrg = ref<string[]>(['root']);
const showAddOrgUsersModal = ref<boolean>(false);
const addOrgUsers = ref({
  users: []
});
const userPattern = ref<string>('');
const userTableCheck = ref<string[]>([]);

const permission = ref({
  orgAdd: hasPermission('org:add')
});

const orgFormRules = computed<FormRules>(() => {
  return {
    name: [
      {
        required: true,
        message: t('systemSettings.org.orgNameValidator'),
        trigger: ['input', 'blur']
      }
    ],
    description: [
      {
        required: false,
        trigger: ['input', 'blur']
      }
    ]
  };
});

const addOrgUsersFormRules = computed<FormRules>(() => {
  return {
    users: [
      {
        required: true,
        validator(_rule: FormItemRule, value: string) {
          if (!value || value.length === 0) {
            return new Error(t('systemSettings.org.selectAddUser'));
          }
          return true;
        },
        trigger: ['input', 'blur']
      }
    ]
  };
});

const userTableColumns = computed<DataTableColumn<any>[]>(() => [
  {
    type: 'selection'
  },
  {
    title: t('userSettings.profile.username'),
    key: 'username'
  },
  {
    title: t('userSettings.profile.name'),
    key: 'name'
  },
  {
    title: t('userSettings.profile.email'),
    key: 'email'
  },
  {
    title: t('userSettings.profile.phone'),
    key: 'phone',
    render: (row: any) => {
      if (row.areaCode && row.phone) {
        return row.areaCode + row.phone;
      }
      return '';
    }
  }
]);

const {
  loading: getAllOrgLoading,
  data: getAllOrgRes,
  send: doGetAllOrg
} = useRequest(() => http.Get<any[]>('/org')).onSuccess(() => {
  getAllOrgRes.value.push({
    id: 'root',
    parentId: '',
    name: 'Root',
    description: '',
    disabled: true
  });
  for (let org of getAllOrgRes.value) {
    if (!org.new && permission.value.orgAdd) {
      org.suffix = () =>
        h(
          NButton,
          {
            text: true,
            onClick: (e) => {
              e.stopPropagation();
              addOrg(org);
            }
          },
          {
            default: renderIconMethod(IconAdd, '#aaaaaa')
          }
        );
    }
  }
  orgList.value = arrayToTree(getAllOrgRes.value, '');
});

const {
  loading: addOrgLoading,
  data: addOrgRes,
  send: doAddOrg
} = useRequest(() => http.Post<any>('/org', currentOrg.value), {
  immediate: false
}).onSuccess(() => {
  addOrUpdateOrgSuccess(addOrgRes.value);
});

const {
  loading: updateOrgLoading,
  data: updateOrgRes,
  send: doUpdateOrg
} = useRequest(() => http.Put<any>('/org', currentOrg.value), {
  immediate: false
}).onSuccess(() => {
  addOrUpdateOrgSuccess(updateOrgRes.value);
});

function addOrUpdateOrgSuccess(res: any) {
  doGetAllOrg();
  currentOrg.value = res;
  currentOrgId.value = res.id;
  userTableReload();
  window.$msg.success(t('common.saveSuccess'));
}

const { loading: deleteOrgLoading, send: doDeleteOrg } = useRequest(
  () => http.Delete('/org/' + currentOrgId.value),
  {
    immediate: false
  }
).onSuccess(() => {
  doGetAllOrg();
  currentOrg.value = null;
  currentOrgId.value = '';
  window.$msg.success(t('common.deleteSuccess'));
});

const {
  loading: userTableLoading,
  data: userTableData,
  page: userTablePage,
  total: userTableTotal,
  pageSize: userTablePageSize,
  reload: userTableReloadEvent
} = usePagination(
  (page, pageSize) =>
    http.Get<any>(
      '/org/' +
        currentOrgId.value +
        '/users/' +
        page +
        '/' +
        pageSize +
        '?search=' +
        userPattern.value
    ),
  {
    immediate: false,
    total: (res) => res.totalElements,
    data: (res) => res.content
  }
);

const {
  loading: getNotExistOrgUsersLoading,
  data: notExistOrgUsers,
  send: doGetNotExistOrgUsers
} = useRequest(
  () => http.Get<any[]>('/org/' + currentOrgId.value + '/users/not_exits'),
  {
    immediate: false
  }
);

const { loading: addUsersToOrgLoading, send: doAddUsersToOrg } = useRequest(
  () =>
    http.Put(
      '/org/' + currentOrgId.value + '/_add_users',
      addOrgUsers.value.users
    ),
  {
    immediate: false
  }
).onSuccess(() => {
  showAddOrgUsersModal.value = false;
  userTableReload();
  window.$msg.success(t('common.addSuccess'));
});

const { loading: removeUsersFromOrgLoading, send: doRemoveUsersFromOrg } =
  useRequest(
    () =>
      http.Put(
        '/org/' + currentOrgId.value + '/_remove_users',
        userTableCheck.value
      ),
    {
      immediate: false
    }
  ).onSuccess(() => {
    userTableReload();
    window.$msg.success(t('common.deleteSuccess'));
  });

function selectedOrg(value: any) {
  treeForeach(orgList.value, (org: any, parent: any[]): boolean => {
    if (org.new && value[0] !== org.id) {
      parent.splice(parent.indexOf(org), 1);
      return false;
    }
    return true;
  });
  treeForeach(orgList.value, (org: any): boolean => {
    if (org.id === value[0]) {
      currentOrg.value = JSON.parse(JSON.stringify(org));
      currentOrgId.value = currentOrg.value.id;
      if (!currentOrg.value.new) {
        userTableReload();
      }
      return false;
    }
    return true;
  });
}

function expandOrg(value: any) {
  expandedOrg.value = value;
}

function addOrg(parentOrg: any) {
  const id = ulid();
  const newOrg = {
    id: id,
    parentId: parentOrg.id,
    name: 'New org',
    description: '',
    new: true
  };
  if (parentOrg.children) {
    parentOrg.children.push(newOrg);
  } else {
    parentOrg.children = [newOrg];
  }
  if (expandedOrg.value.indexOf(parentOrg.id) === -1) {
    expandedOrg.value.push(parentOrg.id);
  }
  selectedOrg([id]);
}

function validateOrgForm() {
  if (orgFormRef.value) {
    orgFormRef.value.validate((errors: any) => {
      if (!errors) {
        if (currentOrg.value.new) {
          doAddOrg();
        } else {
          doUpdateOrg();
        }
      }
    });
  }
}

function deleteOrg() {
  if (currentOrg.value.new) {
    doGetAllOrg();
    currentOrg.value = null;
    currentOrgId.value = '';
  } else {
    doDeleteOrg();
  }
}

function userTablePageSizeChange(pageSize: number) {
  userTableCheck.value = [];
  userTablePageSize.value = pageSize;
}

function userTablePageChange(page: number) {
  userTableCheck.value = [];
  userTablePage.value = page;
}

function userTableReload() {
  userTableCheck.value = [];
  userTableReloadEvent();
}

function addUsersToOrg() {
  addOrgUsers.value.users = [];
  showAddOrgUsersModal.value = true;
  doGetNotExistOrgUsers();
}

function validateAddOrgUsersForm() {
  if (addOrgUsersFormRef.value) {
    addOrgUsersFormRef.value.validate((errors: any) => {
      if (!errors) {
        doAddUsersToOrg();
      }
    });
  }
}

function userTableHandleCheck(rowKeys: any) {
  userTableCheck.value = rowKeys;
}

function deleteOrgUsers() {
  if (!userTableCheck.value || userTableCheck.value.length === 0) {
    window.$msg.warning(
      undefined,
      t('systemSettings.org.orgUsersDeleteSelectCheck')
    );
    return;
  }
  doRemoveUsersFromOrg();
}
</script>
