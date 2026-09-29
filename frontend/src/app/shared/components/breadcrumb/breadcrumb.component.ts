import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideChevronRight } from '../../icons';

export interface BreadcrumbItem {
  label: string;
  link?: string | any[];
}

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [RouterLink, LucideChevronRight],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <nav class="breadcrumb" aria-label="Breadcrumb">
      @for (item of items(); track $index; let last = $last) {
        @if (item.link && !last) {
          <a [routerLink]="item.link">{{ item.label }}</a>
        } @else {
          <span [class.current]="last">{{ item.label }}</span>
        }
        @if (!last) {
          <svg lucideChevronRight [size]="13" class="sep"></svg>
        }
      }
    </nav>
  `,
  styles: `
    .breadcrumb {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13.5px;
      min-width: 0;
    }

    a,
    span {
      color: var(--text-3);
      text-decoration: none;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    a:hover {
      color: var(--text);
    }

    .current {
      color: var(--text);
      font-weight: 600;
    }

    .sep {
      flex: none;
      color: var(--text-muted);
    }
  `,
})
export class BreadcrumbComponent {
  readonly items = input.required<BreadcrumbItem[]>();
}
