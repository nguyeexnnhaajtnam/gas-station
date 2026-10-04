import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { LucideRefreshCw } from '../../icons';

/**
 * The single page header used by every screen: eyebrow + H1 + description,
 * and on the right an optional status ("Trực tiếp · HH:mm:ss" or
 * "Cập nhật HH:mm:ss") plus the "Làm mới" button.
 */
@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [DatePipe, LucideRefreshCw],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page-header">
      <div class="titles">
        @if (eyebrow()) {
          <span class="eyebrow">{{ eyebrow() }}</span>
        }
        <h1>{{ title() }}</h1>
        @if (subtitle()) {
          <p>{{ subtitle() }}</p>
        }
      </div>
      <div class="actions">
        @if (status() !== 'none') {
          <span class="status num">
            <span class="dot" [class.live]="status() === 'live'" [class.paused]="status() === 'paused'"></span>
            @switch (status()) {
              @case ('live') { <span class="full">Trực tiếp · </span> }
              @case ('paused') { <span class="full">Tạm dừng · </span> }
              @default { <span class="full">Cập nhật </span> }
            }
            {{ time() ? (time() | date: 'HH:mm:ss') : '—' }}
          </span>
        }
        <ng-content select="[actions]"></ng-content>
        @if (showRefresh()) {
          <button type="button" class="refresh" [disabled]="refreshing()" (click)="refresh.emit()">
            <svg lucideRefreshCw [size]="16" [class.spin]="refreshing()"></svg>Làm mới
          </button>
        }
      </div>
    </div>
  `,
  styles: `
    :host {
      display: block;
    }

    .page-header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: var(--space-4);
      flex-wrap: wrap;
      margin-bottom: var(--space-5);
    }

    .titles {
      display: flex;
      flex-direction: column;
      gap: 2px;
      min-width: 0;
    }

    .eyebrow {
      font-size: 11.5px;
      font-weight: 600;
      letter-spacing: .1em;
      text-transform: uppercase;
      color: var(--color-neutral-600);
    }

    h1 {
      font-size: 24px;
      line-height: 1.25;
      font-weight: 700;
      letter-spacing: -0.01em;
    }

    p {
      font-size: 13.5px;
      color: var(--color-neutral-700);
    }

    .actions {
      display: flex;
      align-items: center;
      gap: var(--space-4);
      flex-wrap: wrap;
    }

    .status {
      display: inline-flex;
      align-items: center;
      gap: var(--space-2);
      font-size: 13px;
      color: var(--color-neutral-700);
      white-space: nowrap;
    }

    .dot {
      width: 8px;
      height: 8px;
      background: var(--color-neutral-400);
    }

    .dot.live {
      background: var(--color-success);
      animation: live-pulse 2s ease-in-out infinite;
    }

    .dot.paused {
      background: var(--color-accent);
    }

    .refresh {
      display: inline-flex;
      align-items: center;
      gap: var(--space-2);
      height: 36px;
      padding: 0 14px;
      border: 2px solid var(--color-text);
      background: transparent;
      font-size: 13.5px;
      font-weight: 600;
      color: var(--color-text);
      text-align: left;
    }

    .refresh:hover:not(:disabled) {
      background: var(--color-neutral-200);
    }

    .refresh:disabled {
      opacity: .6;
    }

    @media (max-width: 639px) {
      h1 {
        font-size: 20px;
      }

      .full {
        display: none;
      }
    }
  `,
})
export class PageHeaderComponent {
  readonly eyebrow = input<string>('');
  readonly title = input.required<string>();
  readonly subtitle = input<string>('');
  /** 'live' only for Tổng quan / Theo dõi online. */
  readonly status = input<'none' | 'live' | 'paused' | 'updated'>('none');
  readonly time = input<Date | null>(null);
  readonly showRefresh = input(false);
  readonly refreshing = input(false);
  readonly refresh = output<void>();
}
