// ===== NiHao Store v2: estado versionado, migración, SRS, logros =====
const STORE_KEY = 'nihao_v2';
const BACKUP_KEY = 'nihao_v1_backup';
const OLD_KEYS = ['zh_xp','zh_racha','zh_last_day','zh_best_mem','zh_best_quiz','zh_best_escucha','zh_best_pinyin','zh_flash_vistas','zh_tema','zh_sonido'];

function leerLegacy(k) {
  try {
    const v = localStorage.getItem(k);
    return v === null ? undefined : JSON.parse(v);
  } catch (e) { return undefined; }
}

function defaultStore() {
  return {
    v: 2, xp: 0, racha: 0, lastDay: null, goal: 50,
    xpByDay: {},          // 'YYYY-MM-DD' (local) -> xp
    words: {},            // hanzi -> {ok, fail, last}
    games: {
      quiz:    { best: 0, played: 0, ok: 0 },
      escucha: { best: 0, played: 0, ok: 0 },
      pinyin:  { best: 0, played: 0, ok: 0 },
      tonos:   { best: 0, played: 0, ok: 0 },
      pronuncia: { best: 0, played: 0, ok: 0 },
      escribe: { best: 0, played: 0, ok: 0 },
      mem:     { best: {}, wins: 0 },
      flash:   { vistas: 0 }
    },
    achv: {},             // id -> fecha ISO
    camino: {},           // unitId -> {done: nivel máx superado (0-3), stars: {1:★,2:★,3:★}}
    cultura: { leidos: [] }, // ids de poemas leídos
    // economía pay-to-win (monedas del juego, sin dinero real)
    coins: 0, gastado: 0, freeze: 0, xp2: 0, pistas: 0, corazonesExtra: 0,
    items: {},            // id de item -> veces comprado
    ui: { tema: 'light', sonido: true, cat: 'todas', memPairs: 8 },
    lastBackup: null
  };
}

function migrarLegacy() {
  const hay = OLD_KEYS.some(k => localStorage.getItem(k) !== null);
  if (!hay) return null;
  const S = defaultStore();
  const g = leerLegacy;
  if (g('zh_xp') !== undefined) S.xp = g('zh_xp') || 0;
  if (g('zh_racha') !== undefined) S.racha = g('zh_racha') || 0;
  if (g('zh_last_day') !== undefined) S.lastDay = g('zh_last_day');
  if (g('zh_best_quiz') !== undefined) S.games.quiz.best = g('zh_best_quiz') || 0;
  if (g('zh_best_escucha') !== undefined) S.games.escucha.best = g('zh_best_escucha') || 0;
  if (g('zh_best_pinyin') !== undefined) S.games.pinyin.best = g('zh_best_pinyin') || 0;
  if (g('zh_best_mem') !== undefined && g('zh_best_mem')) S.games.mem.best = { 8: g('zh_best_mem') };
  if (g('zh_flash_vistas') !== undefined) S.games.flash.vistas = g('zh_flash_vistas') || 0;
  if (g('zh_tema') !== undefined) S.ui.tema = g('zh_tema') || 'light';
  if (g('zh_sonido') !== undefined) S.ui.sonido = g('zh_sonido') !== false;
  // respaldo de seguridad y limpieza de claves viejas
  const legado = {};
  OLD_KEYS.forEach(k => { legado[k] = leerLegacy(k); localStorage.removeItem(k); });
  try { localStorage.setItem(BACKUP_KEY, JSON.stringify(legado)); } catch (e) {}
  return S;
}

function loadStore() {
  try {
    const raw = localStorage.getItem(STORE_KEY);
    if (raw) {
      const S = Object.assign(defaultStore(), JSON.parse(raw));
      S.games = Object.assign(defaultStore().games, S.games);
      S.ui = Object.assign({ tema: 'light', sonido: true, cat: 'todas', memPairs: 8 }, S.ui);
      // normaliza récord antiguo de memorama (string -> por nivel)
      if (typeof S.games.mem.best === 'string') S.games.mem.best = S.games.mem.best ? { 8: S.games.mem.best } : {};
      if (!S.games.mem.best || typeof S.games.mem.best !== 'object') S.games.mem.best = {};
      return S;
    }
  } catch (e) {}
  return migrarLegacy() || defaultStore();
}

let S = loadStore();
function save() {
  try { localStorage.setItem(STORE_KEY, JSON.stringify(S)); } catch (e) {}
}

