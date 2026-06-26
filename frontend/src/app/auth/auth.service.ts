import { HttpClient, HttpContext, HttpContextToken } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';

export const SKIP_AUTH_REDIRECT = new HttpContextToken<boolean>(() => false);

const TOKEN_KEY = 'thortful.auth.token';
const USER_KEY = 'thortful.auth.user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly token = signal<string | null>(sessionStorage.getItem(TOKEN_KEY));
  private readonly user = signal<string | null>(sessionStorage.getItem(USER_KEY));

  readonly username = this.user.asReadonly();
  readonly isAuthenticated = computed(() => this.token() !== null);

  authToken(): string | null {
    return this.token();
  }

  login(username: string, password: string): Observable<boolean> {
    const token = 'Basic ' + btoa(`${username}:${password}`);
    return this.http
      .get('/cards/v1/cards', {
        params: { size: 1 },
        headers: { Authorization: token },
        context: new HttpContext().set(SKIP_AUTH_REDIRECT, true)
      })
      .pipe(
        map(() => {
          this.store(username, token);
          return true;
        }),
        catchError(() => of(false))
      );
  }

  logout(): void {
    this.token.set(null);
    this.user.set(null);
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
  }

  private store(username: string, token: string): void {
    this.token.set(token);
    this.user.set(username);
    sessionStorage.setItem(TOKEN_KEY, token);
    sessionStorage.setItem(USER_KEY, username);
  }
}
