import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { CardFormDialog } from './card-form-dialog';
import { CategoryOption, GreetingCard } from './card.models';
import { CardService } from './card.service';
import { ConfirmDialog } from './confirm-dialog';

@Component({
  selector: 'app-cards-list',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressBarModule,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './cards-list.html',
  styleUrl: './cards-list.scss'
})
export class CardsList implements OnInit {
  private readonly cardService = inject(CardService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly displayedColumns = ['title', 'category', 'artist', 'price', 'stockStatus', 'actions'];
  protected readonly cards = signal<GreetingCard[]>([]);
  protected readonly total = signal(0);
  protected readonly loading = signal(false);
  protected readonly categories = signal<CategoryOption[]>([]);

  protected readonly searchControl = new FormControl('');
  protected readonly categoryControl = new FormControl<string | null>(null);

  protected readonly pageSizeOptions = [10, 20, 50, 100];
  protected pageIndex = 0;
  protected pageSize = 20;
  protected sortActive = 'title';
  protected sortDirection: 'asc' | 'desc' = 'asc';

  ngOnInit(): void {
    this.cardService.getCategories()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((categories) => this.categories.set(categories));

    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.reload());

    this.categoryControl.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.reload());

    this.load();
  }

  protected onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  protected onSort(sort: Sort): void {
    this.sortActive = sort.active;
    this.sortDirection = sort.direction || 'asc';
    this.reload();
  }

  protected openCreateDialog(): void {
    const ref = this.dialog.open(CardFormDialog, {
      width: '480px',
      data: { categories: this.categories() }
    });
    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((created: GreetingCard | undefined) => {
        if (created) {
          this.snackBar.open(`Added “${created.title}”`, 'Dismiss', { duration: 3000 });
          this.reload();
        }
      });
  }

  protected confirmDelete(card: GreetingCard): void {
    const ref = this.dialog.open(ConfirmDialog, {
      width: '400px',
      data: {
        title: 'Delete card',
        message: `Delete “${card.title}”? This cannot be undone.`,
        confirmLabel: 'Delete'
      }
    });
    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed: boolean | undefined) => {
        if (confirmed) {
          this.deleteCard(card);
        }
      });
  }

  private deleteCard(card: GreetingCard): void {
    this.cardService.delete(card.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.snackBar.open(`Deleted “${card.title}”`, 'Dismiss', { duration: 3000 });
          this.load();
        },
        error: () => this.snackBar.open('Could not delete the card.', 'Dismiss', { duration: 4000 })
      });
  }

  private reload(): void {
    this.pageIndex = 0;
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.cardService.search({
      page: this.pageIndex,
      size: this.pageSize,
      sort: `${this.sortActive},${this.sortDirection}`,
      search: this.searchControl.value ?? undefined,
      category: this.categoryControl.value
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.cards.set(response.content);
          this.total.set(response.page.totalElements);
          this.loading.set(false);
        },
        error: () => {
          this.cards.set([]);
          this.total.set(0);
          this.loading.set(false);
          this.snackBar.open('Could not load cards.', 'Dismiss', { duration: 4000 });
        }
      });
  }
}
