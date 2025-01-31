import { createAlova } from 'alova';
import adapterFetch from 'alova/fetch';
import VueHook from 'alova/vue';
import router from '@/router';
import { mainStore } from '@/store';

const http = createAlova({
  baseURL: '/api',
  statesHook: VueHook,
  requestAdapter: adapterFetch(),
  cacheFor: null,
  beforeRequest: (method) => {
    window.$loading.start();
    const mStore = mainStore(window.$pinia);
    method.config.headers['Accept-Language'] = mStore.getLanguage;
    const token = mStore.getToken;
    if (token) {
      method.config.headers['Authorization'] = token;
    }
  },
  responded: {
    onSuccess: async (response) => {
      let data = null;
      try {
        data = await response.json();
      } catch (err) {
        console.error(err);
      }
      if (data && data.success) {
        window.$loading.finish();
        return data.data;
      } else {
        window.$loading.error();
        if (response.status === 401) {
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
        if (data && data.code) {
          window.$msg.error(data.code, data.message);
          throw new Error(data.message);
        } else {
          window.$msg.error(response.status, response.statusText);
          throw new Error(response.statusText);
        }
      }
    },
    onError: (err) => {
      window.$loading.error();
      window.$msg.error(err.name, err.message);
    }
  }
});

export default http;
