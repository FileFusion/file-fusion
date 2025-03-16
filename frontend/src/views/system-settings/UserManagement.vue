<template>
  <div>
    <n-card hoverable>
      <n-flex justify="space-between">
        <n-flex>
          <n-button
            v-permission="'user_management:add'"
            type="primary"
            @click="addUser()">
            {{ $t('common.add') }}
          </n-button>
          <n-popconfirm
            :positive-button-props="{ type: 'error' }"
            @positive-click="deleteUsers(userTableCheck)">
            <template #trigger>
              <n-button
                v-permission="'user_management:delete'"
                :loading="deleteUserLoading"
                type="error">
                {{ $t('common.delete') }}
              </n-button>
            </template>
            {{ $t('common.batchDeleteConfirm') }}
          </n-popconfirm>
        </n-flex>
        <n-flex :wrap="false" justify="end">
          <n-input-group>
            <n-input
              v-model:value="userPattern"
              :placeholder="
                $t('common.search') +
                ' ' +
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
                  <i-search />
                </n-icon>
              </template>
            </n-input>
            <n-button ghost type="primary" @click="userTableReload()">
              {{ $t('common.search') }}
            </n-button>
          </n-input-group>
        </n-flex>
      </n-flex>
      <n-data-table
        :bordered="false"
        :checked-row-keys="userTableCheck"
        :columns="userTableColumns"
        :data="userTableData"
        :loading="userTableLoading"
        :pagination="{
          page: userTablePage,
          pageSize: userTablePageSize,
          pageSizes: [10, 20, 50],
          itemCount: userTableTotal,
          showSizePicker: true,
          showQuickJumper: true,
          prefix: (pagination: PaginationInfo) => {
            return $t('common.total') + ': ' + pagination.itemCount;
          }
        }"
        :row-key="(row: any) => row.id"
        remote
        class="mt-3"
        @update:sorter="userTableHandleSorter"
        @update:page="userTablePageChange"
        @update:page-size="userTablePageSizeChange"
        @update:checked-row-keys="userTableHandleCheck" />
    </n-card>
    <n-drawer v-model:show="showUserEdit" :width="502" placement="right">
      <n-drawer-content>
        <template #header>
          {{
            (showUserEditIsAdd ? $t('common.add') : $t('common.edit')) +
            $t('common.user')
          }}
        </template>
        <n-spin
          :show="getAllRoleLoading || addUserLoading || updateUserLoading">
          <n-form
            ref="profileFormRef"
            :model="currentOptionUser"
            :rules="profileRules">
            <n-grid :cols="24">
              <n-form-item-gi
                :label="$t('userSettings.profile.username')"
                :span="24"
                path="username">
                <n-input
                  v-model:value="currentOptionUser.username"
                  :disabled="!showUserEditIsAdd"
                  :placeholder="$t('userSettings.profile.username')"
                  clearable
                  maxlength="100"
                  show-count />
              </n-form-item-gi>
              <n-form-item-gi
                :label="$t('userSettings.profile.name')"
                :span="24"
                path="name">
                <n-input
                  v-model:value="currentOptionUser.name"
                  :placeholder="$t('userSettings.profile.name')"
                  clearable
                  maxlength="50"
                  show-count />
              </n-form-item-gi>
              <n-form-item-gi
                :label="$t('userSettings.profile.email')"
                :span="24"
                path="email">
                <n-auto-complete
                  v-model:value="currentOptionUser.email"
                  :options="emailAutoCompleteOptions">
                  <template
                    #default="{
                      handleInput,
                      handleBlur,
                      handleFocus,
                      value: slotValue
                    }">
                    <n-input
                      :placeholder="$t('userSettings.profile.email')"
                      :status="emailAutoCompleteStatus"
                      :value="slotValue"
                      clearable
                      maxlength="100"
                      show-count
                      @blur="handleBlur"
                      @focus="handleFocus"
                      @input="handleInput" />
                  </template>
                </n-auto-complete>
              </n-form-item-gi>
              <n-form-item-gi
                :label="$t('userSettings.profile.phone')"
                :span="24"
                path="phone">
                <n-input-group>
                  <n-select
                    v-model:value="currentOptionUser.areaCode"
                    :options="areaCodes(language)"
                    :placeholder="$t('userSettings.profile.areaCode')"
                    clearable
                    filterable />
                  <n-input
                    v-model:value="currentOptionUser.phone"
                    :placeholder="$t('userSettings.profile.phone')"
                    clearable
                    maxlength="40"
                    show-count />
                </n-input-group>
              </n-form-item-gi>
              <n-form-item-gi
                :label="$t('common.role')"
                :span="24"
                path="roleIds">
                <n-select
                  v-model:value="currentOptionUser.roleIds"
                  :disabled="currentOptionUser.systemdUser"
                  :options="allRoles"
                  label-field="name"
                  value-field="id"
                  :placeholder="
                    $t('systemSettings.user.userRolesSelectPlaceholder')
                  "
                  clearable
                  filterable
                  multiple />
              </n-form-item-gi>
              <n-form-item-gi
                v-if="showUserEditIsAdd"
                :label="$t('login.password')"
                :span="24"
                path="password">
                <n-input
                  v-model:value="currentOptionUser.password"
                  :placeholder="$t('login.password')"
                  clearable
                  maxlength="64"
                  show-count />
              </n-form-item-gi>
              <n-form-item-gi
                v-if="!showUserEditIsAdd"
                :label="$t('userSettings.profile.changePassword')"
                :span="6"
                path="changePassword">
                <n-switch v-model:value="changePassword" />
              </n-form-item-gi>
              <n-form-item-gi
                v-if="!showUserEditIsAdd && changePassword"
                :label="$t('userSettings.profile.newPassword')"
                :span="18"
                path="password">
                <n-input
                  v-model:value="newPassword"
                  :placeholder="$t('userSettings.profile.newPassword')"
                  clearable
                  maxlength="64"
                  show-count />
              </n-form-item-gi>
              <n-form-item-gi
                :label="$t('userSettings.profile.accountStatus.enabled')"
                :span="24"
                path="enabled">
                <n-switch
                  v-model:value="currentOptionUser.enabled"
                  :disabled="currentOptionUser.systemdUser" />
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </n-spin>
        <template #footer>
          <n-button @click="showUserEdit = false">
            {{ $t('common.cancel') }}
          </n-button>
          <n-button class="ml-3" type="primary" @click="validateProfileForm()"
            >{{ $t('common.save') }}
          </n-button>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script lang="ts" setup>
