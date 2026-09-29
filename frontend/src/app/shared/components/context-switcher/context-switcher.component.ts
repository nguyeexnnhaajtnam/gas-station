import { ChangeDetectionStrategy, Component, ElementRef, HostListener, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiService } from '../../../core/api.service';
import { Company, Station } from '../../../core/api.models';
import { CompanyContextService } from '../../../core/company-context.service';
import { StationActivationService } from '../../services/station-activation.service';
import { ToastService } from '../../services/toast.service';
import { initials, stationShortName } from '../../utils/station-name';
import { LucideBuilding, LucideChevronDown, LucideCheck, LucideFuel, LucideLoaderCircle } from '../../icons';

@Component({
  selector: 'app-context-switcher',
  standalone: true,
  imports: [RouterLink, LucideBuilding, LucideChevronDown, LucideCheck, LucideFuel, LucideLoaderCircle],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="context-switcher">
      <button
        type="button"
        class="segment"
        [class.open]="companyMenuOpen()"
        (click)="toggleCompanyMenu()"
      >
        <svg lucideBuilding [size]="16" class="icon"></svg>
        <span class="text">
          <span class="segment-label">Công ty</span>
          <strong class="segment-value">{{ context.selectedCompany()?.name || 'Chưa chọn' }}</strong>
        </span>
        <svg lucideChevronDown [size]="14" class="chevron"></svg>
      </button>

      <button
        type="button"
        class="segment"
        [class.open]="stationMenuOpen()"
        [disabled]="!context.selectedCompany()"
        (click)="toggleStationMenu()"
      >
        @if (activation.activatingStationId()) {
          <svg lucideLoaderCircle [size]="16" class="icon spin"></svg>
        } @else {
          <svg lucideFuel [size]="16" class="icon"></svg>
        }
        <span class="text">
          <span class="segment-label">Trạm</span>
          <strong class="segment-value">{{ shortStationName() || 'Chưa chọn' }}</strong>
        </span>
        <svg lucideChevronDown [size]="14" class="chevron"></svg>
      </button>

      @if (companyMenuOpen()) {
        <div class="menu company-menu">
          @if (companiesLoading()) {
            <div class="menu-empty">Đang tải…</div>
          } @else if (companies().length === 0) {
            <div class="menu-empty">Không có đơn vị nào.</div>
          } @else {
            @for (company of companies(); track company.id) {
              <button type="button" class="menu-item" (click)="pickCompany(company)">
                <span class="mono">{{ initial(company.name) }}</span>
                <span class="label">{{ company.name }}</span>
                @if (context.selectedCompany()?.id === company.id) {
                  <svg lucideCheck [size]="14" class="check"></svg>
                }
              </button>
            }
          }
          <a class="menu-footer" routerLink="/companies" (click)="companyMenuOpen.set(false)">Xem tất cả đơn vị</a>
        </div>
      }

      @if (stationMenuOpen()) {
        <div class="menu station-menu">
          @if (stationsLoading()) {
            <div class="menu-empty">Đang tải…</div>
          } @else if (stations().length === 0) {
            <div class="menu-empty">Công ty chưa có trạm nào.</div>
          } @else {
            @for (station of stations(); track station.id) {
              <button
                type="button"
                class="menu-item station-item"
                [disabled]="!!activation.activatingStationId()"
                (click)="pickStation(station)"
              >
                <span class="label-group">
                  <span class="label">{{ shortNameOf(station.name) }}</span>
                  <small>Mã trạm: {{ station.code || '—' }}</small>
                </span>
                @if (context.selectedStation()?.id === station.id) {
                  <svg lucideCheck [size]="14" class="check"></svg>
                }
              </button>
            }
          }
        </div>
      }
    </div>
  `,
  styles: `
    .context-switcher {
      position: relative;
      display: flex;
      align-items: stretch;
      height: var(--control-h-lg);
      border: 1px solid var(--line);
      background: var(--surface);
    }

    .segment {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      padding: 0 var(--space-3);
      background: transparent;
      border: 0;
      border-right: 1px solid var(--line);
      max-width: 260px;
      color: var(--text);
    }

    .segment:last-child {
      border-right: 0;
    }

    .segment:disabled {
      opacity: .45;
      cursor: not-allowed;
    }

    .segment:hover:not(:disabled) {
      background: var(--surface-2);
    }

    .icon {
      flex: none;
      color: var(--text-3);
    }

    .icon.spin {
      animation: spin 0.9s linear infinite;
      color: var(--accent);
    }

    @keyframes spin {
      to {
        transform: rotate(360deg);
      }
    }

    .text {
      display: grid;
      min-width: 0;
      text-align: left;
    }

    .segment-label {
      font-size: 11px;
      color: var(--text-3);
    }

    .segment-value {
      font-size: 13px;
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .chevron {
      flex: none;
      color: var(--text-3);
    }

    .menu {
      position: absolute;
      top: calc(100% + 4px);
      right: 0;
      width: 330px;
      max-height: 360px;
      overflow: auto;
      background: var(--surface);
      border: 1px solid var(--line);
      box-shadow: var(--shadow-float);
      z-index: 100;
    }

    .station-menu {
      left: 0;
      right: auto;
    }

    .menu-empty {
      padding: var(--space-4);
      font-size: 13px;
      color: var(--text-muted);
    }

    .menu-item {
      display: flex;
      align-items: center;
      gap: var(--space-3);
      width: 100%;
      padding: var(--space-3);
      border: 0;
      border-bottom: 1px solid var(--line-soft);
      background: transparent;
      text-align: left;
    }

    .menu-item:hover:not(:disabled) {
      background: var(--surface-2);
    }

    .menu-item:disabled {
      opacity: .6;
    }

    .mono {
      display: grid;
      place-items: center;
      width: 28px;
      height: 28px;
      flex: none;
      background: var(--surface-2);
      font-size: 12px;
      font-weight: 700;
    }

    .label-group {
      display: grid;
      gap: 2px;
    }

    .label-group small {
      color: var(--text-3);
      font-size: 12px;
    }

    .label {
      font-size: 13.5px;
      color: var(--text);
      flex: 1;
    }

    .check {
      color: var(--accent);
      flex: none;
    }

    .menu-footer {
      display: block;
      padding: var(--space-3);
      font-size: 13px;
      font-weight: 500;
      color: var(--accent-text);
      text-decoration: none;
      text-align: center;
    }

    .menu-footer:hover {
      background: var(--surface-2);
    }

    @media (max-width: 1180px) {
      .segment {
        max-width: 170px;
      }
    }
  `,
})
export class ContextSwitcherComponent {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly elementRef = inject(ElementRef<HTMLElement>);
  readonly context = inject(CompanyContextService);
  readonly activation = inject(StationActivationService);

  readonly companyMenuOpen = signal(false);
  readonly stationMenuOpen = signal(false);
  readonly companies = signal<Company[]>([]);
  readonly companiesLoading = signal(false);
  readonly stations = signal<Station[]>([]);
  readonly stationsLoading = signal(false);

  shortStationName() {
    return stationShortName(this.context.selectedStation()?.name);
  }

  shortNameOf(name: string) {
    return stationShortName(name);
  }

  initial(name: string) {
    return initials(name, 'C');
  }

  toggleCompanyMenu() {
    this.stationMenuOpen.set(false);
    const next = !this.companyMenuOpen();
    this.companyMenuOpen.set(next);
    if (next && this.companies().length === 0) {
      this.companiesLoading.set(true);
      this.api
        .companies()
        .pipe(finalize(() => this.companiesLoading.set(false)))
        .subscribe({ next: (r) => this.companies.set(r.items), error: () => this.companies.set([]) });
    }
  }

  toggleStationMenu() {
    if (!this.context.selectedCompany()) return;
    this.companyMenuOpen.set(false);
    const next = !this.stationMenuOpen();
    this.stationMenuOpen.set(next);
    if (next) {
      this.stationsLoading.set(true);
      this.api
        .stations(this.context.selectedCompany()!.id)
        .pipe(finalize(() => this.stationsLoading.set(false)))
        .subscribe({ next: (r) => this.stations.set(r.items), error: () => this.stations.set([]) });
    }
  }

  pickCompany(company: Company) {
    this.companyMenuOpen.set(false);
    this.context.select(company);
    void this.router.navigate(['/companies', company.id, 'stations']);
  }

  pickStation(station: Station) {
    if (this.activation.activatingStationId()) return;
    this.stationMenuOpen.set(false);
    this.activation.activatingStationId.set(station.id);
    this.api
      .selectStation(station.id)
      .pipe(finalize(() => this.activation.activatingStationId.set(null)))
      .subscribe({
        next: (response) => {
          this.context.selectStation(response.station);
          this.toast.success(`Đã mở trạm ${stationShortName(response.station.name)}`);
          void this.router.navigate(['/dashboard']);
        },
        error: () => this.toast.error('Không thể mở trạm. Vui lòng thử lại.'),
      });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.companyMenuOpen.set(false);
      this.stationMenuOpen.set(false);
    }
  }
}
