import { createBLAKE3 } from 'hash-wasm';

function formatFileSize(bytes: number): string {
  if (!bytes || bytes === 0) {
    return '0 B';
  }
  bytes = Math.abs(bytes);
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function getFileRelativePath(file: File): string {
  if (!file?.webkitRelativePath?.includes('/')) {
    return '';
  }
  return file.webkitRelativePath.substring(
    0,
    file.webkitRelativePath.lastIndexOf('/')
  );
}

async function getFileHash(file: File | Blob): Promise<string> {
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
function supportImagePreview(mimeType: string): boolean {
  return supportImagePreviewType.includes(mimeType);
}

const supportVideoPreviewType = ['video/mp4'];
function supportVideoPreview(mimeType: string): boolean {
  return supportVideoPreviewType.includes(mimeType);
}

interface Chunk {
  index: number;
  start: number;
  end: number;
}

function getFileChunks(fileSize: number, chunkSize: number): Chunk[] {
  if (fileSize <= 0) {
    return [];
  }
  const totalChunks = Math.ceil(fileSize / chunkSize);
  return Array.from({ length: totalChunks }, (_, index) => {
    const start = index * chunkSize;
    const end = Math.min(start + chunkSize - 1, fileSize - 1);
    return { index, start, end };
  });
}

export type { Chunk };

export {
  formatFileSize,
  getFileRelativePath,
  getFileHash,
  supportImagePreview,
  supportVideoPreview,
  getFileChunks
};
