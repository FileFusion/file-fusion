<template>
  <router-view v-slot="{ Component }">
    <transition :appear="true" mode="out-in" name="fade-slide">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script lang="ts" setup></script>
