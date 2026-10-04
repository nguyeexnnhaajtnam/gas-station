import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { LucideFuel, LucideWifi, LucideWifiOff } from '../icons';
import { PumpView } from '../utils/pump-view';

/** Full dispenser card (1 pump = 1 nozzle): cap → status cells → screen → base. */
@Component({
  selector: 'app-pump-dispenser',
  standalone: true,
  imports: [DecimalPipe, LucideFuel, LucideWifi, LucideWifiOff],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header [style.border-bottom-color]="pump().strip">
      <span class="name" [title]="pump().name">{{ pump().name }}</span>
      <span class="fuel-chip" [title]="pump().fuel"><i [style.background]="pump().fuelColor"></i>{{ pump().fuel }}</span>
    </header>
    <div class="body">
      <div class="status-row">
        <div class="cell">
          <span class="icon-box" [style.color]="pump().connColor">
            @switch (pump().conn) {
              @case ('ONLINE') { <svg lucideWifi [size]="18"></svg> }
              @case ('OFFLINE') { <svg lucideWifiOff [size]="18"></svg> }
              @default {
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h.01M8.5 16.43a5 5 0 0 1 7 0"></path><path d="M5 12.86a10 10 0 0 1 14 0M2 8.82a15 15 0 0 1 20 0" stroke-dasharray="3 3"></path></svg>
              }
            }
          </span>
          <span class="text">
            <span class="caption">KẾT NỐI</span>
            <span class="value" [style.color]="pump().connColor">{{ pump().connLabel }}</span>
          </span>
        </div>
        <div class="cell bordered">
          <span class="icon-box" [style.color]="pump().stateColor"><svg lucideFuel [size]="18"></svg></span>
          <span class="text">
            <span class="caption">VÒI BƠM</span>
            <span class="value" [style.color]="pump().stateColor">
              @if (pump().state === 'PUMPING') { <i class="pulse" [style.background]="pump().stateColor"></i> }
              {{ pump().stateLabel }}
            </span>
          </span>
        </div>
      </div>
      <div class="screen-wrap">
        <div class="screen num" [style.color]="pump().digit">
          <span class="money">{{ pump().money != null ? (pump().money | number: '1.0-0') + ' ₫' : '—' }}</span>
          <span class="sub">
            {{ pump().liters != null ? (pump().liters | number: '1.2-2') + ' L' : '—' }}
            ·
            {{ pump().price != null ? (pump().price | number: '1.0-0') + ' ₫/L' : '—' }}
          </span>
        </div>
      </div>
    </div>
    <div class="base"></div>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      min-width: 0;
      padding: 0 8px;
    }

    header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
      padding: 10px 14px;
      background: var(--color-text);
      color: var(--color-neutral-100);
      border-bottom: 4px solid;
    }

    .name {
      font-size: 18px;
      font-weight: 700;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .fuel-chip {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      height: 24px;
      min-width: 0;
      padding: 0 8px;
      border: 1px solid var(--color-neutral-600);
      font-size: 12px;
      font-weight: 600;
      color: var(--color-neutral-200);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .fuel-chip i {
      width: 8px;
      height: 8px;
      flex: none;
      outline: 1px solid var(--color-neutral-500);
    }

    .body {
      display: flex;
      flex-direction: column;
      background: var(--color-neutral-100);
      border-inline: 2px solid var(--color-text);
    }

    .status-row {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      border-bottom: 2px solid var(--color-text);
    }

    .cell {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px;
      min-width: 0;
    }

    .cell.bordered {
      border-left: 1px solid var(--color-neutral-300);
    }

    .icon-box {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 30px;
      height: 30px;
      flex: none;
      border: 2px solid currentColor;
    }

    .text {
      display: flex;
      flex-direction: column;
      line-height: 1.25;
      min-width: 0;
    }

    .caption {
      font-size: 10.5px;
      font-weight: 600;
      letter-spacing: .08em;
      color: var(--color-neutral-600);
    }

    .value {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      font-size: 13px;
      font-weight: 700;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .value i {
      width: 6px;
      height: 6px;
      flex: none;
    }

    .screen-wrap {
      padding: 12px;
    }

    .screen {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 2px;
      padding: 12px 14px;
      background: var(--color-text);
      border: 3px solid var(--color-neutral-300);
    }

    .money {
      font-size: 26px;
      font-weight: 700;
      line-height: 1.15;
      white-space: nowrap;
    }

    .sub {
      font-size: 13px;
      color: var(--color-neutral-400);
      white-space: nowrap;
    }

    .base {
      height: 12px;
      margin: 0 -8px;
      background: var(--color-text);
    }
  `,
})
export class PumpDispenserComponent {
  readonly pump = input.required<PumpView>();
}
