import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CreateCardRequest } from './card.models';
import { CardService } from './card.service';

describe('CardService', () => {
  let service: CardService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CardService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CardService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends paging, sort, search and category params', () => {
    service.search({ page: 1, size: 10, sort: 'title,asc', search: 'cat', category: 'BIRTHDAY' }).subscribe();

    const req = http.expectOne((r) => r.url === '/cards/v1/cards');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sort')).toBe('title,asc');
    expect(req.request.params.get('search')).toBe('cat');
    expect(req.request.params.get('category')).toBe('BIRTHDAY');
    req.flush({ content: [], page: { size: 10, number: 1, totalElements: 0, totalPages: 0 } });
  });

  it('omits search and category when not provided', () => {
    service.search({ page: 0, size: 20, sort: 'title,asc' }).subscribe();

    const req = http.expectOne((r) => r.url === '/cards/v1/cards');
    expect(req.request.params.has('search')).toBe(false);
    expect(req.request.params.has('category')).toBe(false);
    req.flush({ content: [], page: { size: 20, number: 0, totalElements: 0, totalPages: 0 } });
  });

  it('gets categories', () => {
    service.getCategories().subscribe();
    http.expectOne('/cards/v1/categories').flush([]);
  });

  it('posts a new card', () => {
    const body: CreateCardRequest = {
      title: 'X',
      category: 'BIRTHDAY',
      artist: 'A',
      price: 3,
      stockStatus: 'IN_STOCK'
    };
    service.create(body).subscribe();

    const req = http.expectOne('/cards/v1/cards');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ id: 1, ...body });
  });

  it('deletes a card by id', () => {
    service.delete(5).subscribe();
    const req = http.expectOne('/cards/v1/cards/5');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
