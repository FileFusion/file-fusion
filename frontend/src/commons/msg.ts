import type { NotificationType } from 'naive-ui';
import { format } from '@/commons/date';

function info(title: string | number | null, content?: string) {
  notify('info', title ? title : window.$t('common.info'), content);
}

function success(title: string | number | null, content?: string) {
  notify('success', title ? title : window.$t('common.success'), content);
}

function warning(title: string | number | null, content?: string) {
  notify('warning', title ? title : window.$t('common.warning'), content);
}

function error(title: string | number | null, content?: string) {
  notify('error', title ? title : window.$t('common.error'), content);
}

function notify(
  type: NotificationType,
  title: string | number,
  content?: string
) {
  window.$notice[type]({
    title: title.toString(),
    content: content,
    meta: format(new Date()),
    duration: 3000
  });
}

export default {
  info,
  success,
  warning,
  error
};
