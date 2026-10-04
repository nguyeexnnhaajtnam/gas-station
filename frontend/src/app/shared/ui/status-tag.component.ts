import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type TagTone = 'success' | 'danger' | 'neutral';

@Component({
  selector: 'app-status-tag',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '[class]': 'tone()' },
  template: `<i></i>{{ label() }}`,
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      height: 22px;
      padding: 0 8px;
      font-size: 12px;
      font-weight: 600;
      white-space: nowrap;
      background: var(--color-neutral-200);
      color: var(--color-neutral-700);
    }

    i {
      width: 6px;
      height: 6px;
      flex: none;
      background: var(--color-neutral-500);
    }

    :host(.success) {
      background: var(--color-success-bg);
      color: var(--color-success-text);
    }

    :host(.success) i {
      background: var(--color-success);
    }

    :host(.danger) {
      background: var(--color-accent-100);
      color: var(--color-accent-700);
    }

    :host(.danger) i {
      background: var(--color-accent);
    }
  `,
})
export class StatusTagComponent {
  readonly tone = input<TagTone>('neutral');
  readonly label = input.required<string>();
}
