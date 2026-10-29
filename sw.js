/* NiHao Lab · Service Worker: la app funciona sin internet tras la 1ª visita */
const CACHE = 'nihao-v9';
const HANZI_WRITER_ORIGIN = 'https://cdn.jsdelivr.net';
const HANZI_WRITER_SCRIPT = 'https://cdn.jsdelivr.net/npm/hanzi-writer@3.5/dist/hanzi-writer.min.js';
const ASSETS = [
  './',
  'index.html',
  'styles.css',
  'app.js',
  'store.js',
  'vocab.js',
  'cultura.js',
  'manifest.webmanifest',
  'icons/icon-192.png',
  'icons/icon-512.png',
  'icons/apple-touch-180.png',
  'icons/favicon-32.png'
];

self.addEventListener('install', e => {
  e.waitUntil(
    caches.open(CACHE).then(async cache => {
      await cache.addAll(ASSETS);
      try {
        const response = await fetch(HANZI_WRITER_SCRIPT, { mode: 'cors' });
        if (response.ok) await cache.put(HANZI_WRITER_SCRIPT, response);
      } catch (_) { /* La app principal se instala aunque falle la guía externa. */ }
    }).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys()
      .then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET') return;
  const requestUrl = new URL(e.request.url);
  const sameOrigin = requestUrl.origin === location.origin;
  const hanziResource = requestUrl.origin === HANZI_WRITER_ORIGIN &&
    (requestUrl.href === HANZI_WRITER_SCRIPT || requestUrl.pathname.includes('/hanzi-writer-data@'));
  if (!sameOrigin && !hanziResource) return; // Google Fonts etc.: directo a red, sin caché
  e.respondWith(
    caches.match(e.request, { ignoreSearch: true }).then(hit => {
      if (hit) return hit;
      return fetch(e.request).then(res => {
        if (res && res.ok && (sameOrigin || res.type === 'cors')) {
          const cp = res.clone();
          caches.open(CACHE).then(c => c.put(e.request, cp));
        }
        return res;
      }).catch(() => sameOrigin ? caches.match('index.html') : Promise.reject(new Error('Sin conexión')));
    })
  );
});
