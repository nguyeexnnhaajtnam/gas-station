import { ChangeDetectionStrategy, Component, ElementRef, HostListener, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth.service';
import { CompanyContextService } from '../../../core/company-context.service';
import { LucideUser, LucideLogOut } from '../../icons';

@Component({
  selector: 'app-user-menu',
  standalone: true,
  imports: [RouterLink, LucideUser, LucideLogOut],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="user-menu">
      <button type="button" class="avatar" (click)="toggle()">
        <svg lucideUser [size]="16"></svg>
      </button>
      @if (open()) {
        <div class="menu">
          <a class="menu-item" routerLink="/settings">
            <svg lucideUser [size]="14"></svg>
            Tài khoản của tôi
          </a>
          <button type="button" class="menu-item" (click)="logout()">
            <svg lucideLogOut [size]="14"></svg>
            Đăng xuất
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    .user-menu {
      position: relative;
    }

    .avatar {
      display: grid;
      place-items: center;
      width: 34px;
      height: 34px;
      border: 0;
      background: var(--sidebar-line);
      color: #fff;
    }

    .menu {
      position: absolute;
      top: calc(100% + 4px);
      right: 0;
      width: 200px;
      background: var(--surface);
      border: 1px solid var(--line);
      box-shadow: var(--shadow-float);
      z-index: 100;
    }

    .menu-item {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      width: 100%;
      padding: var(--space-3);
      border: 0;
      border-bottom: 1px solid var(--line-soft);
      background: transparent;
      color: var(--text);
      font-size: 13.5px;
      text-align: left;
      text-decoration: none;
    }

    .menu-item:last-child {
      border-bottom: 0;
    }

    .menu-item:hover {
      background: var(--surface-2);
    }
  `,
})
export class UserMenuComponent {
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly context = inject(CompanyContextService);
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  readonly open = signal(false);

  toggle() {
    this.open.set(!this.open());
  }

  logout() {
    this.open.set(false);
    this.auth.logout();
    this.context.clear();
    void this.router.navigate(['/login']);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.open.set(false);
    }
  }
}
