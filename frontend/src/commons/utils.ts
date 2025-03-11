import { NIcon } from 'naive-ui';
import { Component, h } from 'vue';

function arrayToTree(items: any[], parentId: string): any[] {
  return arrayToTreeCustom(items, parentId, 'id', 'parentId');
}

function arrayToTreeCustom(
  items: any[],
  parentId: string,
  idField: string,
  parentIdField: string
): any[] {
  const temp: any[] = [];
  for (const item of items) {
    if (item[parentIdField] == parentId) {
      const children = arrayToTreeCustom(
        items,
        item[idField],
        idField,
        parentIdField
      );
      if (children.length > 0) {
        item.children = children;
      }
      temp.push(item);
    }
  }
  return temp;
}

function treeForeach(tree: any[], func: (node: any, parent: any[]) => boolean) {
  treeForeachCustom(tree, 'children', func);
}

function treeForeachCustom(
  tree: any[],
  childrenField: string,
  func: (node: any, parent: any[]) => boolean
) {
  tree.forEach((node) => {
    const next: boolean = func(node, tree);
    if (!next) {
      return;
    }
    if (node[childrenField]) {
      treeForeachCustom(node[childrenField], childrenField, func);
    }
  });
}

function renderIcon(icon: Component, color?: string, size?: number) {
  if (!icon) {
    return undefined;
  }
  const props: any = {};
  if (color) {
    props['color'] = color;
  }
  if (size) {
    props['size'] = size;
  }
  return h(NIcon, props, { default: () => h(icon) });
}

function renderIconMethod(
  icon: Component | undefined,
  color?: string,
  size?: number
) {
  if (!icon) {
    return undefined;
  }
  return () => {
    return renderIcon(icon, color, size);
  };
}

export {
  arrayToTree,
  arrayToTreeCustom,
  treeForeach,
  treeForeachCustom,
  renderIcon,
  renderIconMethod
};
