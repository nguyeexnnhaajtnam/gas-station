import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  standalone: true,
  imports: [PageHeaderComponent, EmptyStateComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-page-header [eyebrow]="eyebrow" [title]="title" [subtitle]="subtitle"></app-page-header>
    <app-empty-state title="Chức năng đang được phát triển." message="Tính năng này sẽ sớm được bổ sung."></app-empty-state>
  `,
})
export class ComingSoonComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly data = this.route.snapshot.data as { title?: string; eyebrow?: string; subtitle?: string };
  readonly title = this.data.title ?? 'Đang phát triển';
  readonly eyebrow = this.data.eyebrow ?? '';
  readonly subtitle = this.data.subtitle ?? '';
}
