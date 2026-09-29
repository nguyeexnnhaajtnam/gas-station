import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Tank } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { TankLevelComponent } from '../../shared/components/tank-level/tank-level.component';

@Component({
  standalone: true,
  imports: [PageHeaderComponent, EmptyStateComponent, ErrorStateComponent, SkeletonComponent, TankLevelComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './tanks.component.html',
  styleUrl: './tanks.component.scss',
})
export class TanksComponent {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly tanks = signal<Tank[]>([]);

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
        next: (r) => this.tanks.set(r),
        error: () => this.error.set('Không thể tải dữ liệu bồn bể.'),
      });
  }
}
