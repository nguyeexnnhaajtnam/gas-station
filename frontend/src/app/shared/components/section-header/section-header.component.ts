import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideArrowRight } from '../../icons';

@Component({
  selector: 'app-section-header',
  standalone: true,
  imports: [RouterLink, LucideArrowRight],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="section-header">
      <h2>{{ title() }}</h2>
      @if (linkLabel() && linkTo()) {
        <a class="link" [routerLink]="linkTo()">
          {{ linkLabel() }}
          <svg lucideArrowRight [size]="14"></svg>
        </a>
      }
    </div>
  `,
  styles: `
    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-bottom: var(--space-2);
      border-bottom: 2px solid var(--line);
      margin-bottom: var(--space-4);
    }

    h2 {
      font-size: 16px;
      font-weight: 700;
    }

    .link {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 13.5px;
      font-weight: 500;
      color: var(--accent-text);
      text-decoration: none;
    }

    .link:hover {
      text-decoration: underline;
    }
  `,
})
export class SectionHeaderComponent {
  readonly title = input.required<string>();
  readonly linkLabel = input<string>('');
  readonly linkTo = input<string | any[]>('');
}
