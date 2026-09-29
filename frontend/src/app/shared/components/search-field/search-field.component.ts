import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { LucideSearch } from '../../icons';

@Component({
  selector: 'app-search-field',
  standalone: true,
  imports: [LucideSearch],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="search-field">
      <svg lucideSearch [size]="16" class="icon"></svg>
      <input
        type="text"
        [placeholder]="placeholder()"
        [value]="value()"
        (input)="queryChange.emit($any($event.target).value)"
      />
    </div>
  `,
  styles: `
    .search-field {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      width: min(420px, 100%);
      height: var(--control-h-lg);
      padding: 0 var(--space-3);
      border: 1px solid var(--line);
      background: var(--surface);
    }

    .icon {
      flex: none;
      color: var(--text-muted);
    }

    input {
      width: 100%;
      height: 100%;
      border: 0;
      outline: 0;
      background: transparent;
      font-size: 14px;
      color: var(--text);
    }

    input::placeholder {
      color: var(--text-muted);
    }
  `,
})
export class SearchFieldComponent {
  readonly placeholder = input<string>('Tìm kiếm…');
  readonly value = input<string>('');
  readonly queryChange = output<string>();
}
