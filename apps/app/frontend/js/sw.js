/**
 * MODULE 18: Service Worker — cache-first static, network-first API.
 * Optimised for AMOLED S20 FE: aggressive static caching, live API pass-through.
 */
const SW_VERSION = "lennit-v2.0.0";
const STATIC_CACHE = `${SW_VERSION}-static`;
const DYNAMIC_CACHE = `${SW_VERSION}-dynamic`;

const STATIC_ASSETS = [
  "/",
  "/index.html",
  "/styles.css",
  "/js/app.js",
  "/js/charts.js",
  "/js/crypto_engine.js",
  "/manifest.json",
];

// ── Install ────────────────────────────────────────────────────────────────
self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(STATIC_CACHE).then((cache) => cache.addAll(STATIC_ASSETS))
  );
  self.skipWaiting();
});

// ── Activate ───────────────────────────────────────────────────────────────
self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys
          .filter((k) => k !== STATIC_CACHE && k !== DYNAMIC_CACHE)
          .map((k) => caches.delete(k))
      )
    )
  );
  self.clients.claim();
});

// ── Fetch ──────────────────────────────────────────────────────────────────
self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url);

  // API calls: network-first (live trading data must be fresh)
  if (url.pathname.startsWith("/api/") || url.pathname.startsWith("/ws/")) {
    event.respondWith(networkFirst(event.request));
    return;
  }

  // Static assets: cache-first
  event.respondWith(cacheFirst(event.request));
});

async function cacheFirst(request) {
  const cached = await caches.match(request);
  if (cached) return cached;
  try {
    const response = await fetch(request);
    if (response.ok) {
      const cache = await caches.open(DYNAMIC_CACHE);
      cache.put(request, response.clone());
    }
    return response;
  } catch {
    return new Response("Offline — cached version not available", { status: 503 });
  }
}

async function networkFirst(request) {
  try {
    return await fetch(request);
  } catch {
    const cached = await caches.match(request);
    return cached || new Response(JSON.stringify({ error: "offline" }), {
      status: 503,
      headers: { "Content-Type": "application/json" },
    });
  }
}

// ── Background sync (offline queue) ────────────────────────────────────────
self.addEventListener("sync", (event) => {
  if (event.tag === "lennit-trade-queue") {
    event.waitUntil(processPendingTrades());
  }
});

async function processPendingTrades() {
  // TODO: Flush IndexedDB trade queue when network returns
  console.log("[SW] Processing pending trade queue...");
}
