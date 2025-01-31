<template>
  <n-layout class="h-full">
    <n-result
      :title="$t('noFound.title')"
      size="large"
      status="404"
      class="relative top-1/2 -translate-y-2/4">
      <template #footer>
        <n-button @click="backHome()">{{ $t('noFound.backHome') }}</n-button>
      </template>
    </n-result>
  </n-layout>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router';

const router = useRouter();

function backHome() {
  router.push({ name: 'home' });
}
</script>
