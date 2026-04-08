// Lennit Suite · Cryptolyzer
// Phase 7: offline-aware shell with network-first data strategy.

const CACHE_NAME = 'lennit-suite-shell-v1';
const SHELL_URLS = [
  '/web/',
  '/web/index.html',
  '/web/manifest.webmanifest',
  '/web/sw.js',
  // Icons (add more if needed)
  '/web/icons/LS_Main_192.png',
  '/web/icons/LS_Main_512.png'
];

// Install: cache shell
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(SHELL_URLS)).catch(() => {})
  );
  self.skipWaiting();
});

// Activate: clean old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(
        keys.map(k => (k === CACHE_NAME ? null : caches.delete(k)))
      )
    )
  );
  event.waitUntil(self.clients.claim());
});

// Fetch: offline-first for shell, network-first for API
self.addEventListener('fetch', event => {
  const url = new URL(event.request.url);

  // Only care about our own origin
  if (url.origin !== self.location.origin) {
    return;
  }

  // API calls: always go network-first
  if (
    url.pathname.startsWith('/health') ||
    url.pathname.startsWith('/cryptolyzer/')
  ) {
    event.respondWith(fetch(event.request).catch(() => new Response(
      JSON.stringify({ error: 'offline', path: url.pathname }),
      { status: 503, headers: { 'content-type': 'application/json' } }
    )));
    return;
  }

  // Shell/static: cache-first, fallback to network, then to a basic offline page
  if (url.pathname.startsWith('/web')) {
    event.respondWith(
      caches.match(event.request).then(cached => {
        if (cached) return cached;
        return fetch(event.request).then(resp => {
          const copy = resp.clone();
          caches.open(CACHE_NAME).then(cache => cache.put(event.request, copy));
          return resp;
        }).catch(() => {
          if (url.pathname === '/web/' || url.pathname === '/web/index.html') {
            return new Response(
              '<!doctype html><html><body style="background:#020617;color:#e5e7eb;font-family:sans-serif;padding:1rem;"><h1>Offline</h1><p>Lennit Suite shell is cached, but live data is unavailable. Reconnect and retry.</p></body></html>',
              { status: 200, headers: { 'content-type': 'text/html' } }
            );
          }
          return new Response('', { status: 504 });
        });
      })
    );
  }
});