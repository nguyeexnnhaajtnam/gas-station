import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Content block: 2px top rule (accent when warning), title + meta/link header. */
@Component({
  selector: 'app-panel',
  standalone: true,
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '[class.alert]': 'alert()' },
  template: `
    @if (title()) {
      <div class="head">
        <span class="title">{{ title() }}</span>
        @if (link()) {
          <a [routerLink]="link()" [queryParams]="linkParams()">{{ linkLabel() }} →</a>
        } @else if (meta()) {
          <span class="meta" [class.danger]="alert()">{{ meta() }}</span>
        }
        <ng-content select="[panel-meta]"></ng-content>
      </div>
    }
    <ng-content></ng-content>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      min-width: 0;
      background: var(--color-neutral-100);
      border-top: 2px solid var(--color-text);
    }

    :host(.alert) {
      border-top-color: var(--color-accent);
    }

    .head {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      gap: var(--space-3);
      padding: 14px var(--space-4);
      border-bottom: 1px solid var(--color-neutral-300);
    }

    .title {
      font-size: 15px;
      font-weight: 700;
    }

    .meta {
      font-size: 12.5px;
      color: var(--color-neutral-600);
      text-align: right;
    }

    .meta.danger {
      font-weight: 600;
      color: var(--color-accent-700);
    }

    a {
      font-size: 13px;
      font-weight: 600;
      text-decoration: none;
      white-space: nowrap;
    }

    a:hover {
      color: var(--color-accent-700);
    }
  `,
})
export class PanelComponent {
  readonly title = input<string>('');
  readonly meta = input<string>('');
  readonly link = input<string>('');
  readonly linkLabel = input<string>('');
  readonly linkParams = input<Record<string, string> | null>(null);
  readonly alert = input(false);
}