// ===== Fecha local YYYY-MM-DD =====
function diaLocal(d) {
  d = d || new Date();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return d.getFullYear() + '-' + m + '-' + day;
}

// ===== Tracking =====
function trackWord(hanzi, ok) {
  const w = S.words[hanzi] || (S.words[hanzi] = { ok: 0, fail: 0, last: 0, box: 1, due: 0 });
  if (ok) { w.ok++; w.box = Math.min((w.box || 1) + 1, 5); }
  else { w.fail++; w.box = 1; }
  w.last = Date.now();
  w.due = w.last + CAJAS_DIAS[w.box - 1] * 864e5;
  save();
}
function trackGame(g, ok) {
  const G = S.games[g];
  if (!G || G.played === undefined) return;
  G.played++;
  if (ok) G.ok++;
  save();
}
function precision(g) {
  const G = S.games[g];
  if (!G || !G.played) return null;
  return G.ok / G.played;
}
function estrellas(g) {
  const G = S.games[g];
  if (!G || (G.played || 0) < 5) return 0;
  const acc = G.ok / G.played;
  if (acc >= 0.85 && G.played >= 20) return 3;
  if (acc >= 0.7 && G.played >= 10) return 2;
  return 1;
}
const dibEstrellas = n => '★'.repeat(n) + '☆'.repeat(3 - n);

// ===== SRS Leitner (cajas 1-5 con fechas de repaso) =====
// Acierto → sube de caja (repaso más espaciado). Fallo → vuelve a la caja 1.
// Intervalos en días por caja: 1=hoy, 2=1d, 3=3d, 4=7d, 5=15d
const CAJAS_DIAS = [0, 1, 3, 7, 15];
function cajaEntry(hanzi) {
  const w = S.words[hanzi];
  if (!w) return null;
  if (w.box === undefined) { // migra entradas anteriores al sistema Leitner
    w.box = w.fail > 0 ? 1 : (w.ok >= 3 ? 3 : 2);
    w.due = 0;
  }
  return w;
}
function repasoDebido() {
  const now = Date.now();
  return Object.keys(S.words).filter(h => {
    const e = cajaEntry(h);
    return e && e.due <= now;
  }).length;
}
function srsScore(hanzi) {
  const e = cajaEntry(hanzi);
  const now = Date.now();
  if (!e) return 2000 + Math.random() * 10;           // no vistas: descubrimiento
  if (e.fail > 0 || e.due <= now) {                   // debidas o falladas: prioridad
    const overdue = Math.min((now - e.due) / 864e5, 10);
    return 3000 + e.fail * 10 + overdue * 5 + Math.random() * 3;
  }
  return -(e.due - now) / 864e5 + Math.random();      // no debidas: cuanto más lejos, menos
}
function srsPick(list, n) {
  return [...list]
    .sort((a, b) => srsScore(b.hanzi) - srsScore(a.hanzi))
    .slice(0, n);
}
// ===== Estado personal por palabra (Cuaderno) =====
function wordStatus(hz) {
  const e = cajaEntry(hz);
  if (!e) return 'nueva';
  if (e.fail > 0 && e.box <= 2) return 'debil';
  if (e.box >= 4) return 'dominada';
  return 'aprendiendo';
}
function contadores(list) {
  const c = { nueva: 0, aprendiendo: 0, debil: 0, dominada: 0 };
  (list || []).forEach(w => c[wordStatus(w.hanzi)]++);
  return c;
}
function weakWords(list, n) {
  n = n || 10;
  return list
    .filter(w => S.words[w.hanzi] && S.words[w.hanzi].fail > 0)
    .sort((a, b) => {
      const ea = S.words[a.hanzi], eb = S.words[b.hanzi];
      const ra = ea.fail / (ea.ok + ea.fail), rb = eb.fail / (eb.ok + eb.fail);
      return (rb - ra) || (eb.fail - ea.fail);
    })
    .slice(0, n);
}

