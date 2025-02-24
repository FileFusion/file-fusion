import type { Directive, DirectiveBinding } from 'vue';
import { mainStore } from '@/store';

type PermissionType = string | string[];
interface PermissionDirectiveBinding extends DirectiveBinding {
  value: PermissionType;
}

const hasPermissionDirective: Directive<HTMLElement, PermissionType> = {
  mounted(el, binding) {
    hasPermissionDirectiveUpdated(el, binding, false);
  },
  updated(el, binding) {
    hasPermissionDirectiveUpdated(el, binding, false);
  }
};

const hasPermissionOrDirective: Directive<HTMLElement, PermissionType> = {
  mounted(el, binding) {
    hasPermissionDirectiveUpdated(el, binding, true);
  },
  updated(el, binding) {
    hasPermissionDirectiveUpdated(el, binding, true);
  }
};

function hasPermissionDirectiveUpdated(
  el: HTMLElement,
  binding: PermissionDirectiveBinding,
  or: boolean
) {
  validatePermissionFormat(binding.value);
  el.classList.toggle('permission-hidden', !hasPermission(binding.value, or));
}

function validatePermissionFormat(value: unknown) {
  const isValid =
    typeof value === 'string' ||
    (Array.isArray(value) && value.every((v) => typeof v === 'string'));
  if (!isValid) {
    throw new Error('The parameter format must be an [string...] or a string');
  }
}

function hasPermission(permissions: string | string[], or?: boolean): boolean {
  const mStore = mainStore(window.$pinia);
  const userPermissionIds = mStore.getPermissionIds;
  if (!userPermissionIds) {
    return false;
  }
  if (typeof permissions === 'string') {
    return userPermissionIds.includes(permissions);
  }
  return or
    ? permissions.some((p) => userPermissionIds.includes(p))
    : permissions.every((p) => userPermissionIds.includes(p));
}

function hasPermissionOr(permissions: string | string[]): boolean {
  return hasPermission(permissions, true);
}

export {
  hasPermissionDirective,
  hasPermissionOrDirective,
  hasPermission,
  hasPermissionOr
};
