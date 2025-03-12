import 'virtual:uno.css';
import '@/style.css';
import type { NotificationApi } from 'naive-ui';
import type { DialogApi } from 'naive-ui';
import type { LoadingBarApi } from 'naive-ui';
import { createApp } from 'vue';
import { createPinia, type Pinia } from 'pinia';
import EventEmitter from 'eventemitter3';
import i18n from '@/commons/i18n';
import msg from '@/commons/msg';
import {
  hasPermissionDirective,
  hasPermissionOrDirective
} from '@/commons/permission';
import router from '@/router';
import http from '@/commons/http';
import App from '@/App.vue';

declare global {
  interface Window {
    $pinia: Pinia;
    $event: EventEmitter;
    $notice: NotificationApi;
    $dialog: DialogApi;
    $loading: LoadingBarApi;
    $msg: typeof msg;
    $http: typeof http;
    $t: typeof i18n.global.t;
  }
}

const pinia: Pinia = createPinia();

window.$pinia = pinia;
window.$event = new EventEmitter();
window.$msg = msg;
window.$http = http;
window.$t = i18n.global.t;

createApp(App)
  .use(pinia)
  .use(i18n)
  .use(router)
  .directive('permission', hasPermissionDirective)
  .directive('permission-or', hasPermissionOrDirective)
  .mount('#app');