// ===== Distractores adaptativos =====
// Si vas bien (precisión alta), los distractores se parecen: misma
// categoría, carácter compartido o misma inicial de pinyin.
function distractores(correcta, list, n, dificil) {
  const resto = list.filter(w => w.hanzi !== correcta.hanzi);
  if (!dificil) return [...resto].sort(() => Math.random() - 0.5).slice(0, n);
  const ini = s => (s || '').toLowerCase().replace(/[^a-z]/g, '').charAt(0);
  const puntaje = w => {
    let p = Math.random() * 2;
    if (w.cat === correcta.cat) p += 3;
    if ([...w.hanzi].some(ch => correcta.hanzi.includes(ch))) p += 4;
    if (ini(w.pinyin) && ini(w.pinyin) === ini(correcta.pinyin)) p += 2;
    return p;
  };
  return [...resto].sort((a, b) => puntaje(b) - puntaje(a)).slice(0, n);
}
function modoDificil(g) {
  const p = precision(g);
  return p !== null && S.games[g].played >= 8 && p >= 0.75;
}

// ===== Logros =====
const LOGROS = [
  { id: 'primer_paso', emoji: '👣', nombre: 'Primer paso', desc: 'Gana tus primeros 10 XP' },
  { id: 'cien',        emoji: '💯', nombre: 'Centena', desc: 'Acumula 100 XP totales' },
  { id: 'quinientos',  emoji: '🚀', nombre: 'Despegue', desc: 'Acumula 500 XP totales' },
  { id: 'racha3',      emoji: '🔥', nombre: 'Constancia x3', desc: 'Practica 3 días seguidos' },
  { id: 'racha7',      emoji: '🏮', nombre: 'Semana china', desc: 'Practica 7 días seguidos' },
  { id: 'quiz5',       emoji: '⚡', nombre: 'En racha', desc: '5 aciertos seguidos en Quiz' },
  { id: 'mem_win',     emoji: '🧩', nombre: 'Memoria total', desc: 'Completa un Memorama' },
  { id: 'lector25',    emoji: '📚', nombre: 'Lector', desc: 'Repasa 25 flashcards' },
  { id: 'leccion1',    emoji: '🛤️', nombre: 'En camino', desc: 'Completa tu 1ª lección' },
  { id: 'unidad1',     emoji: '👑', nombre: 'Unidad top', desc: 'Termina una unidad entera' },
  { id: 'poeta',       emoji: '🖋️', nombre: 'Poeta', desc: 'Lee 3 poemas clásicos' },
  { id: 'voz',         emoji: '🎤', nombre: 'Buena voz', desc: 'Acierta 3 pronunciaciones con el micro' },
  { id: 'trazo',       emoji: '✍️', nombre: 'Calígrafo', desc: 'Completa 3 trazados con ≥60% de cobertura' },
  { id: 'mercader',    emoji: '🪙', nombre: 'Cliente frecuente', desc: 'Gasta 100 monedas en la tienda' },
];
// Devuelve true si es nuevo desbloqueo
function desbloquear(id) {
  if (S.achv[id]) return false;
  S.achv[id] = new Date().toISOString();
  save();
  return true;
}

// ===== Export / Import / Reset =====
function exportarProgreso() {
  const datos = { app: 'nihao-lab', v: 2, exported: new Date().toISOString(), state: S };
  const blob = new Blob([JSON.stringify(datos, null, 2)], { type: 'application/json' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = 'nihao-progreso-' + diaLocal() + '.json';
  document.body.appendChild(a);
  a.click();
  setTimeout(() => { URL.revokeObjectURL(a.href); a.remove(); }, 500);
  S.lastBackup = new Date().toISOString();
  save();
}
function importarProgreso(file, cb) {
  const r = new FileReader();
  r.onload = () => {
    try {
      const d = JSON.parse(r.result);
      if (!d || d.app !== 'nihao-lab' || !d.state || typeof d.state.xp !== 'number') throw new Error('formato');
      S = Object.assign(defaultStore(), d.state);
      S.games = Object.assign(defaultStore().games, d.state.games);
      S.ui = Object.assign({ tema: 'light', sonido: true, cat: 'todas', memPairs: 8 }, d.state.ui);
      if (typeof S.games.mem.best === 'string') S.games.mem.best = S.games.mem.best ? { 8: S.games.mem.best } : {};
      save();
      cb(null);
    } catch (e) { cb(e); }
  };
  r.onerror = () => cb(new Error('lectura'));
  r.readAsText(file);
}
function resetJuego(g) {
  const base = defaultStore().games[g];
  if (base !== undefined) { S.games[g] = JSON.parse(JSON.stringify(base)); save(); }
}
function resetPalabras() { S.words = {}; save(); }
function borrarTodoNuevo() {
  localStorage.removeItem(STORE_KEY);
  localStorage.removeItem(BACKUP_KEY);
  location.reload();
}
