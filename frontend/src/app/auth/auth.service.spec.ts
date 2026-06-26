import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('starts unauthenticated', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.username()).toBeNull();
  });

  it('verifies and stores the token on successful login', () => {
    let result: boolean | undefined;
    service.login('admin', 'changeme').subscribe((ok) => (result = ok));

    const req = http.expectOne((r) => r.url === '/cards/v1/cards');
    expect(req.request.headers.get('Authorization')).toBe('Basic ' + btoa('admin:changeme'));
    req.flush({ content: [], page: {} });

    expect(result).toBe(true);
    expect(service.isAuthenticated()).toBe(true);
    expect(service.username()).toBe('admin');
    expect(service.authToken()).toBe('Basic ' + btoa('admin:changeme'));
    expect(sessionStorage.getItem('thortful.auth.token')).toBeTruthy();
  });

  it('returns false and stays unauthenticated on a rejected login', () => {
    let result: boolean | undefined;
    service.login('admin', 'wrong').subscribe((ok) => (result = ok));

    http.expectOne((r) => r.url === '/cards/v1/cards').flush('no', {
      status: 401,
      statusText: 'Unauthorized'
    });

    expect(result).toBe(false);
    expect(service.isAuthenticated()).toBe(false);
  });

  it('clears state on logout', () => {
    service.login('admin', 'changeme').subscribe();
    http.expectOne((r) => r.url === '/cards/v1/cards').flush({});

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.username()).toBeNull();
    expect(sessionStorage.getItem('thortful.auth.token')).toBeNull();
  });
});
