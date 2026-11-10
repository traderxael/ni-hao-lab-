// ===== Pruebas de NiHao Lab (Node, sin dependencias) =====
// Ejecutar: node --test test/   (o npm test)
const { test } = require('node:test');
const assert = require('node:assert');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const ROOT = path.join(__dirname, '..');

// ---- stub mínimo del navegador para poder cargar store.js y app.js ----
function makeEl(id) {
  const el = {
    id, style: {}, dataset: {}, children: [], _text: '', innerHTML: '', value: '', disabled: false,
    classList: {
      _s: new Set(),
      add(c) { this._s.add(c); }, remove(c) { this._s.delete(c); },
      toggle(c, on) { if (on === undefined) on = !this._s.has(c); on ? this._s.add(c) : this._s.delete(c); return on; },
      contains(c) { return this._s.has(c); },
    },
    set textContent(v) { this._text = String(v); }, get textContent() { return this._text; },
    setAttribute() {}, getAttribute() { return null; }, removeAttribute() {},
    closest() { return null; }, insertBefore(n) { this.children.push(n); return n; },
    appendChild(ch) { this.children.push(ch); return ch; },
    querySelector() { return makeEl('q'); }, querySelectorAll() { return []; },
    addEventListener() {}, focus() {}, click() {}, remove() {},
    getContext() { return {}; }, getBoundingClientRect() { return { left: 0, top: 0 }; },
  };
  return el;
}
function makeCtx() {
  const store = new Map();
  const els = new Map();
  const ctx = {
    console, setTimeout, clearTimeout, setInterval, clearInterval, Math, JSON, Date, Object, Array, String, Number, Boolean,
    Promise, Set, Map, WeakMap, RegExp, Error, isNaN, parseInt, parseFloat, Intl, URLSearchParams,
    localStorage: {
      getItem: k => (store.has(k) ? store.get(k) : null),
      setItem: (k, v) => store.set(k, String(v)),
      removeItem: k => store.delete(k),
    },
    document: {
      getElementById: id => { if (!els.has(id)) els.set(id, makeEl(id)); return els.get(id); },
      querySelector: () => makeEl('q'), querySelectorAll: () => [],
      createElement: t => makeEl(t), createDocumentFragment: () => makeEl('frag'),
      documentElement: makeEl('html'), head: makeEl('head'),
      body: makeEl('body'), addEventListener() {}, visibilityState: 'visible',
      createObjectURL: () => 'blob:x',
    },
    window: { matchMedia: () => ({ matches: false }), addEventListener() {} },
    navigator: { serviceWorker: undefined, mediaDevices: undefined, standalone: false },
    location: { protocol: 'file:', search: '', reload() {} },
    requestAnimationFrame: fn => setTimeout(fn, 0), cancelAnimationFrame: id => clearTimeout(id),
    speechSynthesis: { speak() {}, cancel() {}, getVoices: () => [] },
    SpeechSynthesisUtterance: function () {},
    ResizeObserver: function () { this.observe = () => {}; this.disconnect = () => {}; },
    matchMedia: () => ({ matches: false }),
    AudioContext: undefined, Blob: function () {}, URL: globalThis.URL,
  };
  ctx.globalThis = ctx;
  return ctx;
}
const EXPORT = `
;(() => { try {
  const names = ['VOCAB','UNIDADES','ADIVINA_PISTAS','NIVEL_LEN','PARES_TONO','TONO_INFO',
    'pool','unitWords','defaultStore','S','LEITNER','loadStore','save','writeNow','trackWord',
    'wordStatus','dueToday','distractores','muestra','rnd','nowISO','todayStr','achvOk','ACHVS',
    'tonoDe','quitarTonos','tipoPara','empezarLeccion','lec','pintarPregunta','responderLeccion',
    'nuevaAdivina','adActual','adPts','adRacha','adNivelPista','adOk','revelarPista',
    'comprobarAdivinaTecla','responderAdivina','rendirseAdivina','setMemPairs','mazo','open',
    'voltear','showView','iniciarMemorama','elogio','CICLO_TIPOS'];
  const out = {};
  for (const n of names) { try { out[n] = eval(n); } catch (e) {} }
  Object.assign(globalThis, out);
} catch (e) {} })();
`;
function runFile(ctx, f) {
  vm.runInContext(fs.readFileSync(path.join(ROOT, f), 'utf8') + EXPORT, ctx, { filename: f });
}
function load(files) {
  const ctx = makeCtx();
  vm.createContext(ctx);
  for (const f of files) runFile(ctx, f);
  return ctx;
}

