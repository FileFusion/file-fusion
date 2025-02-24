<template>
  <n-layout class="h-full">
    <navigation-bar></navigation-bar>
    <n-layout class="absolute inset-0 top-16" has-sider>
      <side-menu></side-menu>
      <n-layout :native-scrollbar="false">
        <breadcrumb-bar class="m-2"></breadcrumb-bar>
        <n-layout-content class="content-min-height" embedded>
          <div class="m-2">
            <router-view-content></router-view-content>
          </div>
        </n-layout-content>
        <upload-view></upload-view>
        <n-back-top :bottom="88" :right="24" />
        <footer-bar></footer-bar>
      </n-layout>
    </n-layout>
  </n-layout>
</template>

<script lang="ts" setup>
import UploadView from '@/views/files/components/UploadView.vue';
</script>
