import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CardQuery, CategoryOption, GreetingCard, PagedResponse } from './card.models';

@Injectable({ providedIn: 'root' })
export class CardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/cards/v1';

  search(query: CardQuery): Observable<PagedResponse<GreetingCard>> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sort', query.sort);
    if (query.search) {
      params = params.set('search', query.search);
    }
    if (query.category) {
      params = params.set('category', query.category);
    }
    return this.http.get<PagedResponse<GreetingCard>>(`${this.baseUrl}/cards`, { params });
  }

  getCategories(): Observable<CategoryOption[]> {
    return this.http.get<CategoryOption[]>(`${this.baseUrl}/categories`);
  }
}
