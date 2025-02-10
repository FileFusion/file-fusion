<template>
  <n-card hoverable>
    <n-grid :cols="4" x-gap="24">
      <n-gi :span="1">
        <n-flex :size="12" vertical>
          <n-flex :wrap="false" justify="space-between">
            <n-input
              v-model:value="rolePattern"
              :placeholder="$t('common.search')"
              clearable>
              <template #prefix>
                <n-icon>
                  <i-search />
                </n-icon>
              </template>
            </n-input>
            <n-button
              v-permission="'role:add'"
              ghost
              type="primary"
              @click="addRole()"
              >{{ $t('common.add') }}</n-button
            >
          </n-flex>
          <n-spin
            :show="
              getAllPermissionLoading ||
              getAllBasicsPermissionLoading ||
              getAllRoleLoading
            ">
            <n-tree
              :cancelable="false"
              :data="roles"
              :pattern="rolePattern"
              :selected-keys="[currentRoleId]"
              :virtual-scroll="true"
              block-line
              key-field="id"
              label-field="name"
              @update:selected-keys="selectedRole" />
          </n-spin>
        </n-flex>
      </n-gi>
      <n-gi v-if="currentRole" :span="3">
        <n-spin
          :show="addRoleLoading || updateRoleLoading || deleteRoleLoading">
          <n-form
            ref="roleFormRef"
            :model="currentRole"
            :rules="roleFormRules"
            inline>
            <n-form-item
              :label="$t('systemSettings.role.roleName')"
              path="name">
              <n-input
                v-model:value="currentRole.name"
                :placeholder="$t('systemSettings.role.roleName')"
                clearable
                maxlength="100"
                show-count />
            </n-form-item>
            <n-form-item :label="$t('common.description')" path="description">
              <n-input
                v-model:value="currentRole.description"
                :placeholder="$t('common.description')"
                clearable
                maxlength="500"
                show-count />
            </n-form-item>
            <n-form-item v-permission="'role:edit'">
              <n-button type="primary" @click="validateRoleForm()">
                {{ $t('common.save') }}
              </n-button>
            </n-form-item>
            <n-form-item
              v-if="!currentRole.systemRole"
              v-permission="'role:delete'">
              <n-popconfirm
                :positive-button-props="{ type: 'error' }"
                @positive-click="deleteRole()">
                <template #trigger>
                  <n-button type="error">
                    {{ $t('common.delete') }}
                  </n-button>
                </template>
                {{ $t('common.deleteConfirm') }}
              </n-popconfirm>
            </n-form-item>
          </n-form>
          <div>
            <div>
              <n-text strong>
                {{ $t('systemSettings.role.rolePermissions') }}
              </n-text>
            </div>
            <n-flex :size="12" vertical class="mt-3">
              <n-input
                v-model:value="permissionPattern"
                :placeholder="$t('common.search')"
                clearable>
                <template #prefix>
                  <n-icon>
                    <i-search />
                  </n-icon>
                </template>
              </n-input>
              <n-spin :show="getRolePermissionLoading">
                <n-tree
                  :checked-keys="permissionsChecked"
                  :data="permissions"
                  :pattern="permissionPattern"
                  block-line
                  cascade
                  checkable
                  key-field="id"
                  label-field="name"
                  @update:checked-keys="updatePermissionsChecked" />
              </n-spin>
            </n-flex>
          </div>
        </n-spin>
      </n-gi>
      <n-gi v-if="!currentRole" :span="3" class="mt-6">
        <n-empty
          :description="$t('systemSettings.role.chooseRoleFirst')"></n-empty>
      </n-gi>
    </n-grid>
  </n-card>
</template>

<script lang="ts" setup>
import type { FormRules } from 'naive-ui';
import { computed, h, ref } from 'vue';
import { NText } from 'naive-ui';
import { useI18n } from 'vue-i18n';
import { ulid } from 'ulidx';
import { useRequest } from 'alova/client';
import { arrayToTreeCustom } from '@/commons/utils';
import { hasPermission } from '@/commons/permission';

const { t } = useI18n();
const http = window.$http;
const roleFormRef = ref<HTMLFormElement>();

const hasPermissionValue = ref({
  roleEdit: hasPermission('role:edit')
});

const rolePattern = ref<string>('');
const permissions = ref<any[]>([]);
const permissionPattern = ref<string>('');
const currentRoleId = ref<string>('');
const currentRole = ref<any>(null);

