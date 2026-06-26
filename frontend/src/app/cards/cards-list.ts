import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { CategoryOption, GreetingCard } from './card.models';
import { CardService } from './card.service';

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
    MatIconModule
  ],
  templateUrl: './cards-list.html',
  styleUrl: './cards-list.scss'
})
export class CardsList implements OnInit {
  private readonly cardService = inject(CardService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly displayedColumns = ['title', 'category', 'artist', 'price', 'stockStatus'];
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
        }
      });
  }
}
