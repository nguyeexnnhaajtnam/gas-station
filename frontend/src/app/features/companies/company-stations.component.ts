import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Station } from '../../core/api.models';
import { CompanyContextService } from '../../core/company-context.service';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { SearchFieldComponent } from '../../shared/components/search-field/search-field.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { BrandTopbarComponent } from '../../shared/components/brand-topbar/brand-topbar.component';
import { ToastHostComponent } from '../../shared/components/toast-host/toast-host.component';
import { StationActivationService } from '../../shared/services/station-activation.service';
import { ToastService } from '../../shared/services/toast.service';
import { initials, stationShortName } from '../../shared/utils/station-name';
import { LucideLoaderCircle, LucideCircleAlert, LucideX, LucideArrowRight } from '../../shared/icons';

@Component({
  standalone: true,
  imports: [
    RouterLink,
    BrandTopbarComponent,
    ToastHostComponent,
    PageHeaderComponent,
    SearchFieldComponent,
    EmptyStateComponent,
    ErrorStateComponent,
    SkeletonComponent,
    StatusBadgeComponent,
    LucideLoaderCircle,
    LucideCircleAlert,
    LucideX,
    LucideArrowRight,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './company-stations.component.html',
  styleUrl: './company-stations.component.scss',
})
export class CompanyStationsComponent {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  readonly context = inject(CompanyContextService);
  readonly activation = inject(StationActivationService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly selectionError = signal('');
  readonly stations = signal<Station[]>([]);
  readonly query = signal('');
  readonly companyId = this.route.snapshot.paramMap.get('companyId') ?? '';

  readonly filtered = computed(() => {
    const q = this.query().trim().toLocaleLowerCase('vi');
    return q
      ? this.stations().filter((s) => [s.name, s.code, s.email, s.phone].some((v) => v?.toLocaleLowerCase('vi').includes(q)))
      : this.stations();
  });

  readonly resultCountLabel = computed(() => {
    const total = this.stations().length;
    return this.query().trim() ? `${this.filtered().length} / ${total} trạm` : `${total} trạm`;
  });

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.api
      .stations(this.companyId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => this.stations.set(r.items),
        error: (e) => this.error.set(e?.error?.message ?? 'Nguồn dữ liệu trạm hiện không khả dụng.'),
      });
  }

  select(station: Station) {
    if (this.activation.activatingStationId() !== null) return;
    this.selectionError.set('');
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
        error: (error) => {
          this.selectionError.set(
            error?.status === 404 ? 'Trạm không còn tồn tại. Hãy tải lại danh sách.' : 'Không thể mở trạm. Vui lòng thử lại.',
          );
          this.toast.error('Không thể mở trạm. Vui lòng thử lại.');
        },
      });
  }

  dismissSelectionError() {
    this.selectionError.set('');
  }

  shortName(name: string) {
    return stationShortName(name);
  }

  initial(name: string) {
    return initials(stationShortName(name), 'T');
  }
}
