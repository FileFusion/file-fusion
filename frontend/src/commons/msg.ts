import type { NotificationType } from 'naive-ui';
import { format } from 'date-fns';

function info(title: string | number | undefined, content?: string) {
  notify('info', title ? title : window.$t('common.info'), content);
}

function success(title: string | number | undefined, content?: string) {
  notify('success', title ? title : window.$t('common.success'), content);
}

function warning(title: string | number | undefined, content?: string) {
  notify('warning', title ? title : window.$t('common.warning'), content);
}

function error(title: string | number | undefined, content?: string) {
  notify('error', title ? title : window.$t('common.error'), content);
}

function notify(
  type: NotificationType,
  title: string | number,
  content?: string
) {
  window.$notice[type]({
    title: title + '',
    content: content,
    meta: format(new Date(), 'yyyy-MM-dd HH:mm:ss'),
    duration: 3000
  });
}

export default {
  info,
  success,
  warning,
  error
};