import type {
  DataTableColumn,
  DataTableSortState,
  FormRules,
  FormItemRule,
  PaginationInfo
} from 'naive-ui';
import { NButton, NDropdown, NGi, NGrid, NTag, NText } from 'naive-ui';
import { computed, h, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import IconDelete from '~icons/icon-park-outline/delete';
import IconEditTwo from '~icons/icon-park-outline/edit-two';
import IconDown from '~icons/icon-park-outline/down';
import { useRequest, usePagination } from 'alova/client';
import emailSuffix from '@/commons/email-suffix';
import areaCodes from '@/commons/area-codes';
import { mainStore } from '@/store';
import { hasPermission } from '@/commons/permission';
import { renderIconMethod } from '@/commons/utils';

const mStore = mainStore();
const { t } = useI18n();
const http = window.$http;
const language = computed(() => mStore.getLanguage);
const profileFormRef = ref<HTMLFormElement>();

const userPattern = ref<string>('');
const userTableCheck = ref<string[]>([]);
const userTableSorter = ref<DataTableSortState | null>(null);
const showUserEdit = ref(false);
const showUserEditIsAdd = ref(true);
const currentOptionUser = ref({
  id: null,
  username: '',
  name: '',
  email: '',
  areaCode: null,
  phone: '',
  password: '',
  systemdUser: false,
  enabled: true,
  roleIds: <string[]>[]
});
const emailAutoCompleteStatus = ref<'success' | 'warning' | 'error'>('success');
const changePassword = ref(false);
const newPassword = ref('');

const permission = ref({
  userManagementEdit: hasPermission('user_management:edit'),
  userManagementDelete: hasPermission('user_management:delete')
});

const profileRules = computed<FormRules>(() => {
  return {
    username: [
      {
        required: true,
        message: t('userSettings.profile.validator.usernameEmpty'),
        trigger: ['input', 'blur']
      }
    ],
    name: [
      {
        required: true,
        message: t('userSettings.profile.validator.nameEmpty'),
        trigger: ['input', 'blur']
      }
    ],
    email: [
      {
        validator(_rule: FormItemRule, value: string) {
          if (value) {
            const regular = new RegExp(
              /^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]+$/
            );
            if (!regular.test(value)) {
              emailAutoCompleteStatus.value = 'error';
              return new Error(t('userSettings.profile.validator.emailError'));
            }
          }
          emailAutoCompleteStatus.value = 'success';
          return true;
        },
        trigger: ['input', 'blur']
      }
    ],
    phone: [
      {
        validator(_rule: FormItemRule, value: string) {
          if (currentOptionUser.value.areaCode || value) {
            if (!value) {
              return new Error(t('userSettings.profile.validator.phoneEmpty'));
            }
            const regular = new RegExp(/^\d+$/);
            if (!regular.test(value)) {
              return new Error(t('userSettings.profile.validator.phoneError'));
            }
            if (!currentOptionUser.value.areaCode) {
              return new Error(
                t('userSettings.profile.validator.areaCodeEmpty')
              );
            }
          }
          return true;
        },
        trigger: ['input', 'blur']
      }
    ],
    password: [
      {
        required: true,
        validator() {
          let value = '';
          if (!showUserEditIsAdd.value && changePassword.value) {
            value = newPassword.value;
          } else {
            value = currentOptionUser.value.password;
          }
          if (!value || value.trim() === '') {
            return new Error(t('userSettings.profile.validator.passwordEmpty'));
          }
          const regular = new RegExp(
            /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{6,64}$/
          );
          if (!regular.test(value)) {
            return new Error(t('userSettings.profile.validator.passwordError'));
          }
          return true;
        },
        trigger: ['input', 'blur']
      }
    ]
  };
});

