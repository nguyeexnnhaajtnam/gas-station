import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { LucideSearch } from '../icons';
import { EmptyStateComponent } from '../components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../components/error-state/error-state.component';
import { SkeletonComponent } from '../components/skeleton/skeleton.component';

export type ListState = 'loading' | 'error' | 'empty' | 'ready';

/**
 * Standard list layout: toolbar (search + filter buttons + record count),
 * the projected `.dl-table`, and a footer with pagination. Elements marked with
 * the `filters` attribute are placed in the toolbar.
 */
@Component({
  selector: 'app-data-list',
  standalone: true,
  imports: [LucideSearch, EmptyStateComponent, ErrorStateComponent, SkeletonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toolbar">
      <label class="search-box">
        <svg lucideSearch [size]="16"></svg>
        <input type="search" [placeholder]="searchPlaceholder()" [value]="query()" (input)="queryChange.emit($any($event.target).value)" />
      </label>
      <ng-content select="[filters]"></ng-content>
      <span class="count">{{ countLabel() }}</span>
    </div>

    @switch (state()) {
      @case ('loading') {
        <app-skeleton type="rows" [rows]="8" [columns]="skeletonColumns()"></app-skeleton>
      }
      @case ('error') {
        <app-error-state [title]="errorTitle()" [message]="errorMessage()" (retry)="retry.emit()"></app-error-state>
      }
      @case ('empty') {
        <app-empty-state [title]="emptyTitle()" [message]="emptyMessage()" [actionLabel]="emptyActionLabel()" (action)="emptyAction.emit()"></app-empty-state>
      }
      @default {
        <div class="dl-scroll"><ng-content></ng-content></div>
      }
    }

    @if (state() === 'ready') {
      <div class="footer">
        <span>{{ countLabel() }}</span>
        <div class="pager">
          <button type="button" [disabled]="page() <= 0" (click)="pageChange.emit(page() - 1)">Trước</button>
          <span class="num">Trang {{ page() + 1 }} / {{ totalPages() || 1 }}</span>
          <button type="button" [disabled]="page() + 1 >= totalPages()" (click)="pageChange.emit(page() + 1)">Sau</button>
        </div>
      </div>
    }
  `,
  styles: `
    :host {
      display: block;
      min-width: 0;
      background: var(--color-neutral-100);
      border-top: 2px solid var(--color-text);
    }

    .toolbar {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      flex-wrap: wrap;
      min-height: 60px;
      padding: 12px var(--space-4);
      border-bottom: 1px solid var(--color-neutral-300);
    }

    .count {
      margin-left: auto;
      font-size: 13px;
      color: var(--color-neutral-700);
      white-space: nowrap;
    }

    .footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: var(--space-2);
      flex-wrap: wrap;
      padding: 10px var(--space-4);
      font-size: 12.5px;
      color: var(--color-neutral-600);
    }

    .pager {
      display: flex;
      align-items: center;
      gap: 4px;
      color: var(--color-text);
      font-size: 13px;
    }

    .pager span {
      padding: 0 10px;
    }

    .pager button {
      height: 32px;
      padding: 0 12px;
      border: 1px solid var(--color-neutral-400);
      background: transparent;
      font-size: 13px;
    }

    .pager button:hover:not(:disabled) {
      border-color: var(--color-text);
    }

    .pager button:disabled {
      border-color: var(--color-neutral-300);
      color: var(--color-neutral-500);
    }

    @media (max-width: 639px) {
      .search-box {
        max-width: none;
        flex-basis: 100%;
      }
    }
  `,
})
export class DataListComponent {
  readonly state = input<ListState>('ready');
  readonly searchPlaceholder = input('Tìm kiếm…');
  readonly query = input('');
  readonly countLabel = input('');
  /** 0-based. */
  readonly page = input(0);
  readonly totalPages = input(1);
  readonly skeletonColumns = input(6);
  readonly emptyTitle = input('Không có dữ liệu phù hợp');
  readonly emptyMessage = input('Thử bỏ bộ lọc hoặc đổi từ khóa tìm kiếm.');
  readonly emptyActionLabel = input('');
  readonly errorTitle = input('Không thể tải dữ liệu');
  readonly errorMessage = input('');

  readonly queryChange = output<string>();
  readonly pageChange = output<number>();
  readonly retry = output<void>();
  readonly emptyAction = output<void>();
}
