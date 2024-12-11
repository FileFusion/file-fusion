import type { Directive } from 'vue';
import { mainStore } from '@/store';

const hasPermissionDirective: Directive = {
  mounted(el: any, binding: any) {
    hasPermissionDirectiveUpdated(el, binding, false);
  },
  updated(el: any, binding: any) {
    hasPermissionDirectiveUpdated(el, binding, false);
  }
};

const hasPermissionOrDirective: Directive = {
  mounted(el: any, binding: any) {
    hasPermissionDirectiveUpdated(el, binding, true);
  },
  updated(el: any, binding: any) {
    hasPermissionDirectiveUpdated(el, binding, true);
  }
};

function hasPermissionDirectiveUpdated(el: any, binding: any, or: boolean) {
  let formatValidation = true;
  if (typeof binding.value !== 'string' && !(binding.value instanceof Array)) {
    formatValidation = false;
  } else if (binding.value instanceof Array) {
    for (const value of binding.value) {
      if (typeof value !== 'string') {
        formatValidation = false;
        break;
      }
    }
  }
  if (!formatValidation) {
    throw new Error('The parameter format must be an [string...] or a string');
  }
  if (!hasPermission(binding.value, or)) {
    el.style.display = 'none';
  } else {
    el.style.display = '';
  }
}

function hasPermission(permissions: string | string[], or?: boolean): boolean {
  const mStore = mainStore(window.$pinia);
  const userPermissions = mStore.getPermissions;
  if (!userPermissions) {
    return true;
  }
  const userPermissionIds: string[] = userPermissions.map(
    (permission: any): string => {
      return permission.id;
    }
  );
  if (typeof permissions === 'string') {
    return userPermissionIds.includes(permissions);
  } else {
    if (or) {
      for (const permission of permissions) {
        if (userPermissionIds.includes(permission)) {
          return true;
        }
      }
      return false;
    } else {
      for (const permission of permissions) {
        if (!userPermissionIds.includes(permission)) {
          return false;
        }
      }
      return true;
    }
  }
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
