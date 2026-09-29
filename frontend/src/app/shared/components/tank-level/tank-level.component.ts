import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-tank-level',
  standalone: true,
  imports: [DecimalPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="tank-row">
      <div class="identity">
        <strong>{{ name() }}</strong>
        <small>{{ fuelName() }}@if (note()) { · {{ note() }} }</small>
      </div>
      <div class="amount num">
        {{ currentLiters() | number: '1.0-0':'vi' }}
        @if (capacityLiters() != null) {
          / {{ capacityLiters() | number: '1.0-0':'vi' }}
        }
        L
      </div>
      @if (capacityLiters() != null) {
        <div class="bar">
          <div class="fill" [class.warning]="isLow()" [style.width.%]="percent()"></div>
        </div>
        <div class="percent num">{{ percent() }}%</div>
      }
    </div>
  `,
  styles: `
    .tank-row {
      display: grid;
      grid-template-columns: 1fr auto;
      align-items: center;
      gap: var(--space-2) var(--space-4);
      padding: var(--space-3) 0;
      border-bottom: 1px solid var(--line-soft);
    }

    .identity strong {
      display: block;
      font-size: 14px;
      font-weight: 600;
    }

    .identity small {
      color: var(--text-3);
      font-size: 12.5px;
    }

    .amount {
      font-size: 14px;
      font-weight: 600;
      text-align: right;
    }

    .bar {
      grid-column: 1 / -1;
      height: 8px;
      background: var(--surface-2);
    }

    .fill {
      height: 100%;
      background: #444141;
    }

    .fill.warning {
      background: var(--warning);
    }

    .percent {
      grid-column: 1 / -1;
      font-size: 15px;
      font-weight: 700;
      text-align: right;
    }
  `,
})
export class TankLevelComponent {
  readonly name = input.required<string>();
  readonly fuelName = input.required<string>();
  readonly currentLiters = input.required<number>();
  readonly capacityLiters = input<number | null>(null);
  readonly note = input<string>('');

  readonly percent = computed(() => {
    const capacity = this.capacityLiters();
    if (!capacity) return 0;
    return Math.round((this.currentLiters() / capacity) * 100);
  });

  readonly isLow = computed(() => this.percent() > 0 && this.percent() < 20);
}
