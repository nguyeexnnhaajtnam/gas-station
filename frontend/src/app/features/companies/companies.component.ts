import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Company } from '../../core/api.models';
import { CompanyContextService } from '../../core/company-context.service';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { SearchFieldComponent } from '../../shared/components/search-field/search-field.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { StatusBadgeComponent, StatusTone } from '../../shared/components/status-badge/status-badge.component';
import { BrandTopbarComponent } from '../../shared/components/brand-topbar/brand-topbar.component';
import { initials } from '../../shared/utils/station-name';
import { LucideEllipsis, LucideChevronRight, LucidePlus } from '../../shared/icons';

@Component({
  standalone: true,
  imports: [
    BrandTopbarComponent,
    PageHeaderComponent,
    SearchFieldComponent,
    EmptyStateComponent,
    ErrorStateComponent,
    SkeletonComponent,
    StatusBadgeComponent,
    LucideEllipsis,
    LucideChevronRight,
    LucidePlus,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './companies.component.html',
  styleUrl: './companies.component.scss',
})
export class CompaniesComponent {
  private readonly api = inject(ApiService);
  private readonly context = inject(CompanyContextService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly companies = signal<Company[]>([]);
  readonly query = signal('');

  readonly filtered = computed(() => {
    const q = this.query().trim().toLocaleLowerCase('vi');
    return q
      ? this.companies().filter((c) => [c.name, c.code, c.email, c.phone].some((v) => v?.toLocaleLowerCase('vi').includes(q)))
      : this.companies();
  });

  readonly resultCountLabel = computed(() => {
    const total = this.companies().length;
    return this.query().trim() ? `${this.filtered().length} / ${total} đơn vị` : `${total} đơn vị`;
  });

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.api
      .companies()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => this.companies.set(r.items),
        error: (e) => this.error.set(e?.error?.message ?? 'Nguồn dữ liệu công ty hiện không khả dụng.'),
      });
  }

  select(company: Company) {
    this.context.select(company);
    void this.router.navigate(['/companies', company.id, 'stations']);
  }

  initial(name: string) {
    return initials(name, 'C');
  }

  statusLabel(status: string) {
    return status === 'ACTIVE' ? 'Đang hoạt động' : status === 'INACTIVE' ? 'Ngừng hoạt động' : 'Chưa xác định';
  }

  statusTone(status: string): StatusTone {
    return status === 'ACTIVE' ? 'success' : status === 'INACTIVE' ? 'unknown' : 'unknown';
  }
}
