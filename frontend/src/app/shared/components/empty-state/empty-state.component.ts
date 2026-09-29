import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { LucideInbox } from '../../icons';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [LucideInbox],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="empty-state" [class.inline]="variant() === 'inline'">
      <svg lucideInbox [size]="28" class="icon"></svg>
      <h3>{{ title() }}</h3>
      @if (message()) {
        <p>{{ message() }}</p>
      }
      @if (actionLabel()) {
        <button type="button" class="btn btn-secondary" (click)="action.emit()">{{ actionLabel() }}</button>
      }
    </div>
  `,
  styles: `
    .empty-state {
      display: grid;
      justify-items: start;
      text-align: left;
      gap: var(--space-2);
      padding: var(--space-7) var(--space-4);
    }

    .icon {
      color: var(--text-muted);
      margin-bottom: var(--space-1);
    }

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: var(--text);
    }

    .inline h3 {
      font-size: 14.5px;
      font-weight: 600;
    }

    p {
      font-size: 13.5px;
      color: var(--text-2);
      max-width: 480px;
    }

    .btn {
      margin-top: var(--space-2);
    }
  `,
})
export class EmptyStateComponent {
  readonly title = input.required<string>();
  readonly message = input<string>('');
  readonly variant = input<'default' | 'inline'>('default');
  readonly actionLabel = input<string>('');
  readonly action = output<void>();
}
