<template>
  <n-layout-footer bordered class="h-16 text-center leading-16">
    <n-button class="underline" text @click="jumpGithub()">
      File Fusion
    </n-button>
    <n-text> V{{ packageInfo.version }} · Made by </n-text>
    <n-button class="underline" text @click="jumpBlog()"> hackyo</n-button>
  </n-layout-footer>
</template>

<script lang="ts" setup>
import packageInfo from '../../package.json';

function jumpGithub() {
  window.open('https://github.com/FileFusion/file-fusion');
}

function jumpBlog() {
  window.open('https://blog.hackyo.cn');
}
</script>
