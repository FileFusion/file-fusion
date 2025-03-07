<template>
  <n-modal
    v-model:show="model"
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
            v-model:value="renameFileForm.name"
            :placeholder="$t('files.personal.fileName')"
            clearable
            maxlength="255"
            show-count />
        </n-form-item>
      </n-form>
      <div class="text-right">
        <n-button size="small" type="primary" @click="validateRenameFileForm()">
          {{ $t('common.save') }}
        </n-button>
      </div>
    </n-spin>
  </n-modal>
</template>

<script setup lang="ts">
import type { FormItemRule, FormRules } from 'naive-ui';
import { computed, ref, watch } from 'vue';
import { useRequest } from 'alova/client';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const http = window.$http;

const emit = defineEmits(['submit']);
const model = defineModel<boolean>();
const props = defineProps({
  id: { type: String, required: true },
  name: { type: String, required: true }
});

const renameFileFormRef = ref<HTMLFormElement>();
const renameFileForm = ref({
  name: ''
});
const renameFileFormRules = computed<FormRules>(() => {
  return {
    name: [
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

const { loading: renameFileLoading, send: doRenameFile } = useRequest(
  () =>
    http.Put('/file_data/_rename', {
      id: props.id,
      name: renameFileForm.value.name
    }),
  {
    immediate: false
  }
).onSuccess(() => {
  window.$msg.success(t('common.saveSuccess'));
  model.value = false;
  emit('submit');
});

function validateRenameFileForm() {
  if (renameFileFormRef.value) {
    renameFileFormRef.value.validate((errors: any) => {
      if (!errors) {
        doRenameFile();
      }
    });
  }
}

watch(model, (newValue) => {
  if (newValue) {
    renameFileForm.value.name = props.name;
  } else {
    renameFileForm.value.name = '';
  }
});
</script>