// ================= vocab.js =================
test('vocab: estructura e integridad', () => {
  const c = load(['vocab.js']);
  const { VOCAB, UNIDADES, ADIVINA_PISTAS, NIVEL_LEN } = c;

  assert.ok(VOCAB.length >= 190, 'debe tener al menos 190 palabras (HSK1-3)');
  const hz = VOCAB.map(w => w.hanzi);
  assert.equal(new Set(hz).size, hz.length, 'no debe haber caracteres duplicados');
  VOCAB.forEach(w => {
    assert.ok(w.hanzi && w.pinyin && w.es && w.hsk >= 1 && w.cat, 'palabra incompleta: ' + JSON.stringify(w));
  });

  UNIDADES.forEach(u => {
    const n = c.unitWords(u.id).length;
    assert.ok(n >= 5, `unidad ${u.id} tiene ${n} palabras (<5)`);
  });
  assert.equal(UNIDADES.length, 12, 'la Ruta debe tener 12 unidades (8 + 4 nuevas HSK3)');

  // NIVELES AÑADIDOS: 4 niveles por unidad
  assert.deepEqual(NIVEL_LEN, [5, 7, 9, 11], 'cada unidad debe tener 4 niveles');

  // pistas de adivina: existen y apuntan a palabras reales de una sola sílaba
  const claves = Object.keys(ADIVINA_PISTAS);
  assert.ok(claves.length > 100, 'debe haber más de 100 caracteres con pistas (' + claves.length + ')');
  const map = new Map(VOCAB.map(w => [w.hanzi, w]));
  claves.forEach(k => {
    const w = map.get(k);
    assert.ok(w, `pista para "${k}" que no está en VOCAB`);
    assert.ok(Array.isArray(ADIVINA_PISTAS[k]) && ADIVINA_PISTAS[k].length >= 2, `pistas insuficientes para ${k}`);
    ADIVINA_PISTAS[k].forEach(p => assert.ok(typeof p === 'string' && p.length > 3));
  });
});

test('vocab: pool() y filtro HSK funcionan para HSK3', () => {
  const c = load(['vocab.js']);
  assert.equal(c.pool(3).length, 40, 'HSK3 debe traer sus 40 palabras nuevas');
  assert.equal(c.pool(1).length + c.pool(2).length + c.pool(3).length, c.VOCAB.length);
});

// ================= store.js =================
test('store: defaultStore incluye el juego adivina y Leitner', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js']);
  const d = c.defaultStore();
  assert.ok(d.games.adivina, 'games.adivina debe existir');
  assert.deepEqual(d.games.adivina, { best: 0, played: 0, ok: 0 });
  assert.equal(d.v, 2);
});

test('store: Leitner sube de caja con aciertos y baja con fallos', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js']);
  c.trackWord('好', true);
  assert.equal(c.S.words['好'].box, 2, 'primer acierto → caja 2');
  c.trackWord('好', true); c.trackWord('好', true);
  assert.equal(c.S.words['好'].box, 4, 'tres aciertos → caja 4');
  c.trackWord('好', false);
  assert.equal(c.S.words['好'].box, 1, 'un fallo devuelve a caja 1');
  assert.equal(c.wordStatus('好'), 'debil', 'con fallos y caja baja es palabra débil');
});

test('store: distractores nunca incluyen la respuesta y respetan n', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js']);
  const correcta = c.VOCAB[0];
  for (let i = 0; i < 20; i++) {
    const d1 = c.distractores(correcta, c.VOCAB, 3, false);
    const d2 = c.distractores(correcta, c.VOCAB, 3, true);
    assert.equal(d1.length, 3); assert.equal(d2.length, 3);
    assert.ok(!d1.some(w => w.hanzi === correcta.hanzi));
    assert.ok(!d2.some(w => w.hanzi === correcta.hanzi));
  }
});

test('store: save con debounce escribe en localStorage', async () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js']);
  c.S.xp = 123;
  c.save(); c.save(); c.save(); // varias llamadas → una escritura
  await new Promise(r => setTimeout(r, 260));
  const raw = JSON.parse(c.localStorage.getItem('nihao_v2'));
  assert.equal(raw.xp, 123, 'el estado debe persistir tras el debounce');
});

test('store: migración desde claves legacy v1', () => {
  const ctx = (() => {
    const c = makeCtx();
    c.localStorage.setItem('zh_xp', '42');
    c.localStorage.setItem('zh_racha', '3');
    c.localStorage.setItem('zh_best_quiz', '70');
    vm.createContext(c);
    return c;
  })();
  runFile(ctx, 'vocab.js');
  runFile(ctx, 'cultura.js');
  runFile(ctx, 'store.js');
  assert.equal(ctx.S.xp, 42, 'hereda XP del legado');
  assert.equal(ctx.S.racha, 3);
  assert.equal(ctx.S.games.quiz.best, 70);
  assert.ok(ctx.localStorage.getItem('nihao_v1_backup'), 'crea respaldo del legado');
});

// ================= app.js (lógica pura) =================
test('app: tonoDe detecta los 4 tonos', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js', 'app.js']);
  assert.equal(c.tonoDe('mā'), 1);
  assert.equal(c.tonoDe('má'), 2);
  assert.equal(c.tonoDe('mǎ'), 3);
  assert.equal(c.tonoDe('mà'), 4);
  assert.equal(c.tonoDe('ma'), 0, 'sin marca = tono neutro 0');
});

