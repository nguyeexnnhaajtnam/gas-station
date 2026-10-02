import { ChangeDetectionStrategy, Component } from '@angular/core';
import { UserMenuComponent } from '../user-menu/user-menu.component';
import { LucideFuel } from '../../icons';

@Component({
  selector: 'app-brand-topbar',
  standalone: true,
  imports: [UserMenuComponent, LucideFuel],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="brand-topbar">
      <div class="brand">
        <span class="mark"><svg lucideFuel [size]="16"></svg></span>
        <div>
          <b>Gas Station</b>
          <small>Quản lý cửa hàng</small>
        </div>
      </div>
      <app-user-menu></app-user-menu>
    </header>
  `,
  styles: `
    :host {
      display: block;
      position: sticky;
      top: 0;
      z-index: 50;
    }

    .brand-topbar {
      height: var(--topbar-height);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 var(--space-7);
      background: var(--surface);
      border-bottom: 2px solid var(--line);
    }

    .brand {
      display: flex;
      align-items: center;
      gap: var(--space-3);
    }

    .mark {
      display: grid;
      place-items: center;
      width: 28px;
      height: 28px;
      background: var(--accent);
      color: #fff;
    }

    .brand b {
      display: block;
      font-size: 14px;
      font-weight: 700;
    }

    .brand small {
      display: block;
      font-size: 11.5px;
      color: var(--text-3);
    }
  `,
})
export class BrandTopbarComponent {}