const emailAutoCompleteOptions = computed(() => {
  const email = currentOptionUser.value.email;
  if (!email) {
    return [];
  }
  let mailsFilter = [];
  let suffix = email.split('@')[1];
  suffix = '@' + (suffix ? suffix : '');
  for (const es of emailSuffix) {
    if (es.startsWith(suffix)) {
      mailsFilter.push(es);
    }
  }
  return mailsFilter.map((mail) => {
    const prefix = email.split('@')[0];
    return {
      label: prefix + mail,
      value: prefix + mail
    };
  });
});

const userTableColumns = computed<DataTableColumn[]>(() => {
  const tableColumn: DataTableColumn[] = [
    {
      type: 'selection'
    },
    {
      type: 'expand',
      expandable: () => true,
      renderExpand: (row) => {
        return h(
          NGrid,
          {
            cols: 4,
            class: 'px-6 py-3'
          },
          {
            default: () => [
              h(
                NGi,
                {
                  span: 4
                },
                {
                  default: () =>
                    h(
                      NText,
                      {
                        strong: true
                      },
                      {
                        default: () =>
                          t('userSettings.profile.accountStatus.title')
                      }
                    )
                }
              ),
              h(
                NGi,
                {
                  span: 1
                },
                {
                  default: () =>
                    h(
                      NText,
                      {
                        type: row.systemdUser ? 'warning' : 'success'
                      },
                      {
                        default: () =>
                          t('userSettings.profile.accountStatus.systemdUser') +
                          ': ' +
                          (row.systemdUser ? t('common.yes') : t('common.no'))
                      }
                    )
                }
              ),
              h(
                NGi,
                {
                  span: 1
                },
                {
                  default: () =>
                    h(
                      NText,
                      {
                        type: row.nonExpired ? 'success' : 'error'
                      },
                      {
                        default: () =>
                          t('userSettings.profile.accountStatus.nonExpired') +
                          ': ' +
                          (row.nonExpired ? t('common.yes') : t('common.no'))
                      }
                    )
                }
              ),
              h(
                NGi,
                {
                  span: 1
                },
                {
                  default: () =>
                    h(
                      NText,
                      {
                        type: row.credentialsNonExpired ? 'success' : 'error'
                      },
                      {
                        default: () =>
                          t(
                            'userSettings.profile.accountStatus.credentialsNonExpired'
                          ) +
                          ': ' +
                          (row.credentialsNonExpired
                            ? t('common.yes')
                            : t('common.no'))
                      }
                    )
                }
              ),
              h(
                NGi,
                {
                  span: 1
                },
                {
                  default: () =>
                    h(
                      NText,
                      {
                        type: row.nonLocked ? 'success' : 'error'
                      },
                      {
                        default: () =>
                          t('userSettings.profile.accountStatus.nonLocked') +
                          ': ' +
                          (row.nonLocked ? t('common.yes') : t('common.no'))
                      }
                    )
                }
              )
            ]
          }
        );
      }
    },
    {
      title: t('userSettings.profile.username'),
      key: 'username',
      resizable: true,
      sorter: true
    },
    {
      title: t('userSettings.profile.name'),
      key: 'name',
      resizable: true,
      sorter: true
    },
    {
      title: t('userSettings.profile.email'),
      key: 'email',
      resizable: true
    },
    {
      title: t('userSettings.profile.phone'),
      key: 'phone',
      resizable: true,
      render: (row: any) => {
        if (row.areaCode && row.phone) {
          return row.areaCode + row.phone;
        }
        return '';
      }
    },
    {
      title: t('common.role'),
      key: 'roles',
      resizable: true,
      render: (row: any) => {
        let roles = [];
        for (const roleId of row.roleIds) {
          roles.push(
            h(
              'div',
              {
                class: 'mt-1'
              },
              [
                h(
                  NTag,
                  {
                    type: 'success'
                  },
                  {
                    default: () => roleIdNameMap.value[roleId]
                  }
                )
              ]
            )
          );
        }
        return roles;
      }
    },
    {
      title: t('userSettings.profile.accountStatus.enabled'),
      key: 'enabled',
      width: 80,
      sorter: true,
      render: (row: any) => {
        return h(
          NTag,
          {
            type: row.enabled ? 'success' : 'warning'
          },
          {
            default: () => (row.enabled ? t('common.yes') : t('common.no'))
          }
        );
      }
    }
  ];
  if (
    permission.value.userManagementEdit ||
    permission.value.userManagementDelete
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
                label: t('common.edit'),
                props: {
                  onClick: () => {
                    editUser(row);
                  }
                },
                show: permission.value.userManagementEdit
              },
              {
                icon: renderIconMethod(IconDelete),
                key: 'delete',
                label: t('common.delete'),
                props: {
                  onClick: () => {
                    deleteUser(row);
                  }
                },
                show: !row.systemdUser && permission.value.userManagementDelete
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

const getUserTableSorter = computed(() => {
  if (!userTableSorter.value || !userTableSorter.value.order) {
    return false;
  }
  return (
    'sorter=' +
    userTableSorter.value.columnKey +
    '&sorterOrder=' +
    userTableSorter.value.order
  );
});

const roleIdNameMap = computed(() => {
  let map: any = {};
  if (allRoles.value) {
    for (let role of allRoles.value) {
      map[role.id] = role.name;
    }
  }
  return map;
});
const { loading: getAllRoleLoading, data: allRoles } = useRequest(() =>
  http.Get<any[]>('/role')
);

const {
  loading: userTableLoading,
  data: userTableData,
  page: userTablePage,
  total: userTableTotal,
  pageSize: userTablePageSize,
  reload: userTableReloadEvent
} = usePagination(
  (page, pageSize) => {
    const sorter = getUserTableSorter.value;
    return http.Get<any>(
      '/user/' +
        page +
        '/' +
        pageSize +
        '?search=' +
        userPattern.value +
        (sorter ? '&' + sorter : '')
    );
  },
  {
    total: (res) => res.totalElements,
    data: (res) => res.content
  }
);

const { loading: deleteUserLoading, send: doDeleteUser } = useRequest(
  (userId: string) => http.Delete('/user/' + userId),
  {
    immediate: false
  }
);

const { loading: addUserLoading, send: doAddUser } = useRequest(
  (user: any) => http.Post('/user', user),
  {
    immediate: false
  }
).onSuccess(() => {
  addOrUpdateUserSuccess();
});

const { loading: updateUserLoading, send: doUpdateUser } = useRequest(
  (user: any) => http.Put('/user', user),
  {
    immediate: false
  }
).onSuccess(() => {
  addOrUpdateUserSuccess();
});

function addOrUpdateUserSuccess() {
  window.$msg.success(t('common.saveSuccess'));
  showUserEdit.value = false;
  userTableReload();
}

function userTableHandleCheck(rowKeys: Array<string | number>) {
  userTableCheck.value = <string[]>rowKeys;
}

function userTableHandleSorter(params: DataTableSortState | null) {
  userTableSorter.value = params;
  userTableReload();
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

function addUser() {
  currentOptionUser.value = {
    id: null,
    username: '',
    name: '',
    email: '',
    areaCode: null,
    phone: '',
    password: '',
    systemdUser: false,
    enabled: true,
    roleIds: []
  };
  showUserEditIsAdd.value = true;
  showUserEdit.value = true;
}

function editUser(user: any) {
  currentOptionUser.value = JSON.parse(JSON.stringify(user));
  changePassword.value = false;
  newPassword.value = '';
  showUserEditIsAdd.value = false;
  showUserEdit.value = true;
}

function deleteUser(user: any) {
  window.$dialog.warning({
    title: t('common.warning'),
    content: t('common.deleteConfirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => {
      deleteUsers([user.id]);
    }
  });
}

async function deleteUsers(userIds: string[]) {
  if (!userIds || userIds.length === 0) {
    window.$msg.warning(t('systemSettings.user.usersDeleteSelectCheck'));
    return;
  }
  for (const userId of userIds) {
    await doDeleteUser(userId);
  }
  window.$msg.success(t('common.deleteSuccess'));
  userTableReload();
}

function validateProfileForm() {
  if (profileFormRef.value) {
    profileFormRef.value.validate((errors: any) => {
      if (!errors) {
        const form = JSON.parse(JSON.stringify(currentOptionUser.value));
        if (!showUserEditIsAdd.value && changePassword.value) {
          form.password = newPassword.value;
        }
        if (showUserEditIsAdd.value) {
          doAddUser(form);
        } else {
          doUpdateUser(form);
        }
      }
    });
  }
}
</script>
