import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type StatusTone = 'success' | 'warning' | 'danger' | 'unknown';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="badge" [class]="tone()">
      <span class="dot"></span>
      <span class="label">{{ label() }}</span>
    </span>
  `,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: var(--text-2);
    }

    .dot {
      width: 8px;
      height: 8px;
      flex: none;
      background: currentColor;
    }

    .success {
      color: var(--success);
    }

    .warning {
      color: var(--warning);
    }

    .danger {
      color: var(--danger);
    }

    .unknown {
      color: var(--unknown);
    }

    .unknown .dot {
      background: transparent;
      border: 1px solid currentColor;
    }
  `,
})
export class StatusBadgeComponent {
  readonly tone = input<StatusTone>('unknown');
  readonly label = input.required<string>();
}
