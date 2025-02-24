<template>
  <n-modal
    v-model:show="showVideoModal"
    style="width: 80vw; height: 80vh"
    preset="card"
    title="视频播放"
    :bordered="false"
    content-style="padding: 0;"
    @after-enter="initPlayer()"
    @after-leave="destroyPlayer">
    <div ref="playerContainer"></div>
  </n-modal>
</template>

<script lang="ts" setup>
import type PresetPlayer from 'xgplayer';
import Player from 'xgplayer';
import 'xgplayer/dist/index.min.css';
import { ref, onBeforeUnmount } from 'vue';
import { useThemeVars } from 'naive-ui';

const themeVars = useThemeVars();

const showVideoModal = ref<boolean>(false);
setTimeout(() => {
  showVideoModal.value = true;
}, 3000);

const playerContainer = ref<HTMLElement | undefined>(undefined);
const playerInstance = ref<PresetPlayer | null>(null);

function initPlayer() {
  if (!playerContainer.value) {
    return;
  }
  if (playerInstance.value) {
    destroyPlayer();
  }
  playerInstance.value = new Player({
    el: playerContainer.value,
    url: 'https://lf3-static.bytednsdoc.com/obj/eden-cn/nupenuvpxnuvo/xgplayer_doc/xgplayer-demo-720p.mp4',
    height: '100%',
    width: '100%',
    videoFillMode: 'contain',
    lang: 'zh-cn',
    pip: true,
    commonStyle: {
      playedColor: themeVars.value.primaryColor,
      volumeColor: themeVars.value.primaryColor
    }
  });
}

function destroyPlayer() {
  if (playerInstance.value) {
    playerInstance.value.destroy();
    playerInstance.value = null;
  }
}

onBeforeUnmount(() => {
  destroyPlayer();
});
</script>

<style>
.xgplayer .xg-options-list li.selected {
  color: var(--primary-color);
}
</style>
