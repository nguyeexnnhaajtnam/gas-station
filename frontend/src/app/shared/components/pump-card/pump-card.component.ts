import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { LucideWifi, LucideWifiOff } from '../../icons';

export type PumpState = 'active' | 'offline' | 'idle';

export interface PumpCardData {
  code: string;
  fuelName: string;
  state: PumpState;
  connected?: boolean;
  amountToday?: number | null;
  liters?: number | null;
  unitPrice?: number | null;
  totalLiters?: number | null;
}

@Component({
  selector: 'app-pump-card',
  standalone: true,
  imports: [DecimalPipe, LucideWifi, LucideWifiOff],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pump-card" [class]="pump().state">
      <div class="row head">
        <span class="code">TRỤ {{ pump().code }}</span>
        @if (pump().connected === true) {
          <svg lucideWifi [size]="14" class="conn"></svg>
        } @else if (pump().connected === false) {
          <svg lucideWifiOff [size]="14" class="conn offline"></svg>
        }
      </div>
      <div class="row fuel">{{ pump().fuelName }}</div>
      <div class="row state">{{ stateLabel() }}</div>
      @if (pump().amountToday != null) {
        <strong class="amount num">{{ pump().amountToday | number: '1.0-0':'vi' }} ₫</strong>
      }
      @if (pump().liters != null && pump().unitPrice != null) {
        <div class="meta num">{{ pump().liters | number: '1.2-2':'vi' }} L · {{ pump().unitPrice | number: '1.0-0':'vi' }} ₫/L</div>
      }
      @if (pump().totalLiters != null) {
        <div class="footer num">Tổng: {{ pump().totalLiters | number: '1.2-2':'vi' }} L</div>
      }
    </div>
  `,
  styles: `
    .pump-card {
      background: var(--surface);
      border: 1px solid var(--line-soft);
      border-top: 3px solid var(--line-soft);
      padding: var(--space-4);
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .pump-card.active {
      border-top-color: var(--success);
    }

    .pump-card.offline {
      border-top-color: var(--danger);
    }

    .row {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .code {
      font-size: 13px;
      font-weight: 700;
      letter-spacing: .06em;
    }

    .conn {
      color: var(--success);
    }

    .conn.offline {
      color: var(--danger);
    }

    .fuel {
      font-size: 12.5px;
      color: var(--text-2);
    }

    .state {
      font-size: 15px;
      font-weight: 600;
    }

    .amount {
      font-size: 24px;
      font-weight: 700;
    }

    .meta {
      font-size: 12.5px;
      color: var(--text-2);
    }

    .footer {
      margin-top: var(--space-2);
      padding-top: var(--space-2);
      border-top: 1px solid var(--line-soft);
      font-size: 12.5px;
      color: var(--text-2);
    }
  `,
})
export class PumpCardComponent {
  readonly pump = input.required<PumpCardData>();

  stateLabel() {
    const state = this.pump().state;
    return state === 'active' ? 'Đang bơm' : state === 'offline' ? 'Ngừng hoạt động' : 'Rảnh';
  }
}
