import { HttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { appConfig } from './app.config';

describe('[NFR-002/AC-5] CSRF token handling', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [...appConfig.providers, provideHttpClientTesting()],
    });
  });

  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';
  });

  it('NFR-002/AC-5 sends the XSRF-TOKEN cookie value as X-XSRF-TOKEN header on POST', () => {
    document.cookie = 'XSRF-TOKEN=token-123; path=/';
    const http = TestBed.inject(HttpClient);
    const backend = TestBed.inject(HttpTestingController);

    http.post('/api/images', {}).subscribe();

    const request = backend.expectOne('/api/images');
    expect(request.request.headers.get('X-XSRF-TOKEN')).toBe('token-123');
    request.flush({});
    backend.verify();
  });
});
