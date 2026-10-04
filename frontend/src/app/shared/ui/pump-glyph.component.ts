import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PumpView } from '../utils/pump-view';

/** Small dispenser drawing used on Tổng quan; opens Theo dõi online. */
@Component({
  selector: 'app-pump-glyph',
  standalone: true,
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a routerLink="/pumps" [title]="pump().name + ' · ' + pump().stateLabel">
      <span class="drawing" aria-hidden="true">
        <span class="cap" [style.border-bottom-color]="pump().strip"></span>
        <span class="body"><span class="screen" [style.background]="pump().screen"></span></span>
        <span class="base"></span>
      </span>
      <span class="name">{{ pump().name }}</span>
      <span class="state" [style.color]="pump().stateColor">
        <i [style.background]="pump().stateColor" [class.pulse]="pump().state === 'PUMPING'"></i>{{ pump().stateLabel }}
      </span>
    </a>
  `,
  styles: `
    :host {
      display: block;
      min-width: 0;
    }

    a {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      padding: 14px 6px;
      color: var(--color-text);
      text-decoration: none;
    }

    a:hover {
      background: var(--color-neutral-200);
    }

    .drawing {
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .cap {
      width: 46px;
      height: 12px;
      background: var(--color-text);
      border-bottom: 3px solid;
    }

    .body {
      display: flex;
      justify-content: center;
      width: 38px;
      height: 42px;
      padding-top: 6px;
      border-inline: 2px solid var(--color-text);
      background: var(--color-neutral-100);
    }

    .screen {
      width: 24px;
      height: 13px;
    }

    .base {
      width: 52px;
      height: 6px;
      background: var(--color-text);
    }

    .name {
      max-width: 100%;
      font-size: 13px;
      font-weight: 700;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .state {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      max-width: 100%;
      font-size: 12px;
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    i {
      width: 6px;
      height: 6px;
      flex: none;
    }
  `,
})
export class PumpGlyphComponent {
  readonly pump = input.required<PumpView>();
}
