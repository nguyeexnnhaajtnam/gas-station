import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { LucideCheck, LucideTriangleAlert, LucideBell } from '../../icons';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast-host',
  standalone: true,
  imports: [LucideCheck, LucideTriangleAlert, LucideBell],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toast-host">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast" [class]="toast.tone">
          @if (toast.tone === 'success') {
            <svg lucideCheck [size]="16"></svg>
          } @else if (toast.tone === 'danger') {
            <svg lucideTriangleAlert [size]="16"></svg>
          } @else {
            <svg lucideBell [size]="16"></svg>
          }
          <span>{{ toast.text }}</span>
        </div>
      }
    </div>
  `,
  styles: `
    .toast-host {
      position: fixed;
      right: var(--space-6);
      bottom: var(--space-6);
      display: flex;
      flex-direction: column;
      gap: var(--space-2);
      z-index: 1000;
    }

    .toast {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      padding: var(--space-3) var(--space-4);
      color: #fff;
      background: var(--text);
      box-shadow: var(--shadow-float);
      font-size: 13.5px;
    }

    .toast.success {
      background: var(--success);
    }

    .toast.danger {
      background: var(--danger);
    }
  `,
})
export class ToastHostComponent {
  readonly toastService = inject(ToastService);
}
