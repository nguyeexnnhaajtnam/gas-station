import { Injectable, signal } from '@angular/core';

export type ToastTone = 'success' | 'danger' | 'default';

export interface ToastMessage {
  id: number;
  text: string;
  tone: ToastTone;
}

const DURATION_MS = 3200;

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  readonly toasts = signal<ToastMessage[]>([]);

  show(text: string, tone: ToastTone = 'default') {
    const id = this.nextId++;
    this.toasts.update((list) => [...list, { id, text, tone }]);
    setTimeout(() => this.dismiss(id), DURATION_MS);
  }

  success(text: string) {
    this.show(text, 'success');
  }

  error(text: string) {
    this.show(text, 'danger');
  }

  dismiss(id: number) {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }
}
