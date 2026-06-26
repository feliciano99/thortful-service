import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.style.colorScheme = '';
  });

  function createService(): ThemeService {
    TestBed.configureTestingModule({ providers: [ThemeService] });
    return TestBed.inject(ThemeService);
  }

  it('defaults to the system preference', () => {
    expect(createService().preference()).toBe('system');
  });

  it('reads a persisted preference on init', () => {
    localStorage.setItem('thortful.theme', 'light');
    expect(createService().preference()).toBe('light');
  });

  it('persists and applies a chosen preference', () => {
    const service = createService();

    service.set('dark');
    TestBed.tick();

    expect(service.preference()).toBe('dark');
    expect(localStorage.getItem('thortful.theme')).toBe('dark');
    expect(document.documentElement.style.colorScheme).toBe('dark');
  });

  it('uses "light dark" for the system preference', () => {
    const service = createService();

    service.set('system');
    TestBed.tick();

    expect(document.documentElement.style.colorScheme).toBe('light dark');
  });
});
