import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (type() === 'rows') {
      <div class="skeleton-rows">
        @for (row of rowRange(); track row) {
          <div class="skeleton-row" [style.grid-template-columns]="'repeat(' + columns() + ', 1fr)'">
            @for (col of colRange(); track col) {
              <i></i>
            }
          </div>
        }
      </div>
    } @else {
      <i class="skeleton-block" [style.height.px]="height()"></i>
    }
  `,
  styles: `
    .skeleton-rows {
      padding: 4px var(--space-4);
    }

    .skeleton-row {
      display: grid;
      gap: var(--space-6);
      padding: var(--space-4) var(--space-1);
      border-bottom: 1px solid var(--line-soft);
    }

    i {
      height: 13px;
      background: var(--surface-2);
      animation: skeleton-pulse 1.4s ease-in-out infinite;
    }

    .skeleton-block {
      display: block;
      width: 100%;
      background: var(--surface-2);
      animation: skeleton-pulse 1.4s ease-in-out infinite;
    }
  `,
})
export class SkeletonComponent {
  readonly type = input<'rows' | 'block'>('block');
  readonly rows = input(4);
  readonly columns = input(4);
  readonly height = input(104);

  rowRange() {
    return Array.from({ length: this.rows() }, (_, i) => i);
  }

  colRange() {
    return Array.from({ length: this.columns() }, (_, i) => i);
  }
}