test('app: quitarTonos normaliza pinyin', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js', 'app.js']);
  assert.equal(c.quitarTonos('Nǐ hǎo'), 'ni hao');
  assert.equal(c.quitarTonos('Lǜsè'), 'luse');
});

test('app: tipoPara — nivel 4 añade "adivina" al ciclo', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js', 'app.js']);
  const l1 = [0, 1, 2, 3, 4].map(i => c.tipoPara(1, i));
  assert.ok(l1.every(t => t === 'quiz'), 'nivel 1 todo quiz');
  const l3 = [0, 1, 2, 3, 4].map(i => c.tipoPara(3, i));
  assert.ok(!l3.includes('adivina'), 'nivel 3 no usa adivina');
  const l4 = [0, 1, 2, 3, 4].map(i => c.tipoPara(4, i));
  assert.equal(l4[4], 'adivina', 'nivel 4 intercala adivinar el carácter');
});

test('app: sesión de lección completa hasta nivel 4 (simulación)', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js', 'app.js']);
  c.empezarLeccion('numeros', 1);
  assert.ok(c.lec, 'empezarLeccion crea la sesión');
  assert.equal(c.lec.queue.length, 5, 'nivel 1 = 5 preguntas');
  assert.equal(c.lec.hearts, 3 + (c.S.corazonesExtra || 0));

  c.empezarLeccion('numeros', 4);
  assert.equal(c.lec.queue.length, 11, 'nivel 4 = 11 preguntas');
  assert.ok(c.lec.queue.some(q => q.type === 'adivina'), 'el nivel 4 trae preguntas de adivinar');
  for (let guard = 0; guard < 60 && c.lec && !c.lec.ended; guard++) {
    const item = c.lec.queue[c.lec.i];
    c.pintarPregunta(item);            // no debe lanzar
    c.responderLeccion(true, null);    // acertamos siempre
  }
  assert.ok(c.lec.ended, 'la lección termina');
  const cu = c.S.camino['numeros'];
  assert.equal(cu.done, 4, 'se supera el nivel 4 (maestría)');
  assert.ok(cu.stars[4] >= 1, 'se registran estrellas del nivel 4');
  assert.ok(c.S.achv['maestria'], 'se desbloquea el logro Maestría');
});

test('app: 🧐 Adivina el carácter — acierto, pistas y puntuación', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js', 'app.js']);
  c.nuevaAdivina();
  assert.ok(c.adActual, 'hay palabra activa');
  assert.ok(c.ADIVINA_PISTAS[c.adActual.hanzi], 'la palabra elegida tiene pistas');

  c.revelarPista();
  assert.equal(c.adNivelPista, 1, 'una pista revelada');

  const inp = c.document.getElementById('ad-input');
  inp.value = c.adActual.hanzi;
  const xpAntes = c.S.xp, ptsAntes = c.adPts;
  c.comprobarAdivinaTecla();
  assert.ok(c.adOk, 'se marca como resuelta');
  assert.ok(c.adPts > ptsAntes, 'suma puntos');
  assert.ok(c.S.xp > xpAntes, 'convierte en XP');
  assert.equal(c.adPts - ptsAntes, 10, 'segunda pista usada → 12-2 = 10 pts');
  assert.equal(c.S.games.adivina.played, 1);
  assert.equal(c.S.games.adivina.ok, 1);

  c.nuevaAdivina();
  c.rendirseAdivina();
  assert.equal(c.adRacha, 0, 'rendirse corta la racha');
  assert.equal(c.S.games.adivina.played, 2);
});

test('app: Adivina acepta pinyin como respuesta alternativa', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js', 'app.js']);
  c.nuevaAdivina();
  const inp = c.document.getElementById('ad-input');
  inp.value = c.quitarTonos(c.adActual.pinyin);
  const antes = c.adPts;
  c.comprobarAdivinaTecla();
  assert.ok(c.adOk && c.adPts > antes, 'pinyin sin tonos también vale (menos puntos)');
});

test('app: memorama — parejas válidas', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js', 'app.js']);
  c.setMemPairs(6);
  assert.equal(c.mazo.length, 12, '6 parejas = 12 cartas');
  assert.equal(new Set(c.mazo.map(x => x.id)).size, 12, 'ids únicos');
  const porPar = {};
  c.mazo.forEach(x => { (porPar[x.par] = porPar[x.par] || []).push(x.kind); });
  Object.values(porPar).forEach(v => {
    assert.equal(v.length, 2);
    assert.deepEqual([...v].sort(), ['es', 'hz'], 'cada pareja une hz+es');
  });
  c.voltear(c.mazo[0].id, c.document.createElement('div'));
  assert.equal(c.open.length, 1);
});

test('app: showView navega a la vista adivina sin errores', () => {
  const c = load(['vocab.js', 'cultura.js', 'store.js', 'app.js']);
  c.showView('adivina');
  assert.ok(c.document.getElementById('view-adivina').classList.contains('active'));
});
