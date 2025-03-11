import { createBLAKE3 } from 'hash-wasm';

function formatFileSize(bytes: number) {
  if (!bytes || bytes === 0) {
    return '0 B';
  }
  bytes = Math.abs(bytes);
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

async function getFileHash(file: File): Promise<string> {
  const hashInstance = await createBLAKE3();
  hashInstance.init();
  const reader = file.stream().getReader();
  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    hashInstance.update(value);
  }
  return hashInstance.digest('hex');
}

const supportImagePreviewType = [
  'image/gif',
  'image/jpeg',
  'image/png',
  'image/svg+xml',
  'image/webp'
];
function supportImagePreview(mimeType: string) {
  return supportImagePreviewType.includes(mimeType);
}

const supportVideoPreviewType = ['video/mp4'];
function supportVideoPreview(mimeType: string) {
  return supportVideoPreviewType.includes(mimeType);
}

export {
  formatFileSize,
  getFileHash,
  supportImagePreview,
  supportVideoPreview
};
