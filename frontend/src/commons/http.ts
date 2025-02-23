import type { AxiosResponse, AxiosError } from 'axios';
import { createAlova } from 'alova';
import { axiosRequestAdapter } from '@alova/adapter-axios';
import VueHook from 'alova/vue';
import router from '@/router';
import { mainStore } from '@/store';

const http = createAlova({
  baseURL: '/api',
  statesHook: VueHook,
  requestAdapter: axiosRequestAdapter(),
  cacheFor: null,
  beforeRequest: (method) => {
    if (!method.meta || method.meta.loading !== false) {
      window.$loading.start();
    }
    const mStore = mainStore(window.$pinia);
    method.config.headers['Accept-Language'] = mStore.getLanguage;
    const token = mStore.getToken;
    if (token) {
      method.config.headers['Authorization'] = token;
    }
  },
  responded: {
    onSuccess: (response: AxiosResponse) => {
      const data = response.data;
      if (response.config.responseType === 'blob') {
        window.$loading.finish();
        return data;
      }
      if (data.success) {
        window.$loading.finish();
        return data.data;
      }
      window.$loading.error();
      window.$msg.error(data.code, data.message);
      throw new Error(data.code + ': ' + data.message);
    },
    onError: (err: AxiosError) => {
      window.$loading.error();
      let code;
      let message;
      if (err.response) {
        if (err.response.data) {
          const data: any = err.response.data;
          code = data.code;
          message = data.message;
        } else {
          code = err.response.status;
          message = err.response.statusText;
        }
      } else {
        code = err.code;
        message = err.message;
      }

      if (code === 401) {
        const mStore = mainStore(window.$pinia);
        mStore.setToken(null);
        const currentRoute = router.currentRoute.value;
        if (currentRoute.name !== 'login') {
          router
            .push({
              name: 'login',
              query: { redirect: currentRoute.path }
            })
            .then();
        }
      }

      window.$msg.error(code, message);
      throw new Error(code + ': ' + message);
    }
  }
});

export default http;
