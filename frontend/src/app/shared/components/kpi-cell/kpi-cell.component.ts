import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type KpiFormat = 'currency' | 'number' | 'integer';

@Component({
  selector: 'app-kpi-cell',
  standalone: true,
  imports: [CurrencyPipe, DecimalPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="kpi-cell">
      <div class="head">
        <span class="label">{{ label() }}</span>
        @if (unit()) {
          <span class="unit">{{ unit() }}</span>
        }
      </div>
      @if (value() === null) {
        <strong class="value muted num">—</strong>
        <small class="note">Chưa có dữ liệu</small>
      } @else if (format() === 'currency') {
        <strong class="value num">{{ value() | currency: 'VND':'symbol':'1.0-0':'vi' }}</strong>
      } @else if (format() === 'number') {
        <strong class="value num">{{ value() | number: '1.1-2':'vi' }}</strong>
      } @else {
        <strong class="value num">{{ value() | number: '1.0-0':'vi' }}</strong>
      }
    </div>
  `,
  styles: `
    .kpi-cell {
      padding: 14px var(--space-5) 16px;
      min-height: 104px;
      border-top: 2px solid var(--text);
      display: flex;
      flex-direction: column;
      gap: var(--space-2);
    }

    .head {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      gap: var(--space-2);
    }

    .label {
      font-size: 13px;
      font-weight: 500;
      color: var(--text-2);
    }

    .unit {
      font-size: 12px;
      color: var(--text-muted);
    }

    .value {
      font-size: 28px;
      font-weight: 700;
    }

    .value.muted {
      color: var(--text-muted);
    }

    .note {
      font-size: 12.5px;
      color: var(--text-muted);
    }
  `,
})
export class KpiCellComponent {
  readonly label = input.required<string>();
  readonly unit = input<string>('');
  readonly value = input.required<number | null>();
  readonly format = input<KpiFormat>('integer');
}
