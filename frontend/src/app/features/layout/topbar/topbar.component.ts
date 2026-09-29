import { ChangeDetectionStrategy, Component, computed, ElementRef, HostListener, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../../../core/auth.service';
import { CompanyContextService } from '../../../core/company-context.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb.component';
import { ContextSwitcherComponent } from '../../../shared/components/context-switcher/context-switcher.component';
import { ROUTE_TITLES } from '../../../shared/utils/route-titles';
import { stationShortName } from '../../../shared/utils/station-name';
import { LucideBell, LucideUser, LucideLogOut } from '../../../shared/icons';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [RouterLink, BreadcrumbComponent, ContextSwitcherComponent, LucideBell, LucideUser, LucideLogOut],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss',
})
export class TopbarComponent {
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly elementRef = inject(ElementRef<HTMLElement>);
  readonly context = inject(CompanyContextService);

  readonly userMenuOpen = signal(false);

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => e.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  readonly breadcrumbItems = computed<BreadcrumbItem[]>(() => {
    const url = this.url();
    const company = this.context.selectedCompany();
    const station = this.context.selectedStation();

    if (/^\/companies\/[^/]+\/stations/.test(url)) {
      return [
        { label: 'Công ty / Đại lý', link: '/companies' },
        { label: company?.name ?? 'Đơn vị' },
        { label: 'Trạm xăng' },
      ];
    }
    if (url === '/companies' || url === '/') {
      return [{ label: 'Công ty / Đại lý' }];
    }

    const pageTitle = this.pageTitle(url);
    if (company && station) {
      const stationsLink = ['/companies', company.id, 'stations'];
      return [
        { label: company.name, link: stationsLink },
        { label: stationShortName(station.name), link: stationsLink },
        { label: pageTitle },
      ];
    }
    return [{ label: pageTitle }];
  });

  private pageTitle(url: string): string {
    const path = url.split('?')[0];
    for (const [route, title] of Object.entries(ROUTE_TITLES)) {
      if (path.startsWith(route)) return title;
    }
    return '';
  }

  toggleUserMenu() {
    this.userMenuOpen.set(!this.userMenuOpen());
  }

  logout() {
    this.userMenuOpen.set(false);
    this.auth.logout();
    this.context.clear();
    void this.router.navigate(['/login']);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.userMenuOpen.set(false);
    }
  }
}
