import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { LucideTriangleAlert } from '../icons';

/**
 * Tank drawing: 2px frame with a cap wider than the body. Fills from the bottom
 * only when a fill percentage is known (needs tank capacity). Negative stock
 * shows a hatched warning instead.
 */
@Component({
  selector: 'app-tank-gauge',
  standalone: true,
  imports: [LucideTriangleAlert],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[style.width.px]': 'width()',
    '[style.height.px]': 'height()',
    '[class.negative]': 'negative()',
    role: 'img',
    '[attr.aria-label]': 'negative() ? "Tồn âm" : percent() == null ? "Chưa có dung tích" : percent() + "%"',
  },
  template: `
    <span class="cap"></span>
    @if (!negative() && percent() != null) {
      <span class="fill" [style.height.%]="clamped()" [style.background]="color()"></span>
    }
    @if (negative()) {
      <span class="center danger">
        <svg lucideTriangleAlert [size]="22"></svg>
        <b>TỒN ÂM</b>
      </span>
    } @else if (percent() == null) {
      <span class="center"><b>—</b></span>
    }
  `,
  styles: `
    :host {
      position: relative;
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
      flex: none;
      border: 2px solid var(--color-text);
      background: var(--color-neutral-100);
    }

    :host(.negative) {
      background: repeating-linear-gradient(135deg, var(--color-accent-100) 0 6px, var(--color-neutral-100) 6px 12px);
    }

    .cap {
      position: absolute;
      left: -8px;
      right: -8px;
      top: -2px;
      height: 6px;
      background: var(--color-text);
    }

    .fill {
      display: block;
      width: 100%;
    }

    .center {
      position: absolute;
      inset: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 4px;
      color: var(--color-neutral-500);
    }

    .center.danger {
      color: var(--color-accent-700);
    }

    b {
      font-size: 11px;
      font-weight: 700;
      letter-spacing: .06em;
      background: var(--color-neutral-100);
      padding: 0 4px;
    }
  `,
})
export class TankGaugeComponent {
  readonly width = input(84);
  readonly height = input(120);
  /** 0–100, or null when the tank capacity is unknown. */
  readonly percent = input<number | null>(null);
  readonly negative = input(false);
  readonly color = input('var(--color-text)');

  clamped() {
    return Math.max(0, Math.min(100, this.percent() ?? 0));
  }
}
