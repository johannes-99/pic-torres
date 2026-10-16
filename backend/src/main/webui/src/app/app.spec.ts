import { provideRouter } from '@angular/router';
import { render, screen } from '@testing-library/angular';
import { App } from './app';

describe('[NFR-001/AC-3] App shell', () => {
  it('renders the toolbar with the application name', async () => {
    await render(App, { providers: [provideRouter([])] });

    expect(screen.getByText('pic-torres')).toBeTruthy();
  });

  it('renders a main content area for routed pages', async () => {
    await render(App, { providers: [provideRouter([])] });

    expect(screen.getByRole('main')).toBeTruthy();
  });
});
