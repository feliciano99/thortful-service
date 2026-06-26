import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { vi } from 'vitest';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpCtrl: HttpTestingController;
  const router = { navigate: vi.fn() };

  beforeEach(() => {
    sessionStorage.clear();
    router.navigate.mockClear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: Router, useValue: router }
      ]
    });
    http = TestBed.inject(HttpClient);
    httpCtrl = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpCtrl.verify());

  it('attaches the Authorization header when a token is stored', () => {
    sessionStorage.setItem('thortful.auth.token', 'Basic stored-token');

    http.get('/cards/v1/cards').subscribe();

    const req = httpCtrl.expectOne('/cards/v1/cards');
    expect(req.request.headers.get('Authorization')).toBe('Basic stored-token');
    req.flush({});
  });

  it('leaves the request unchanged when no token is stored', () => {
    http.get('/cards/v1/cards').subscribe();

    const req = httpCtrl.expectOne('/cards/v1/cards');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('redirects to /login on a 401 response', () => {
    http.get('/cards/v1/cards').subscribe({ next: () => {}, error: () => {} });

    httpCtrl
      .expectOne('/cards/v1/cards')
      .flush('no', { status: 401, statusText: 'Unauthorized' });

    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
