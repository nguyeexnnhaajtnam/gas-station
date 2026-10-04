import { ChangeDetectionStrategy, Component, ElementRef, HostListener, inject, input, signal } from '@angular/core';
import { LucideChevronDown } from '../icons';

/** Filter button ("Nhãn **Giá trị** ⌄") that opens a small panel with projected fields. */
@Component({
  selector: 'app-filter-popover',
  standalone: true,
  imports: [LucideChevronDown],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button type="button" class="filter-btn" [class.open]="open()" [attr.aria-expanded]="open()" (click)="open.set(!open())">
      <span class="filter-label">{{ label() }}</span>
      @if (value()) {
        <b>{{ value() }}</b>
      }
      <svg lucideChevronDown [size]="14"></svg>
    </button>
    @if (open()) {
      <div class="panel" [class.right]="alignRight()">
        <ng-content></ng-content>
      </div>
    }
  `,
  styles: `
    :host {
      position: relative;
      display: inline-flex;
    }

    .panel {
      position: absolute;
      top: calc(100% + 6px);
      left: 0;
      z-index: 20;
      display: flex;
      flex-direction: column;
      gap: var(--space-3);
      min-width: 260px;
      padding: var(--space-4);
      background: var(--color-neutral-100);
      border: 1px solid var(--color-divider);
      box-shadow: var(--shadow-float);
    }

    .panel.right {
      left: auto;
      right: 0;
    }
  `,
})
export class FilterPopoverComponent {
  private readonly host = inject(ElementRef<HTMLElement>);
  readonly label = input.required<string>();
  readonly value = input('');
  readonly alignRight = input(false);
  readonly open = signal(false);

  close() {
    this.open.set(false);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (this.open() && !this.host.nativeElement.contains(event.target as Node)) this.open.set(false);
  }

  @HostListener('document:keydown.escape')
  onEscape() {
    this.open.set(false);
  }
}
