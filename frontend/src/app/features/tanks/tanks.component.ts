import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Tank } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { PanelComponent } from '../../shared/ui/panel.component';
import { StatusTagComponent } from '../../shared/ui/status-tag.component';
import { TankGaugeComponent } from '../../shared/ui/tank-gauge.component';
import { LucideTriangleAlert } from '../../shared/icons';
import { fuelColor } from '../../shared/utils/fuel-color';

@Component({
  standalone: true,
  imports: [
    DecimalPipe,
    PageHeaderComponent,
    EmptyStateComponent,
    ErrorStateComponent,
    SkeletonComponent,
    PanelComponent,
    StatusTagComponent,
    TankGaugeComponent,
    LucideTriangleAlert,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './tanks.component.html',
  styleUrl: './tanks.component.scss',
})
export class TanksComponent {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly tanks = signal<Tank[]>([]);
  readonly updatedAt = signal<Date | null>(null);

  readonly views = computed(() =>
    this.tanks().map((t) => ({ ...t, negative: t.estimatedVolumeLiters < 0, color: fuelColor(t.fuelName) })),
  );
  readonly negativeCount = computed(() => this.views().filter((t) => t.negative).length);

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.api
      .tanks()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => {
          this.tanks.set(r);
          this.updatedAt.set(new Date());
        },
        error: () => this.error.set('Không thể tải dữ liệu bồn bể.'),
      });
  }
}
