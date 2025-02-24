import type { Directive, DirectiveBinding } from 'vue';
import { mainStore } from '@/store';

export type PermissionType = string | string[];
interface PermissionDirectiveBinding extends DirectiveBinding {
  value: PermissionType;
}

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

export function hasPermission(
  permissions: PermissionType,
  or?: boolean
): boolean {
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

export function hasPermissionOr(permissions: PermissionType): boolean {
  return hasPermission(permissions, true);
}

const createPermissionDirective = (isOr = false): Directive<HTMLElement> => ({
  mounted: (el, binding) => hasPermissionDirectiveUpdated(el, binding, isOr),
  updated: (el, binding) => hasPermissionDirectiveUpdated(el, binding, isOr)
});

export const hasPermissionDirective = createPermissionDirective();
export const hasPermissionOrDirective = createPermissionDirective(true);