const roleFormRules = computed<FormRules>(() => {
  return {
    name: [
      {
        required: true,
        message: t('systemSettings.role.roleNameValidator'),
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

const { loading: getAllPermissionLoading, data: permissionSources } =
  useRequest(() => http.Get<any[]>('/permission'), {
    initialData: []
  });

const {
  loading: getAllBasicsPermissionLoading,
  data: basicsPermissionSources
} = useRequest(() => http.Get<any[]>('/permission?basics=true'), {
  initialData: []
});

const {
  loading: getAllRoleLoading,
  data: roles,
  send: doGetAllRole
} = useRequest(() => http.Get<any[]>('/role'), {
  initialData: []
}).onSuccess(() => {
  for (let role of roles.value) {
    if (role.systemRole) {
      role.suffix = () =>
        h(
          NText,
          { depth: 3 },
          { default: () => t('systemSettings.role.systemRole') }
        );
    }
  }
});

const {
  loading: addRoleLoading,
  data: addRoleRes,
  send: doAddRole
} = useRequest(
  () =>
    http.Post<any>('/role', {
      role: currentRole.value,
      permissions: permissionsChecked.value
    }),
  {
    immediate: false
  }
).onSuccess(() => {
  addOrUpdateRoleSuccess(addRoleRes.value);
});

const {
  loading: updateRoleLoading,
  data: updateRoleRes,
  send: doUpdateRole
} = useRequest(
  () =>
    http.Put<any>('/role', {
      role: currentRole.value,
      permissions: permissionsChecked.value
    }),
  {
    immediate: false
  }
).onSuccess(() => {
  addOrUpdateRoleSuccess(updateRoleRes.value);
});

function addOrUpdateRoleSuccess(res: any) {
  window.$msg.success(t('common.saveSuccess'));
  doGetAllRole();
  currentRole.value = res;
  currentRoleId.value = res.id;
}

const { loading: deleteRoleLoading, send: doDeleteRole } = useRequest(
  () => http.Delete('/role/' + currentRoleId.value),
  {
    immediate: false
  }
).onSuccess(() => {
  window.$msg.success(t('common.deleteSuccess'));
  doGetAllRole();
  currentRole.value = null;
  currentRoleId.value = '';
});

const {
  loading: getRolePermissionLoading,
  data: permissionsChecked,
  send: doGetRolePermissionRole
} = useRequest(
  () => http.Get<string[]>('/role/' + currentRoleId.value + '/permission'),
  {
    immediate: false,
    initialData: []
  }
);

function selectedRole(value: string[]) {
  for (let i = 0; i < roles.value.length; i++) {
    if (roles.value[i].new && value[0] !== roles.value[i].id) {
      roles.value.splice(i, 1);
      i--;
    }
  }
  for (const role of roles.value) {
    if (role.id === value[0]) {
      currentRole.value = JSON.parse(JSON.stringify(role));
      currentRoleId.value = currentRole.value.id;
      for (const basicsPermission of basicsPermissionSources.value) {
        permissionsChecked.value.push(basicsPermission.id);
      }
      if (!currentRole.value.new) {
        doGetRolePermissionRole();
      }
      updateAllPermission(
        currentRole.value.systemRole || !hasPermissionValue.value.roleEdit
      );
      break;
    }
  }
}

function updateAllPermission(disabled: boolean) {
  const ps = JSON.parse(JSON.stringify(permissionSources.value));
  for (let permission of ps) {
    permission.suffix = () =>
      h(NText, { depth: 3 }, { default: () => permission.description });
    permission.disabled = disabled ? true : permission.basics;
  }
  permissions.value = arrayToTreeCustom(ps, 'root', 'id', 'parentId');
}

function addRole() {
  const id = ulid();
  roles.value.push({
    id: id,
    name: 'New role',
    description: '',
    new: true
  });
  selectedRole([id]);
}

function validateRoleForm() {
  if (roleFormRef.value) {
    roleFormRef.value.validate((errors: any) => {
      if (!errors) {
        if (currentRole.value.new) {
          doAddRole();
        } else {
          doUpdateRole();
        }
      }
    });
  }
}

function deleteRole() {
  if (currentRole.value.new) {
    doGetAllRole();
    currentRole.value = null;
    currentRoleId.value = '';
  } else {
    doDeleteRole();
  }
}

function updatePermissionsChecked(permissionIds: string[]) {
  permissionsChecked.value = permissionIds;
}
</script>
