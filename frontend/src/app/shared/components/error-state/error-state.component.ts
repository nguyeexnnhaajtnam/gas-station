import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { LucideTriangleAlert } from '../../icons';

@Component({
  selector: 'app-error-state',
  standalone: true,
  imports: [LucideTriangleAlert],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="error-state">
      <svg lucideTriangleAlert [size]="28" class="icon"></svg>
      <h3>{{ title() }}</h3>
      <p>{{ message() || 'Kiểm tra kết nối mạng rồi thử lại…' }}</p>
      <button type="button" class="btn btn-secondary" (click)="retry.emit()">Thử lại</button>
      @if (requestId()) {
        <span class="request-id">Mã yêu cầu: {{ requestId() }}</span>
      }
    </div>
  `,
  styles: `
    .error-state {
      display: grid;
      justify-items: start;
      text-align: left;
      gap: var(--space-2);
      padding: var(--space-7) var(--space-4);
    }

    .icon {
      color: var(--danger);
      margin-bottom: var(--space-1);
    }

    h3 {
      font-size: 16px;
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

    .request-id {
      font-size: 11.5px;
      color: var(--text-muted);
    }
  `,
})
export class ErrorStateComponent {
  readonly title = input.required<string>();
  readonly message = input<string>('');
  readonly requestId = input<string>('');
  readonly retry = output<void>();
}
