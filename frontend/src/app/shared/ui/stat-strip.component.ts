import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export interface StatItem {
  label: string;
  value: string;
  unit?: string;
}

/** Equal cells separated by 1px lines, 2px rules top and bottom. */
@Component({
  selector: 'app-stat-strip',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (item of items(); track item.label) {
      <div class="cell">
        <span class="label">{{ item.label }}</span>
        <span class="value-row num">
          <span class="value">{{ item.value }}</span>
          @if (item.unit) {
            <span class="unit">{{ item.unit }}</span>
          }
        </span>
      </div>
    }
  `,
  styles: `
    :host {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1px;
      background: var(--color-neutral-300);
      border-top: 2px solid var(--color-text);
      border-bottom: 2px solid var(--color-text);
    }

    .cell {
      display: flex;
      flex-direction: column;
      gap: 2px;
      padding: var(--space-4);
      background: var(--color-neutral-100);
      min-width: 0;
    }

    .label {
      font-size: 12.5px;
      color: var(--color-neutral-700);
    }

    .value-row {
      display: flex;
      align-items: baseline;
      gap: 6px;
      white-space: nowrap;
    }

    .value {
      font-size: 28px;
      font-weight: 700;
      line-height: 1.2;
    }

    .unit {
      font-size: 13px;
      color: var(--color-neutral-600);
    }

    @media (max-width: 639px) {
      .value {
        font-size: 24px;
      }
    }
  `,
})
export class StatStripComponent {
  readonly items = input.required<StatItem[]>();
}
