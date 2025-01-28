<template>
  <router-view v-slot="{ Component }">
    <transition name="fade-slide">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script lang="ts" setup></script>
