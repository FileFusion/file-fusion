type Task<T> = {
  taskFn: (signal: AbortSignal) => Promise<T>;
  resolve: (value: T) => void;
  reject: (reason?: any) => void;
  aborted: boolean;
};

export class ConcurrencyController {
  private readonly maxConcurrency: number;
  private activeCount = 0;
  private queue: Task<unknown>[] = [];
  private activeTasks: Task<unknown>[] = [];
  private abortController: AbortController;
  private scheduled = false;

  constructor(maxConcurrency: number) {
    if (maxConcurrency < 1) {
      throw new Error('Concurrency must be at least 1');
    }
    this.maxConcurrency = maxConcurrency;
    this.abortController = new AbortController();
  }

  get isAborted(): boolean {
    return this.abortController.signal.aborted;
  }

  get pendingCount(): number {
    return this.queue.length;
  }

  get runningCount(): number {
    return this.activeCount;
  }

  add<T>(taskFn: (signal: AbortSignal) => Promise<T>): Promise<T> {
    if (this.isAborted) {
      return Promise.reject(this.createAbortError());
    }
    return new Promise<T>((resolve, reject) => {
      const task: Task<unknown> = {
        taskFn: taskFn as (signal: AbortSignal) => Promise<unknown>,
        resolve: resolve as (value: unknown) => void,
        reject: reject,
        aborted: false
      };
      this.queue.push(task);
      this.scheduleProcessing();
    });
  }

  abort(): void {
    if (this.isAborted) {
      return;
    }
    this.abortController.abort();
    const error = this.createAbortError();
    while (this.queue.length > 0) {
      const task = this.queue.shift()!;
      task.aborted = true;
      task.reject(error);
    }
    for (const task of this.activeTasks) {
      task.aborted = true;
      task.reject(error);
    }
    this.activeTasks = [];
    this.activeCount = 0;
  }

  private scheduleProcessing(): void {
    if (this.scheduled || !this.canProcessTask) {
      return;
    }
    this.scheduled = true;
    queueMicrotask(() => {
      this.scheduled = false;
      while (this.canProcessTask) {
        this.processNextTask();
      }
    });
  }

  private processNextTask(): void {
    const task = this.queue.shift()!;
    this.activeCount++;
    this.activeTasks.push(task);
    task
      .taskFn(this.abortController.signal)
      .then((result) => {
        if (!task.aborted) {
          task.resolve(result);
        }
      })
      .catch((error) => {
        if (!task.aborted) {
          task.reject(error);
        }
      })
      .finally(() => {
        this.activeCount--;
        this.activeTasks = this.activeTasks.filter((t) => t !== task);
        this.scheduleProcessing();
      });
  }

  private get canProcessTask(): boolean {
    return (
      !this.isAborted &&
      this.activeCount < this.maxConcurrency &&
      this.queue.length > 0
    );
  }

  private createAbortError(): DOMException {
    return new DOMException('Operation aborted', 'AbortError');
  }
}
