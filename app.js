// ===== NiHao Lab: ES -> ZH (con Store v2 + SRS) =====
function showView(name) {
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.toggle('active', b.dataset.v === name));
  document.getElementById('view-' + name).classList.add('active');
  const btn = document.getElementById('btn-' + name);
  if (btn) btn.classList.add('active');
  actualizarMenu();
  pintarRepaso();
  if (name === 'progreso') renderProgreso();
  if (name === 'ruta') renderRuta();
  if (name === 'cultura') renderCultura();
  if (name === 'cuaderno') renderCuaderno();
  if (name === 'tienda') renderTienda();
}

function borrarTodo() {
  if (confirm('¿Borrar TODO el progreso? Podrás exportarlo antes en 📊 Progreso.')) borrarTodoNuevo();
}

let filtroHSK = 0;
function cambiarFiltro() {
  filtroHSK = parseInt(document.getElementById('filtro-hsk').value);
  S.ui.cat = document.getElementById('filtro-cat').value || 'todas';
  save();
  salirRepaso();
  actualizarMenu();
  iniciarMemorama(); nuevoMazoFlash(); nuevaPregunta();
  if (tModo === 'pares') nuevoPar(); else nuevoTono();
  nuevaPronuncia();
}
function pool() {
  let p = filtroHSK ? VOCAB.filter(w => w.hsk === filtroHSK) : VOCAB;
  if (S.ui.cat && S.ui.cat !== 'todas') {
    const c = p.filter(w => w.cat === S.ui.cat);
    if (c.length >= 4) p = c; // categorías muy pequeñas usan el pool HSK
  }
  return p;
}
const cap = s => s.charAt(0).toUpperCase() + s.slice(1);
function initCats() {
  const sel = document.getElementById('filtro-cat');
  const cats = {};
  VOCAB.forEach(w => cats[w.cat] = (cats[w.cat] || 0) + 1);
  Object.keys(cats).sort().forEach(c => {
    const o = document.createElement('option');
    o.value = c;
    o.textContent = cap(c) + ' (' + cats[c] + ')';
    sel.appendChild(o);
  });
  sel.value = S.ui.cat || 'todas';
}
const rnd = arr => arr[Math.floor(Math.random() * arr.length)];
function muestra(arr, n) { return [...arr].sort(() => Math.random() - .5).slice(0, n); }

// ===== Racha + XP + monedas =====
function registrarVisita() {
  const hoy = diaLocal();
  if (S.lastDay !== hoy) {
    const ayer = diaLocal(new Date(Date.now() - 864e5));
    if (S.lastDay === ayer) S.racha = S.racha + 1;
    else if ((S.freeze || 0) > 0) { S.freeze--; toastLogro('🧊 Tu protector salvó la racha'); }
    else S.racha = 1;
    S.lastDay = hoy;
    save();
  }
  if (S.racha >= 3) maybeAchv('racha3');
  if (S.racha >= 7) maybeAchv('racha7');
}
function addXP(n) {
  let ganancia = n;
  if ((S.xp2 || 0) > 0) { ganancia = n * 2; S.xp2--; save(); }
  const hoy = diaLocal();
  const antes = S.xpByDay[hoy] || 0;
  S.xp += ganancia;
  S.xpByDay[hoy] = antes + ganancia;
  save();
  toastXP(ganancia, ganancia !== n);
  if (S.xp >= 10) maybeAchv('primer_paso');
  if (S.xp >= 100) maybeAchv('cien');
  if (S.xp >= 500) maybeAchv('quinientos');
  if (antes < S.goal && S.xpByDay[hoy] >= S.goal) {
    toastLogro('🎯 ¡Meta diaria cumplida! (' + S.goal + ' XP)');
    confeti();
  }
  actualizarMenu();
}
function addMonedas(n) {
  S.coins = (S.coins || 0) + n;
  save();
  const t = document.createElement('div');
  t.className = 'xp-toast';
  t.textContent = '🪙 +' + n;
  document.body.appendChild(t);
  apilarToast(t, 30);
  setTimeout(() => t.remove(), 1300);
  actualizarMenu();
}
function maybeAchv(id) {
  if (!desbloquear(id)) return;
  const a = LOGROS.find(x => x.id === id);
  if (a) { toastLogro('🏆 Logro: ' + a.emoji + ' ' + a.nombre); confeti(); }
}

// ===== Feedback visual (toasts apilados) =====
let toastCount = 0;
function apilarToast(el, base) {
  const i = toastCount++;
  el.style.top = `calc(${base}% + ${i * 52}px)`;
  setTimeout(() => { el.remove(); toastCount = Math.max(0, toastCount - 1); }, 2300);
}
const ELOGIOS = ['¡Genial!', '¡Exacto!', '¡Brutal!', '¡De lujo!', '¡Súper!', '¡Nihao! 🐼', '¡Imparable!'];
const elogio = () => rnd(ELOGIOS);
function toastXP(n, doble) {
  const t = document.createElement('div');
  t.className = 'xp-toast';
  t.textContent = '+' + n + ' XP' + (doble ? ' ⚡x2' : '');
  document.body.appendChild(t);
  apilarToast(t, 22);
  setTimeout(() => t.remove(), 1300);
}
function toastLogro(txt) {
  const t = document.createElement('div');
  t.className = 'xp-toast logro';
  t.textContent = txt;
  document.body.appendChild(t);
  apilarToast(t, 22);
  setTimeout(() => t.remove(), 2200);
}
function confeti() {
  const emo = ['🎉','✨','🐼','🧧','⭐','🏮'];
  for (let i = 0; i < 34; i++) {
    const s = document.createElement('span');
    s.className = 'confeti';
    s.textContent = rnd(emo);
    s.style.left = Math.random() * 100 + 'vw';
    s.style.animationDuration = (1.8 + Math.random() * 1.6) + 's';
    s.style.fontSize = (0.9 + Math.random() * 1.1) + 'rem';
    document.body.appendChild(s);
    setTimeout(() => s.remove(), 3600);
  }
}

// ===== Tema y sonido =====
function aplicarTema(t) {
  document.documentElement.setAttribute('data-theme', t);
  const b = document.getElementById('btn-tema');
  if (b) b.textContent = t === 'dark' ? '☀️' : '🌙';
  S.ui.tema = t;
  save();
}
function toggleTema() {
  const act = document.documentElement.getAttribute('data-theme') || 'light';
  aplicarTema(act === 'dark' ? 'light' : 'dark');
}
function toggleSonido() {
  S.ui.sonido = !S.ui.sonido;
  save();
  const b = document.getElementById('btn-sonido');
  b.textContent = S.ui.sonido ? '🔊' : '🔇';
  b.classList.toggle('off', !S.ui.sonido);
  if (!S.ui.sonido) { try { speechSynthesis.cancel(); } catch (e) {} }
}
// Títulos de nivel por XP
const TITULOS = ['🐣 Pollito', '🌱 Brote', '🐼 Panda junior', '🏮 Farolillo', '🐲 Dragón', '👑 Maestro', '🌟 Leyenda'];
const tituloNivel = n => TITULOS[Math.min(Math.floor((n - 1) / 2), TITULOS.length - 1)];
function actualizarMenu() {
  const nivel = Math.floor(S.xp / 100) + 1;
  document.getElementById('xp-total').textContent = S.xp;
  document.getElementById('xp-nivel').textContent = nivel;
  document.getElementById('xp-titulo').textContent = tituloNivel(nivel);
  document.getElementById('xp-racha').textContent = S.racha;
  document.getElementById('coins').textContent = '🪙 ' + (S.coins || 0);
  document.getElementById('xp-fill').style.width = ((S.xp % 100)) + '%';
  document.getElementById('xp-next').textContent = (100 - (S.xp % 100)) + ' XP al nivel ' + (nivel + 1);
  const p = pool();
  const sufHSK = filtroHSK ? ' HSK' + filtroHSK : ' HSK1+2';
  const sufCat = (S.ui.cat && S.ui.cat !== 'todas') ? ' · ' + cap(S.ui.cat) : '';
  document.getElementById('conteo').textContent = p.length + ' palabras' + sufHSK + sufCat;
  document.getElementById('record-memorama').textContent = (S.games.mem.best[S.ui.memPairs || 8]) || '—';
  document.getElementById('record-quiz').textContent = dibEstrellas(estrellas('quiz')) + ' ' + S.games.quiz.best + ' pts';
  document.getElementById('record-escucha').textContent = dibEstrellas(estrellas('escucha')) + ' ' + S.games.escucha.best + ' pts';
  document.getElementById('record-pinyin').textContent = dibEstrellas(estrellas('pinyin')) + ' ' + S.games.pinyin.best + ' pts';
  document.getElementById('record-tonos').textContent = dibEstrellas(estrellas('tonos')) + ' ' + S.games.tonos.best + ' pts';
  document.getElementById('record-pronuncia').textContent = dibEstrellas(estrellas('pronuncia')) + ' ' + S.games.pronuncia.best + ' pts';
  document.getElementById('record-escribe').textContent = dibEstrellas(estrellas('escribe')) + ' ' + S.games.escribe.best + ' pts';
  const dd = document.getElementById('daily-due');
  if (dd) dd.textContent = repasoDebido() + ' palabras te esperan hoy';
  const deb = repasoDebido();
  const chip = document.getElementById('srs-due');
  chip.textContent = deb > 0 ? `🎯 ${deb} palabra${deb > 1 ? 's' : ''} para repasar hoy` : '✅ Repaso al día';
  chip.classList.toggle('due', deb > 0);
  document.getElementById('record-flash').textContent = S.games.flash.vistas + ' vistas';
  // progreso del camino
  let cStars = 0, cUnits = 0;
  UNIDADES.forEach(u => {
    const st = S.camino[u.id];
    if (st) { cStars += Object.values(st.stars || {}).reduce((a, b) => a + b, 0); if ((st.done || 0) >= 1) cUnits++; }
  });
  document.getElementById('record-ruta').textContent = cUnits === 0 ? 'Empieza hoy 🛤️' : `Unidad ${Math.min(cUnits + 1, UNIDADES.length)}/${UNIDADES.length} · ⭐${cStars}`;
  document.getElementById('record-cultura').textContent = `${leidos().length}/${POEMAS.length} poemas 🏮`;
  const cc = contadores(VOCAB);
  document.getElementById('record-cuaderno').textContent = `${cc.dominada}/${VOCAB.length} dominadas ✅`;
  document.getElementById('record-tienda').textContent = (S.coins || 0) + ' monedas';
}

// ===== Audio gratis (lento opcional) =====
function speak(zh, lento) {
  if (!S.ui.sonido) return;
  try {
    speechSynthesis.cancel();
    const u = new SpeechSynthesisUtterance(zh);
    u.lang = 'zh-CN'; u.rate = lento ? 0.5 : 0.85;
    speechSynthesis.speak(u);
  } catch (e) {}
}

// ===== Modo repaso (palabras débiles) =====
let repasoPool = null;
function empezarRepaso(game) {
  const deb = weakWords(pool(), 12);
  repasoPool = deb.length >= 4 ? deb : srsPick(pool(), 12);
  showView(game);
  if (game === 'quiz') nuevaPregunta();
  if (game === 'escucha') nuevaEscucha();
  if (game === 'pinyin') nuevaPinyin();
  if (game === 'tonos') { if (tModo === 'pares') nuevoPar(); else nuevoTono(); }
  if (game === 'pronuncia') nuevaPronuncia();
  pintarRepaso();
  toastLogro('🎯 Repasando tus ' + repasoPool.length + ' palabras débiles');
}
function salirRepaso() { repasoPool = null; pintarRepaso(); }
function pintarRepaso() {
  const on = !!repasoPool;
  document.querySelectorAll('.repaso-banner').forEach(b => {
    b.classList.toggle('hidden', !on);
    const c = b.querySelector('.repaso-count');
    if (c && on) c.textContent = repasoPool.length + ' débiles';
  });
}

// ================= MEMORAMA ZH-ES (con SRS) =================
let memCartas = [], memPrimera = null, memBloqueo = false, memIntentos = 0, memParejas = 0, memSeg = 0, memTimer = null, memTotal = 8;
function setMemPairs(n) {
  S.ui.memPairs = n;
  save();
  pintarPares();
  iniciarMemorama();
  actualizarMenu();
}
function pintarPares() {
  document.querySelectorAll('#mem-levels button').forEach(b =>
    b.classList.toggle('toggle-on', parseInt(b.dataset.n) === (S.ui.memPairs || 8)));
}
function iniciarMemorama() {
  const N = Math.min(S.ui.memPairs || 8, pool().length);
  const base = srsPick(pool(), N);
  memTotal = base.length;
  let mazo = [];
  base.forEach((w, i) => {
    mazo.push({ id: i * 2, pair: i, hz: w.hanzi, show: w.hanzi, sub: w.pinyin });
    mazo.push({ id: i * 2 + 1, pair: i, hz: w.hanzi, show: w.es, sub: '🔁 ' + w.hanzi });
  });
  mazo.sort(() => Math.random() - .5);
  memCartas = mazo; memPrimera = null; memBloqueo = false;
  memIntentos = 0; memParejas = 0; memSeg = 0;
  clearInterval(memTimer);
  document.getElementById('mem-win').classList.add('hidden');
  updMem();
  const board = document.getElementById('mem-board');
  board.innerHTML = '';
  mazo.forEach(c => {
    const d = document.createElement('div');
    d.className = 'mem-card zh';
    d.innerHTML = '❓';
    d.onclick = () => voltear(c.id, d);
    board.appendChild(d);
  });
  memTimer = setInterval(() => { memSeg++; document.getElementById('mem-tiempo').textContent = memSeg + 's'; }, 1000);
  pintarPares();
}
function updMem() {
  document.getElementById('mem-intentos').textContent = memIntentos;
  document.getElementById('mem-parejas').textContent = memParejas + '/' + memTotal;
  document.getElementById('mem-tiempo').textContent = memSeg + 's';
}
function voltear(id, el) {
  if (memBloqueo) return;
  const c = memCartas.find(x => x.id === id);
  if (c.done || el.classList.contains('open')) return;
  el.innerHTML = `<div>${c.show}</div><small>${c.sub}</small>`;
  el.classList.add('open');
  if (!memPrimera) { memPrimera = { c, el }; }
  else {
    memIntentos++;
    if (memPrimera.c.pair === c.pair) {
      memPrimera.c.done = true; c.done = true;
      memPrimera.el.classList.add('matched'); el.classList.add('matched');
      speak(c.hz);
      trackWord(c.hz, true);
      memPrimera = null; memParejas++;
      addXP(5);
      if (memParejas === memTotal) {
        clearInterval(memTimer);
        const msg = `🎉 ¡${memTotal} parejas en ${memIntentos} intentos y ${memSeg}s! +40 XP`;
        const w = document.getElementById('mem-win');
        w.textContent = msg; w.classList.remove('hidden');
        addXP(40); confeti();
        addMonedas(6);
        S.games.mem.wins++;
        const prevN = S.games.mem.best[memTotal] ? parseInt(S.games.mem.best[memTotal]) : Infinity;
        if (memIntentos < prevN) S.games.mem.best[memTotal] = `${memIntentos} intentos · ${memSeg}s`;
        save();
        maybeAchv('mem_win');
      }
    } else {
      trackWord(memPrimera.c.hz, false);
      trackWord(c.hz, false);
      memBloqueo = true;
      const p = memPrimera; memPrimera = null;
      setTimeout(() => { p.el.innerHTML = '❓'; p.el.classList.remove('open'); el.innerHTML = '❓'; el.classList.remove('open'); memBloqueo = false; }, 750);
    }
    updMem(); actualizarMenu();
  }
}

// ================= QUIZ (SRS + adaptativo + contrarreloj) =================
let qActual = null, qPts = 0, qStreak = 0, qN = 0;
let qContra = false, qTLeft = 0, qTTimer = null;
let qRonda = false, qRondaN = 0, qRondaOk = 0;
const RONDA_LEN = 10;
function toggleRonda() {
  if (qRonda) { finRonda(true); return; }
  if (qContra) finContra();
  qRonda = true; qRondaN = 0; qRondaOk = 0;
  const b = document.getElementById('btn-ronda');
  b.textContent = '⏹️ Terminar ronda';
  b.classList.add('toggle-on');
  nuevaPregunta();
  pintarRonda();
}
function pintarRonda() {
  const t = document.getElementById('q-ronda');
  t.classList.toggle('hidden', !qRonda);
  if (qRonda) t.textContent = `🎯 ${qRondaN}/${RONDA_LEN}`;
}
function finRonda(cancel) {
  qRonda = false;
  const b = document.getElementById('btn-ronda');
  b.textContent = '🎯 Ronda ×10';
  b.classList.remove('toggle-on');
  pintarRonda();
  if (cancel) return;
  const pct = Math.round(qRondaOk / RONDA_LEN * 100);
  document.getElementById('q-feedback').textContent =
    `🎯 Ronda completa: ${qRondaOk}/${RONDA_LEN} (${pct}%)` + (qRondaOk >= 7 ? ' ¡Brutal! +20 XP' : ' +5 XP por intentarlo');
    addXP(qRondaOk >= 7 ? 20 : 5);
    if (qRondaOk >= 7) { confeti(); addMonedas(5); }
  actualizarMenu();
}
function nuevaPregunta() {
  const lista = repasoPool || pool();
  qActual = rnd(lista);
  const dirEsAhSK = Math.random() < .5;
  const distract = distractores(qActual, pool(), 3, modoDificil('quiz'));
  const opts = [...distract, qActual].sort(() => Math.random() - .5);
  qN++;
  document.getElementById('q-n').textContent = qN;
  document.getElementById('q-feedback').textContent = '';
  document.getElementById('q-pregunta').innerHTML = dirEsAhSK
    ? `¿Cómo se dice <b>"${qActual.es}"</b> en chino? <button onclick="speak('${qActual.hanzi}')">🔊</button><br><small>sin pinyin: recuerda el hànzì</small>`
    : `¿Qué significa <b>${qActual.hanzi}</b>? <button onclick="speak('${qActual.hanzi}')">🔊</button><br><small>${qActual.pinyin}</small>`;
  const box = document.getElementById('q-opts');
  box.innerHTML = '';
  opts.forEach(o => {
    const b = document.createElement('button');
    b.className = 'quiz-opt';
    // En ES→ZH se oculta el pinyin: si no, se respondería emparejando pinyin sin saber nada
    b.textContent = dirEsAhSK ? o.hanzi : `${o.es} [${o.pinyin}]`;
    b.onclick = () => responderQuiz(o.hanzi === qActual.hanzi, b);
    box.appendChild(b);
  });
  if (!dirEsAhSK) speak(qActual.hanzi);
}
function responderQuiz(ok, btn) {
  const box = document.getElementById('q-opts');
  [...box.children].forEach(b => b.disabled = true);
  trackWord(qActual.hanzi, ok);
  trackGame('quiz', ok);
  if (ok) {
    qPts += 10 + Math.min(qStreak * 2, 10); qStreak++;
    btn.classList.add('good');
    const bonus = Math.min((qStreak - 1) * 2, 10);
    document.getElementById('q-feedback').textContent = `✅ ${elogio()} ${qActual.hanzi} = ${qActual.es} [${qActual.pinyin}]` + (qActual.fr ? ` 📖 ${qActual.fr.hz}` : '') + (bonus ? ` 🔥 racha x${qStreak} (+${bonus} extra)` : '');
    addXP(10);
    if (qStreak === 5) maybeAchv('quiz5');
    if (qStreak % 5 === 0) confeti();
  } else {
    qStreak = 0;
    btn.classList.add('bad');
    document.getElementById('q-feedback').textContent = `❌ Era: ${qActual.hanzi} = ${qActual.es} [${qActual.pinyin}]`;
  }
  document.getElementById('q-pts').textContent = qPts;
  document.getElementById('q-streak').textContent = qStreak;
  if (qPts > S.games.quiz.best) { S.games.quiz.best = qPts; save(); }
  actualizarMenu();
  if (qRonda) {
    qRondaN++; if (ok) qRondaOk++;
    pintarRonda();
    if (qRondaN >= RONDA_LEN) { finRonda(false); return; }
  }
  if (!qContra) setTimeout(nuevaPregunta, 1400);
  else setTimeout(() => { if (qContra) nuevaPregunta(); }, 900);
}
function toggleContra() {
  if (qContra) { finContra(); return; }
  if (qRonda) finRonda(true);
  if (estrellas('quiz') < 1) { toastLogro('⭐ Consigue 1 estrella en Quiz para desbloquear la contrarreloj'); return; }
  qContra = true; qTLeft = 45;
  const b = document.getElementById('btn-contra');
  b.textContent = '⏹️ Terminar';
  b.classList.add('toggle-on');
  nuevaPregunta();
  clearInterval(qTTimer);
  qTTimer = setInterval(() => { qTLeft--; pintarTimer(); if (qTLeft <= 0) finContra(); }, 1000);
  pintarTimer();
}
function pintarTimer() {
  const t = document.getElementById('q-timer');
  t.classList.toggle('hidden', !qContra);
  t.textContent = qContra ? ('⚡' + qTLeft + 's') : '';
}
function finContra() {
  clearInterval(qTTimer); qContra = false;
  const b = document.getElementById('btn-contra');
  b.textContent = '⚡ Contrarreloj';
  b.classList.remove('toggle-on');
  pintarTimer();
  document.getElementById('q-feedback').textContent = '⏱️ ¡Tiempo! Sesión: ' + qPts + ' pts, ' + qN + ' preguntas.';
}

// ================= ESCUCHA (SRS + adaptativo) =================
let eActual = null, ePts = 0, eN = 0, eStreak = 0;
function nuevaEscucha() {
  const lista = repasoPool || pool();
  eActual = rnd(lista);
  const distract = distractores(eActual, pool(), 3, modoDificil('escucha'));
  const opts = [...distract, eActual].sort(() => Math.random() - .5);
  eN++;
  document.getElementById('e-n').textContent = eN;
  document.getElementById('e-feedback').textContent = '';
  const box = document.getElementById('e-opts');
  box.innerHTML = '';
  opts.forEach(o => {
    const b = document.createElement('button');
    b.className = 'quiz-opt';
    b.textContent = `${o.es} · ${o.pinyin}`;
    b.onclick = () => {
      [...box.children].forEach(x => x.disabled = true);
      const ok = o.hanzi === eActual.hanzi;
      trackWord(eActual.hanzi, ok);
      trackGame('escucha', ok);
      if (ok) {
        ePts += 10; eStreak++; b.classList.add('good');
        document.getElementById('e-feedback').textContent = `✅ ${eActual.hanzi} [${eActual.pinyin}] = ${eActual.es}` + (eStreak >= 2 ? ` 🔥 x${eStreak}` : '');
        addXP(10);
        if (eStreak % 5 === 0) confeti();
      } else {
        eStreak = 0; b.classList.add('bad');
        document.getElementById('e-feedback').textContent = `❌ Era ${eActual.hanzi} [${eActual.pinyin}] = ${eActual.es}`;
      }
      document.getElementById('e-pts').textContent = ePts;
      document.getElementById('e-streak').textContent = eStreak;
      if (ePts > S.games.escucha.best) { S.games.escucha.best = ePts; save(); }
      actualizarMenu();
      setTimeout(nuevaEscucha, 1500);
    };
    box.appendChild(b);
  });
  speak(eActual.hanzi);
}
function repetirAudio() { if (eActual) speak(eActual.hanzi); }
function escucharLento() { if (eActual) speak(eActual.hanzi, true); }

// ================= TONOS (1º-4º) + PARES MÍNIMOS =================
let tActual = null, tPts = 0, tStreak = 0, tN = 0;
let tModo = 'palabras', tPar = null, tParItem = null;
const TONO_INFO = {
  1: { s: 'ˉ', n: '1º · alto y plano' },
  2: { s: 'ˊ', n: '2º · sube ↗' },
  3: { s: 'ˇ', n: '3º · baja y sube' },
  4: { s: 'ˋ', n: '4º · baja ↘' }
};
function setTonoModo(m) {
  tModo = m;
  document.querySelectorAll('#tono-modos button').forEach(b =>
    b.classList.toggle('toggle-on', b.dataset.m === m));
  if (m === 'pares') nuevoPar(); else nuevoTono();
}
function pintarTonoOpts(items, responder, targetId) {
  const box = document.getElementById(targetId || 'tono-opts');
  box.innerHTML = '';
  items.forEach(o => {
    const b = document.createElement('button');
    b.className = 'tono-pick';
    b.innerHTML = `<b>${o.hz}</b><small>${o.py} · ${o.es}</small>`;
    b.onclick = () => responder(o, b);
    box.appendChild(b);
  });
}
function nuevoPar() {
  tPar = rnd(PARES_TONO);
  tParItem = rnd(tPar.opts);
  tN++;
  document.getElementById('t-n').textContent = tN;
  document.getElementById('t-hanzi').textContent = '🎧 ' + tPar.sil + '…';
  document.getElementById('t-es').textContent = 'Misma sílaba, 4 tonos: ¿cuál oíste?';
  document.getElementById('t-feedback').textContent = '';
  pintarTonoOpts(tPar.opts, responderPar);
  speak(tParItem.hz);
}
function responderPar(o, btn) {
  document.querySelectorAll('.tono-pick').forEach(b => b.disabled = true);
  const ok = o.t === tParItem.t;
  trackGame('tonos', ok);
  if (ok) {
    tPts += 10; tStreak++;
    btn.classList.add('good');
    document.getElementById('t-feedback').textContent =
      `✅ ${elogio()} ${tParItem.hz} [${tParItem.py}] = ${tParItem.es} · tono ${tParItem.t}º ${TONO_INFO[tParItem.t].s}` + (tStreak >= 2 ? ` 🔥 x${tStreak}` : '');
    addXP(10);
    if (tStreak % 5 === 0) confeti();
  } else {
    tStreak = 0;
    btn.classList.add('bad');
    document.getElementById('t-feedback').textContent =
      `❌ Oíste ${tParItem.hz} [${tParItem.py}] tono ${tParItem.t}º ${TONO_INFO[tParItem.t].s} (${TONO_INFO[tParItem.t].n})`;
  }
  document.getElementById('t-pts').textContent = tPts;
  document.getElementById('t-streak').textContent = tStreak;
  if (tPts > S.games.tonos.best) { S.games.tonos.best = tPts; save(); }
  actualizarMenu();
  setTimeout(nuevoPar, 1800);
}
function repetirPar() { if (tParItem) speak(tParItem.hz); }
function tonoDe(pinyin) {
  const m = (pinyin || '').match(/[āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ]/);
  if (!m) return 0;
  const ch = m[0];
  if ('āēīōūǖ'.includes(ch)) return 1;
  if ('áéíóúǘ'.includes(ch)) return 2;
  if ('ǎěǐǒǔǚ'.includes(ch)) return 3;
  return 4;
}
function poolTonos() {
  const uno = w => [...w.hanzi].length === 1 && tonoDe(w.pinyin) >= 1;
  const base = (repasoPool || pool()).filter(uno);
  if (base.length >= 4) return base;
  return pool().filter(uno);
}
function nuevoTono() {
  tActual = rnd(poolTonos());
  tN++;
  document.getElementById('t-n').textContent = tN;
  document.getElementById('t-hanzi').textContent = tActual.hanzi;
  document.getElementById('t-es').textContent = tActual.es;
  document.getElementById('t-feedback').textContent = '';
  pintarTonoOpts([1, 2, 3, 4].map(t => ({ t, hz: TONO_INFO[t].s, py: t + 'º', es: TONO_INFO[t].n.split('· ')[1] })), responderTono);
  speak(tActual.hanzi);
}
function responderTono(o, btn) {
  if (!tActual) return;
  document.querySelectorAll('.tono-pick').forEach(b => b.disabled = true);
  const real = tonoDe(tActual.pinyin);
  const ok = o.t === real;
  trackWord(tActual.hanzi, ok);
  trackGame('tonos', ok);
  if (ok) {
    tPts += 10; tStreak++;
    btn.classList.add('good');
    document.getElementById('t-feedback').textContent =
      `✅ ${elogio()} ${tActual.hanzi} [${tActual.pinyin}] = ${tActual.es} — tono ${real}º ${TONO_INFO[real].s}` + (tStreak >= 2 ? ` 🔥 x${tStreak}` : '');
    addXP(10);
    if (tStreak % 5 === 0) confeti();
  } else {
    tStreak = 0;
    btn.classList.add('bad');
    document.getElementById('t-feedback').textContent =
      `❌ Era tono ${real}º ${TONO_INFO[real].s} (${TONO_INFO[real].n}): ${tActual.hanzi} [${tActual.pinyin}]`;
  }
  document.getElementById('t-pts').textContent = tPts;
  document.getElementById('t-streak').textContent = tStreak;
  if (tPts > S.games.tonos.best) { S.games.tonos.best = tPts; save(); }
  actualizarMenu();
  setTimeout(nuevoTono, 1800);
}
function repetirTono() { if (tModo === 'pares') repetirPar(); else if (tActual) speak(tActual.hanzi); }
function escucharTonoLento() { if (tModo === 'pares') { if (tParItem) speak(tParItem.hz, true); } else if (tActual) speak(tActual.hanzi, true); }

// ================= PRONUNCIA (shadowing + reconocimiento de voz) =================
let prActual = null, prPts = 0, prN = 0, prRec = null, prEscuchando = false;
function recDisponible() { return !!(window.SpeechRecognition || window.webkitSpeechRecognition); }
function initRec() {
  const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SR) return null;
  const r = new SR();
  r.lang = 'zh-CN'; r.interimResults = false; r.maxAlternatives = 5;
  r.onresult = e => {
    prEscuchando = false;
    pintarMicro();
    const alts = Array.from(e.results[0]).map(a => a.transcript).join(' ');
    document.getElementById('pr-oido').textContent = '🎧 Oí: ' + alts;
    const ok = alts.replace(/\s/g, '').includes(prActual.hanzi);
    trackGame('pronuncia', ok);
    trackWord(prActual.hanzi, ok);
    if (ok) {
      prPts += 15;
      document.getElementById('pr-feedback').textContent = `✅ ${elogio()} ¡Tu ${prActual.hanzi} [${prActual.pinyin}] se entendió! +15 XP`;
      addXP(15);
      addMonedas(2);
      if (S.games.pronuncia.ok >= 3) maybeAchv('voz');
      setTimeout(nuevaPronuncia, 1800);
    } else {
      document.getElementById('pr-feedback').textContent = `❌ No capté «${prActual.hanzi}». Escucha el modelo 🐢 y prueba otra vez 🎤`;
    }
    document.getElementById('pr-pts').textContent = prPts;
    if (prPts > S.games.pronuncia.best) { S.games.pronuncia.best = prPts; save(); }
    actualizarMenu();
  };
  r.onerror = ev => {
    prEscuchando = false;
    pintarMicro();
    document.getElementById('pr-feedback').textContent = '🎤 Problema con el micro (' + ev.error + '). Revisa permisos o autoevalúate abajo.';
  };
  r.onend = () => { prEscuchando = false; pintarMicro(); };
  return r;
}
function pintarMicro() {
  const b = document.getElementById('btn-micro');
  if (!b) return;
  b.textContent = prEscuchando ? '🔴 Escuchando…' : '🎤 Escúchame';
  b.classList.toggle('toggle-on', prEscuchando);
}
function nuevaPronuncia() {
  prActual = rnd(pool());
  prN++;
  document.getElementById('pr-n').textContent = prN;
  document.getElementById('pr-hanzi').textContent = prActual.hanzi;
  document.getElementById('pr-py').textContent = prActual.pinyin + ' · ' + prActual.es;
  document.getElementById('pr-oido').textContent = '';
  document.getElementById('pr-feedback').textContent = '';
  speak(prActual.hanzi);
}
function escucharYo() {
  if (!prRec) { toastLogro('😅 Tu navegador no reconoce voz. Usa Chrome/Edge o autoevalúate.'); return; }
  if (prEscuchando) return;
  prEscuchando = true;
  pintarMicro();
  document.getElementById('pr-feedback').textContent = '🎤 Habla ahora: di la palabra en chino…';
  try { prRec.start(); } catch (e) { prEscuchando = false; pintarMicro(); }
}
function prAuto(ok) {
  if (!prActual) return;
  trackGame('pronuncia', ok);
  trackWord(prActual.hanzi, ok);
  if (ok) {
    prPts += 10;
    document.getElementById('pr-feedback').textContent = `✅ Marcado como logrado. +10 XP`;
    addXP(10);
    addMonedas(2);
    if (S.games.pronuncia.ok >= 3) maybeAchv('voz');
    setTimeout(nuevaPronuncia, 1200);
  } else {
    document.getElementById('pr-feedback').textContent = `🔁 La repetiremos: ${prActual.hanzi} [${prActual.pinyin}] va a tu repaso 🎯`;
    setTimeout(nuevaPronuncia, 1500);
  }
  document.getElementById('pr-pts').textContent = prPts;
  if (prPts > S.games.pronuncia.best) { S.games.pronuncia.best = prPts; save(); }
  actualizarMenu();
}

// ================= PINYIN =================
let pActual = null, pPts = 0, pN = 0;
function quitarTonos(s) {
  return s.toLowerCase().trim()
    .replace(/ǖ/g, 'v').replace(/ǘ/g, 'v').replace(/ǚ/g, 'v').replace(/ǜ/g, 'v').replace(/ü/g, 'v')
    .replace(/[āáǎàa]/g, 'a').replace(/[ēéěèe]/g, 'e').replace(/[īíǐìi]/g, 'i')
    .replace(/[ōóǒòo]/g, 'o').replace(/[ūúǔùu]/g, 'u').replace(/ň/g, 'n').replace(/[^a-z ]/g, '')
    .replace(/\s+/g, ' ').trim();
}
function nuevaPinyin() {
  pActual = rnd(repasoPool || pool());
  pN++;
  document.getElementById('p-n').textContent = pN;
  document.getElementById('p-hanzi').textContent = pActual.hanzi;
  document.getElementById('p-es').textContent = pActual.es;
  document.getElementById('p-input').value = '';
  document.getElementById('p-feedback').textContent = '';
  document.getElementById('p-input').focus();
}
function escucharPinyin() { if (pActual) speak(pActual.hanzi); }
// Teclado de tonos para móvil/escritorio
const TONOS = ['ā','á','ǎ','à','ē','é','ě','è','ī','í','ǐ','ì','ō','ó','ǒ','ò','ū','ú','ǔ','ù','ǖ','ǘ','ǚ','ǜ','ü','ñ'];
function initTonoBtns() {
  const box = document.getElementById('tono-btns');
  if (!box || box.children.length) return;
  TONOS.forEach(ch => {
    const b = document.createElement('button');
    b.className = 'tono';
    b.textContent = ch;
    b.onclick = () => insertTono(ch);
    box.appendChild(b);
  });
}
function insertTono(ch) {
  const i = document.getElementById('p-input');
  const s = i.selectionStart !== null ? i.selectionStart : i.value.length;
  const e = i.selectionEnd !== null ? i.selectionEnd : s;
  i.value = i.value.slice(0, s) + ch + i.value.slice(e);
  i.focus();
  i.selectionStart = i.selectionEnd = s + 1;
}
function pistaPinyin() {
  if (!pActual) return;
  const sil = pActual.pinyin.split(' ')[0];
  if ((S.pistas || 0) > 0) {
    S.pistas--; save();
    document.getElementById('p-feedback').textContent = `💡 Pista pro: la 1ª sílaba es «${sil}» · quedan ${S.pistas}`;
  } else {
    document.getElementById('p-feedback').textContent = `💡 Empieza por «${sil.charAt(0)}»… (1ª sílaba: ${sil.length} letras). Pistas pro en 🪙 Tienda`;
  }
}
function comprobarPinyin() {
  const val = document.getElementById('p-input').value;
  if (!val.trim() || !pActual) return;
  const ok = quitarTonos(val) === quitarTonos(pActual.pinyin);
  trackWord(pActual.hanzi, ok);
  trackGame('pinyin', ok);
  if (ok) {
    pPts += 15;
    document.getElementById('p-feedback').textContent = `✅ ¡Correcto! ${pActual.hanzi} = [${pActual.pinyin}]`;
    addXP(15);
  } else {
    document.getElementById('p-feedback').textContent = `❌ Era [${pActual.pinyin}]. Tú: ${val}`;
  }
  document.getElementById('p-pts').textContent = pPts;
  if (pPts > S.games.pinyin.best) { S.games.pinyin.best = pPts; save(); }
  actualizarMenu();
  setTimeout(nuevaPinyin, 1600);
}

// iOS/móvil: la voz necesita un gesto previo; se desbloquea al primer toque
function desbloquearVoz() {
  try {
    if (speechSynthesis.getVoices) speechSynthesis.getVoices();
    const u = new SpeechSynthesisUtterance(' ');
    u.volume = 0;
    speechSynthesis.speak(u);
  } catch (e) {}
  document.removeEventListener('pointerdown', desbloquearVoz);
}
document.addEventListener('pointerdown', desbloquearVoz);
// ================= ESCRIBE (trazado con score por cobertura) =================
let esActual = null, esPts = [], esInk = [], esTrazando = false, esPtsSesion = 0;
const ES_SIZE = 300, ES_RADIO = 14, ES_MIN = 60;
function nuevaEscribe() {
  const unos = pool().filter(w => [...w.hanzi].length === 1);
  esActual = rnd(unos.length ? unos : pool());
  esPts = [];
  document.getElementById('es-hanzi').textContent = esActual.hanzi;
  document.getElementById('es-info').textContent = esActual.pinyin + ' · ' + esActual.es + ' — repásalo con el dedo o el mouse';
  document.getElementById('es-score').textContent = '';
  prepararLienzo();
}
function prepararLienzo() {
  const cv = document.getElementById('es-canvas');
  const dpr = window.devicePixelRatio || 1;
  cv.width = ES_SIZE * dpr; cv.height = ES_SIZE * dpr;
  cv.style.width = ES_SIZE + 'px'; cv.style.height = ES_SIZE + 'px';
  const g = cv.getContext('2d');
  g.setTransform(dpr, 0, 0, dpr, 0, 0);
  g.clearRect(0, 0, ES_SIZE, ES_SIZE);
  g.font = '500 ' + (ES_SIZE * 0.78) + 'px "Noto Sans SC", sans-serif';
  g.textAlign = 'center'; g.textBaseline = 'middle';
  g.fillStyle = 'rgba(128,138,155,.28)';
  g.fillText(esActual.hanzi, ES_SIZE / 2, ES_SIZE / 2 + ES_SIZE * 0.03);
  // muestras de tinta en canvas aparte
  const off = document.createElement('canvas');
  off.width = ES_SIZE; off.height = ES_SIZE;
  const o = off.getContext('2d');
  o.font = g.font; o.textAlign = 'center'; o.textBaseline = 'middle';
  o.fillStyle = '#000';
  o.fillText(esActual.hanzi, ES_SIZE / 2, ES_SIZE / 2 + ES_SIZE * 0.03);
  const d = o.getImageData(0, 0, ES_SIZE, ES_SIZE).data;
  esInk = [];
  for (let y = 0; y < ES_SIZE; y += 4) for (let x = 0; x < ES_SIZE; x += 4) {
    if (d[(y * ES_SIZE + x) * 4 + 3] > 120) esInk.push([x, y]);
  }
  redrawTrazos();
}
function redrawTrazos() {
  const cv = document.getElementById('es-canvas');
  const g = cv.getContext('2d');
  const dpr = window.devicePixelRatio || 1;
  g.setTransform(dpr, 0, 0, dpr, 0, 0);
  // redibuja guía + trazos
  g.clearRect(0, 0, ES_SIZE, ES_SIZE);
  g.font = '500 ' + ES_SIZE * 0.78 + 'px "Noto Sans SC", sans-serif';
  g.textAlign = 'center'; g.textBaseline = 'middle';
  g.fillStyle = 'rgba(128,138,155,.28)';
  g.fillText(esActual.hanzi, ES_SIZE / 2, ES_SIZE / 2 + ES_SIZE * 0.03);
  g.strokeStyle = document.documentElement.getAttribute('data-theme') === 'dark' ? '#f2f4f7' : '#1c1e21';
  g.lineWidth = ES_SIZE * 0.055; g.lineCap = 'round'; g.lineJoin = 'round';
  esPts.forEach(st => {
    if (st.length < 1) return;
    g.beginPath();
    g.moveTo(st[0][0], st[0][1]);
    st.forEach(p => g.lineTo(p[0], p[1]));
    g.stroke();
  });
}
function esPos(e) {
  const r = e.target.getBoundingClientRect();
  return [e.clientX - r.left, e.clientY - r.top];
}
function esDown(e) {
  esTrazando = true;
  esPts.push([esPos(e)]);
  e.target.setPointerCapture(e.pointerId);
  redrawTrazos();
}
function esMove(e) {
  if (!esTrazando) return;
  esPts[esPts.length - 1].push(esPos(e));
  redrawTrazos();
}
function esUp() { esTrazando = false; }
function limpiarLienzo() { esPts = []; redrawTrazos(); document.getElementById('es-score').textContent = ''; }
function comprobarEscribe() {
  if (!esPts.length || !esInk.length) { document.getElementById('es-score').textContent = '✍️ Dibújalo primero sobre la guía'; return; }
  const R2 = ES_RADIO * ES_RADIO;
  let hit = 0;
  for (const [x, y] of esInk) {
    for (const st of esPts) {
      let ok = false;
      for (const [px, py] of st) { const dx = x - px, dy = y - py; if (dx * dx + dy * dy < R2) { ok = true; break; } }
      if (ok) { hit++; break; }
    }
  }
  const cov = Math.round(hit / esInk.length * 100);
  const pass = cov >= ES_MIN;
  trackGame('escribe', pass);
  trackWord(esActual.hanzi, pass);
  document.getElementById('es-score').textContent = pass
    ? `✅ ${cov}% de cobertura · ¡ese ${esActual.hanzi} ya es tuyo! +15 XP`
    : `❌ ${cov}% — te faltó tinta (mínimo ${ES_MIN}%). Limpia y otra vez`;
  if (pass) {
    esPtsSesion += 15;
    addXP(15);
    addMonedas(2);
    if (S.games.escribe.ok >= 3) maybeAchv('trazo');
    setTimeout(nuevaEscribe, 1600);
  }
  if (esPtsSesion > S.games.escribe.best) { S.games.escribe.best = esPtsSesion; save(); }
  actualizarMenu();
}

// ================= CAMINO DUOLINGO: unidades + lecciones + corazones =================
let lec = null; // {unit, level, queue:[{type,word}], idx, hearts, ok}
const CICLO_TIPOS = ['quiz', 'escucha', 'pinyin', 'tono'];
function tipoPara(level, i) {
  if (level === 1) return 'quiz';
  if (level === 2) return (i % 3 === 2) ? 'escucha' : 'quiz';
  return CICLO_TIPOS[i % 4]; // nivel 3 y sesión diaria: mezcla completa
}
function unidadDesbloqueada(i) {
  if (i === 0) return true;
  const prev = UNIDADES[i - 1].id;
  return ((S.camino[prev] || {}).done || 0) >= 1;
}
function corazonesInicio() {
  let h = 3;
  const ex = Math.min(S.corazonesExtra || 0, 2);
  if (ex) { h += ex; S.corazonesExtra -= ex; save(); }
  return h;
}
function empezarLeccion(uid, level) {
  const words = unitWords(uid);
  if (words.length < 4) { toastLogro('😅 Pocas palabras en esta unidad'); return; }
  const total = NIVEL_LEN[level - 1];
  const elegidas = srsPick(words, Math.min(total, words.length));
  while (elegidas.length < total) elegidas.push(rnd(words));
  lec = {
    unit: uid, level,
    queue: elegidas.map((w, i) => ({ type: tipoPara(level, i), word: w })),
    idx: 0, hearts: corazonesInicio(), ok: 0
  };
  showView('leccion');
  pintarLeccion();
}
// 🎯 Sesión diaria: 10 palabras debidas (o SRS) con todos los tipos
function empezarSesion() {
  const hoy = Date.now();
  let deb = pool().filter(w => { const e = cajaEntry(w.hanzi); return e && e.due <= hoy; });
  if (deb.length < 6) deb = srsPick(pool(), 10);
  deb = deb.slice(0, 10);
  lec = {
    unit: 'diaria', level: 0,
    queue: deb.map((w, i) => ({ type: CICLO_TIPOS[i % 4], word: w })),
    idx: 0, hearts: corazonesInicio(), ok: 0
  };
  showView('leccion');
  pintarLeccion();
}
function salirLeccion() { lec = null; showView('ruta'); }
function pintarHearts() {
  document.getElementById('lec-hearts').textContent = '❤️'.repeat(Math.max(0, lec.hearts)) + '🖤'.repeat(3 - Math.max(0, lec.hearts));
}
function pintarLeccion() {
  const item = lec.queue[lec.idx];
  const w = item.word;
  const u = UNIDADES.find(x => x.id === lec.unit);
  document.getElementById('lec-title').textContent = lec.unit === 'diaria'
    ? '🎯 Sesión diaria · repaso inteligente'
    : `${u.emoji} ${u.nombre} · Nivel ${lec.level}`;
  document.getElementById('lec-prog-fill').style.width = Math.round(lec.idx / lec.queue.length * 100) + '%';
  pintarHearts();
  document.getElementById('lec-feedback').textContent = '';
  const wrap = document.getElementById('lec-input-wrap');
  wrap.classList.add('hidden');
  document.getElementById('lec-play').classList.add('hidden');
  const box = document.getElementById('lec-opts');
  box.innerHTML = '';
  let lista = unitWords(lec.unit);
  if (lista.length < 4) lista = pool();
  const dificil = lec.level === 3 || lec.unit === 'diaria';
  if (item.type === 'quiz') {
    const dirEs = lec.level >= 2 && Math.random() < 0.5;
    document.getElementById('lec-q').innerHTML = dirEs
      ? `¿Cómo se dice <b>"${w.es}"</b> en chino?<br><small>sin pinyin: recuerda el hànzì</small>`
      : `¿Qué significa <b>${w.hanzi}</b>? <button onclick="speak('${w.hanzi}')">🔊</button><br><small>${w.pinyin}</small>`;
    const opts = [...distractores(w, lista, 3, dificil), w].sort(() => Math.random() - 0.5);
    opts.forEach(o => {
      const b = document.createElement('button');
      b.className = 'quiz-opt';
      b.textContent = dirEs ? o.hanzi : `${o.es} [${o.pinyin}]`;
      b.onclick = () => responderLeccion(o.hanzi === w.hanzi, b);
      box.appendChild(b);
    });
    if (!dirEs) speak(w.hanzi);
  } else if (item.type === 'escucha') {
    document.getElementById('lec-q').innerHTML = `🎧 Escucha y elige el significado`;
    const play = document.getElementById('lec-play');
    play.classList.remove('hidden');
    play.querySelector('button').onclick = () => speak(w.hanzi);
    const opts = [...distractores(w, lista, 3, dificil), w].sort(() => Math.random() - 0.5);
    opts.forEach(o => {
      const b = document.createElement('button');
      b.className = 'quiz-opt';
      b.textContent = `${o.es} · ${o.pinyin}`;
      b.onclick = () => responderLeccion(o.hanzi === w.hanzi, b);
      box.appendChild(b);
    });
    speak(w.hanzi);
  } else if (item.type === 'pinyin') {
    document.getElementById('lec-q').innerHTML = `⌨️ Escribe el pinyin de <b>${w.hanzi}</b><br><small>${w.es}</small>`;
    wrap.classList.remove('hidden');
    const inp = document.getElementById('lec-input');
    inp.value = '';
    setTimeout(() => inp.focus(), 60);
  } else { // tono
    document.getElementById('lec-q').innerHTML = `🔔 ¿Qué tono tiene <b>${w.hanzi}</b>? <button onclick="speak('${w.hanzi}')">🔊</button>`;
    pintarTonoOpts([1, 2, 3, 4].map(t => ({ t, hz: TONO_INFO[t].s, py: t + 'º', es: TONO_INFO[t].n.split('· ')[1] })),
      (o, b) => responderLeccion(o.t === tonoDe(w.pinyin), b), 'lec-opts');
    speak(w.hanzi);
  }
}
function evaluarLecPinyin() {
  if (!lec) return;
  const w = lec.queue[lec.idx].word;
  const val = document.getElementById('lec-input').value;
  if (!val.trim()) return;
  responderLeccion(quitarTonos(val) === quitarTonos(w.pinyin), null);
}
function responderLeccion(ok, btn) {
  const item = lec.queue[lec.idx];
  const w = item.word;
  [...document.getElementById('lec-opts').children].forEach(b => b.disabled = true);
  document.getElementById('lec-input-wrap').classList.add('hidden');
  const juego = item.type === 'tono' ? 'tonos' : item.type;
  trackWord(w.hanzi, ok);
  trackGame(juego, ok);
  if (ok) {
    lec.ok++;
    if (btn) btn.classList.add('good');
    document.getElementById('lec-feedback').textContent = `✅ ${elogio()} ${w.hanzi} = ${w.es} [${w.pinyin}]`;
    addXP(10);
  } else {
    lec.hearts--;
    pintarHearts();
    if (btn) btn.classList.add('bad');
    document.getElementById('lec-feedback').textContent = `❌ Era: ${w.hanzi} = ${w.es} [${w.pinyin}] · 💔 -1 corazón`;
  }
  actualizarMenu();
  lec.idx++;
  document.getElementById('lec-prog-fill').style.width = Math.round(lec.idx / lec.queue.length * 100) + '%';
  if (lec.hearts <= 0) { setTimeout(() => finLeccion(false), 1500); return; }
  if (lec.idx >= lec.queue.length) { setTimeout(() => finLeccion(true), 1500); return; }
  setTimeout(pintarLeccion, 1300);
}
function finLeccion(pass) {
  const stars = !pass ? 0 : (lec.hearts === 3 ? 3 : 2);
  const uIdx = UNIDADES.findIndex(x => x.id === lec.unit);
  const esDiaria = lec.unit === 'diaria';
  let bonus = 0, sigBtn = '';
  if (pass) {
    if (!esDiaria) {
      const cu = S.camino[lec.unit] || (S.camino[lec.unit] = { done: 0, stars: {} });
      cu.done = Math.max(cu.done, lec.level);
      cu.stars[lec.level] = Math.max(cu.stars[lec.level] || 0, stars);
      save();
      bonus = lec.level * 10 + stars * 5;
      maybeAchv('leccion1');
      if (cu.done >= 3) maybeAchv('unidad1');
      if (lec.level < 3) {
        sigBtn = `<button class="btn primary" onclick="empezarLeccion('${lec.unit}',${lec.level + 1})">Siguiente: Nivel ${lec.level + 1} →</button>`;
      } else if (uIdx < UNIDADES.length - 1) {
        const nx = UNIDADES[uIdx + 1];
        sigBtn = `<button class="btn primary" onclick="empezarLeccion('${nx.id}',1)">Siguiente: ${nx.emoji} ${nx.nombre} →</button>`;
      }
    } else {
      bonus = 20 + stars * 5;
      sigBtn = `<button class="btn primary" onclick="empezarSesion()">🎯 Otra sesión →</button>`;
    }
    addXP(bonus); confeti();
    addMonedas(esDiaria ? 8 + stars * 2 : 5 + stars * 2);
  }
  document.getElementById('lec-title').textContent = pass
    ? (esDiaria ? '🎯 ¡Sesión diaria completa!' : '🎉 ¡Lección superada!')
    : '💔 ¡Sin corazones!';
  document.getElementById('lec-play').classList.add('hidden');
  document.getElementById('lec-input-wrap').classList.add('hidden');
  document.getElementById('lec-q').innerHTML = pass
    ? `<b>${'★'.repeat(stars)}${'☆'.repeat(3 - stars)}</b><br>✅ ${lec.ok}/${lec.queue.length} · +${bonus} XP bonus`
    : `✅ ${lec.ok}/${lec.queue.length} · Las débiles irán al repaso 🎯`;
  const repetir = esDiaria
    ? `<button class="btn" onclick="empezarSesion()">🔄 Repetir</button>`
    : `<button class="btn" onclick="empezarLeccion('${lec.unit}',${lec.level})">🔄 Repetir</button>`;
  document.getElementById('lec-opts').innerHTML = repetir + sigBtn +
    `<button class="btn ghost" onclick="showView('ruta')">🛤️ Ruta</button>`;
  document.getElementById('lec-feedback').textContent = '';
  actualizarMenu();
}
function renderRuta() {
  const wrap = document.getElementById('ruta-path');
  wrap.innerHTML = '';
  UNIDADES.forEach((u, i) => {
    const st = S.camino[u.id] || { done: 0, stars: {} };
    const unlocked = unidadDesbloqueada(i);
    const words = unitWords(u.id).length;
    const sec = document.createElement('div');
    sec.className = 'ruta-unit' + (unlocked ? '' : ' locked');
    let nodos = '';
    for (let L = 1; L <= 3; L++) {
      const done = st.done >= L;
      const current = unlocked && st.done === L - 1;
      const cls = done ? 'done' : (current ? 'current' : 'locked');
      const inner = done ? ('★'.repeat(st.stars[L] || 1)) : (current ? L : '🔒');
      nodos += `<button class="node ${cls}" ${done || current ? `onclick="empezarLeccion('${u.id}',${L})"` : ''} title="Nivel ${L}">${inner}</button>`;
      if (L < 3) nodos += '<span class="node-link"></span>';
    }
    sec.innerHTML = `<div class="ruta-head"><span class="ruta-emoji">${unlocked ? u.emoji : '🔒'}</span><div><b>${u.nombre}</b><small>${u.desc} · ${words} palabras</small></div></div><div class="ruta-nodes">${nodos}</div>`;
    wrap.appendChild(sec);
  });
  // progreso total
  let stars = 0, units = 0;
  UNIDADES.forEach(u => {
    const st = S.camino[u.id];
    if (st) { stars += Object.values(st.stars || {}).reduce((a, b) => a + b, 0); if ((st.done || 0) >= 1) units++; }
  });
  document.getElementById('ruta-resumen').textContent = units === 0
    ? 'Empieza por 👋 Saludos y desbloquea el resto'
    : `Unidades abiertas: ${units}/${UNIDADES.length} · ⭐ ${stars}/72`;
}

// ===== Atajos de teclado =====
const activo = id => document.getElementById(id).classList.contains('active');
document.addEventListener('keydown', e => {
  if (e.key === 'Enter' && activo('view-leccion') && document.activeElement && document.activeElement.id === 'lec-input') { evaluarLecPinyin(); return; }
  if (e.key === 'Enter' && activo('view-pinyin')) { comprobarPinyin(); return; }
  if (activo('view-flash') && (e.key === ' ' || e.key === 'Enter')) {
    if (document.activeElement.tagName !== 'INPUT') { e.preventDefault(); voltearFlash(); }
    return;
  }
  if (activo('view-flash') && e.key === 'ArrowRight') { flashSi(); return; }
  if (activo('view-flash') && e.key === 'ArrowLeft') { flashNo(); return; }
  const n = parseInt(e.key);
  if (n >= 1 && n <= 4 && (activo('view-quiz') || activo('view-escucha'))) {
    const box = document.getElementById(activo('view-quiz') ? 'q-opts' : 'e-opts');
    const b = box.children[n - 1];
    if (b && !b.disabled) b.click();
  }
  if (n >= 1 && n <= 4 && activo('view-tonos')) {
    const b = document.querySelectorAll('.tono-pick')[n - 1];
    if (b && !b.disabled) b.click();
  }
  if (n >= 1 && n <= 4 && activo('view-leccion')) {
    const b = document.getElementById('lec-opts').children[n - 1];
    if (b && b.classList.contains('quiz-opt') && !b.disabled) b.click();
  }
});

// ================= FLASHCARDS =================
let fMazo = [], fIdx = 0, fVolteada = false;
function nuevoMazoFlash() {
  fMazo = muestra(pool(), 20);
  fIdx = 0; fVolteada = false;
  mostrarFlash();
}
function mazoDebiles() {
  const deb = weakWords(pool(), 20);
  fMazo = deb.length >= 4 ? deb : srsPick(pool(), 20);
  fIdx = 0; fVolteada = false;
  showView('flash');
  mostrarFlash();
  toastLogro('🎯 Mazo de débiles: ' + fMazo.length + ' fichas');
}
function mostrarFlash() {
  if (!fMazo.length) nuevoMazoFlash();
  if (fIdx >= fMazo.length) { fIdx = 0; fMazo.sort(() => Math.random() - .5); }
  const w = fMazo[fIdx];
  document.getElementById('f-n').textContent = (fIdx + 1) + '/' + fMazo.length;
  document.getElementById('f-prog-fill').style.width = Math.round(fIdx / fMazo.length * 100) + '%';
  document.getElementById('f-front').textContent = w.hanzi;
  document.getElementById('f-back').innerHTML =
    `<span class="hanzi">${w.hanzi}</span><br>${w.pinyin}<br>${w.es}` +
    (w.rad ? `<div class="flash-anat">🔍 ${w.rad}${w.mnemo ? ' · ' + w.mnemo : ''}</div>` : '') +
    (w.fr ? `<div class="flash-fr">📖 ${w.fr.hz}<small>${w.fr.py} · ${w.fr.es}</small></div>` : '') +
    `<small>HSK${w.hsk} · ${w.cat}</small>`;
  document.getElementById('f-back').classList.add('hidden');
  document.getElementById('f-front').classList.remove('hidden');
  fVolteada = false;
}
function voltearFlash() {
  fVolteada = !fVolteada;
  document.getElementById('f-back').classList.toggle('hidden', !fVolteada);
  document.getElementById('f-front').classList.toggle('hidden', fVolteada);
  if (fVolteada) speak(fMazo[fIdx].hanzi);
}
function escucharFlash() { speak(fMazo[fIdx].hanzi); }
function sumarVista() {
  S.games.flash.vistas++;
  save();
  document.getElementById('f-vistas').textContent = S.games.flash.vistas;
  if (S.games.flash.vistas >= 25) maybeAchv('lector25');
  actualizarMenu();
}
function flashSi() {
  trackWord(fMazo[fIdx].hanzi, true);
  sumarVista();
  addXP(5); fIdx++; mostrarFlash();
}
function flashNo() {
  trackWord(fMazo[fIdx].hanzi, false);
  sumarVista();
  fMazo.push(fMazo[fIdx]); fIdx++; mostrarFlash();
}

// ================= PROGRESO =================
function renderProgreso() {
  // Meta diaria (anillo)
  const hoyXP = S.xpByDay[diaLocal()] || 0;
  const C = 2 * Math.PI * 34;
  const fg = document.getElementById('pg-ring-fg');
  fg.style.strokeDasharray = C;
  fg.style.strokeDashoffset = C * (1 - Math.min(hoyXP / S.goal, 1));
  document.getElementById('pg-meta-txt').innerHTML = `<b>${hoyXP}</b> / ${S.goal} XP hoy` + (hoyXP >= S.goal ? ' 🎯' : '');

  // Últimos 7 días
  let max = 1;
  const dias = [];
  for (let i = 6; i >= 0; i--) {
    const d = new Date(Date.now() - i * 864e5);
    const k = diaLocal(d);
    const xp = S.xpByDay[k] || 0;
    max = Math.max(max, xp);
    dias.push({ et: d.getDate() + '/' + (d.getMonth() + 1), xp });
  }
  document.getElementById('pg-week').innerHTML = dias.map(d =>
    `<div class="wk"><div class="wk-bar" style="height:${Math.max(6, Math.round(d.xp / max * 100))}%" title="${d.xp} XP"></div><span>${d.et}</span><b>${d.xp}</b></div>`
  ).join('');

  // Juegos
  const fila = (nombre, g, extra) => {
    const G = S.games[g];
    const acc = G.played ? Math.round(G.ok / G.played * 100) + '%' : '—';
    return `<div class="pg-row"><span>${nombre}</span><span>${G.played || 0} jugadas</span><span>precisión ${acc}</span><span class="stars">${g === 'mem' ? ('🏆 ' + (G.wins || 0) + ' wins') : dibEstrellas(estrellas(g))}</span><span>${extra || ''}</span></div>`;
  };
  document.getElementById('pg-games').innerHTML =
    fila('✅ Quiz', 'quiz', S.games.quiz.best + ' pts récord') +
    fila('🔊 Escucha', 'escucha', S.games.escucha.best + ' pts récord') +
    fila('⌨️ Pinyin', 'pinyin', S.games.pinyin.best + ' pts récord') +
    fila('🔔 Tonos', 'tonos', S.games.tonos.best + ' pts récord') +
    fila('🎤 Pronuncia', 'pronuncia', S.games.pronuncia.best + ' pts récord') +
    fila('✍️ Escribe', 'escribe', S.games.escribe.best + ' pts récord') +
    fila('🃏 Memorama', 'mem', (S.games.mem.best[S.ui.memPairs || 8] ? ('récord ' + S.games.mem.best[S.ui.memPairs || 8]) : 'sin récord')) +
    fila('📇 Flashcards', 'flash', S.games.flash.vistas + ' vistas');

  // Débiles
  const deb = weakWords(pool(), 10);
  document.getElementById('pg-weak').innerHTML = deb.length
    ? deb.map(w => {
        const e = S.words[w.hanzi];
        return `<button class="chip" onclick="speak('${w.hanzi}')" title="${w.es} · fallos: ${e.fail}">${w.hanzi}<small>${w.es} · ❌${e.fail}</small></button>`;
      }).join('')
    : '<p class="pg-empty">Sin fallos registrados. ¡Juega y aquí aparecerán tus palabras a reforzar! 🐼</p>';

  // Logros
  document.getElementById('pg-logros').innerHTML = LOGROS.map(a => {
    const ok = !!S.achv[a.id];
    return `<div class="achv ${ok ? 'on' : ''}"><span>${ok ? a.emoji : '🔒'}</span><b>${a.nombre}</b><small>${a.desc}</small></div>`;
  }).join('');

  // Respaldo
  document.getElementById('pg-backup').textContent = S.lastBackup
    ? 'Último respaldo: ' + new Date(S.lastBackup).toLocaleString()
    : 'Sin respaldo todavía';
  document.querySelectorAll('#goal-set .chip-f').forEach(b =>
    b.classList.toggle('active', parseInt(b.dataset.g) === S.goal));
}

// ================= CULTURA: poemas + fiestas =================
function leidos() {
  if (!S.cultura || !Array.isArray(S.cultura.leidos)) S.cultura = { leidos: [] };
  return S.cultura.leidos;
}
function renderCultura() {
  const box = document.getElementById('cult-poemas');
  box.innerHTML = '';
  const le = leidos();
  POEMAS.forEach(p => {
    const leido = le.includes(p.id);
    const art = document.createElement('article');
    art.className = 'poema' + (leido ? ' leido' : '');
    art.innerHTML =
      `<div class="poema-head"><span class="poema-emoji">${p.emoji}</span>` +
      `<div><b>${p.titulo} <small>[${p.pinyinT}]</small></b><small>${p.tituloEs} · ${p.poeta} (${p.poetaP}) · ${p.dinastia}</small></div>` +
      `<span>${leido ? '✅' : ''}</span></div>` +
      `<p class="poema-nota">${p.nota}</p>` +
      `<div>${p.lineas.map((l, i) =>
        `<div class="poema-linea" onclick="toggleLinea(this,'${p.id}',${i})">` +
        `<div class="hanzi">${l.hz}</div>` +
        `<div class="poema-sub hidden"><div>${l.py}</div><div>${l.es}</div></div></div>`
      ).join('')}</div>` +
      `<div class="controls"><button class="btn ghost sm" onclick="escucharPoema('${p.id}')">🔊 Poema completo</button>` +
      (leido ? '' : ` <button class="btn sm" onclick="marcarLeido('${p.id}')">✅ Marcar leído · +15 XP</button>`) + `</div>`;
    box.appendChild(art);
  });
  document.getElementById('cult-fiestas').innerHTML = CULTURA.map(c =>
    `<div class="cult-card"><div class="card-emoji">${c.emoji}</div>` +
    `<h3>${c.zh} <small>[${c.py}]</small></h3><b>${c.es}</b><p>${c.texto}</p>` +
    `<button class="btn ghost sm" onclick="speak('${c.zh}')">🔊 Escuchar</button></div>`
  ).join('');
  actualizarMenu();
}
function toggleLinea(el, pid, i) {
  const p = POEMAS.find(x => x.id === pid);
  el.querySelector('.poema-sub').classList.toggle('hidden');
  speak(p.lineas[i].hz);
}
function escucharPoema(pid) {
  const p = POEMAS.find(x => x.id === pid);
  speak(p.lineas.map(l => l.hz).join('。'));
}
function marcarLeido(pid) {
  const le = leidos();
  if (le.includes(pid)) return;
  le.push(pid);
  save();
  addXP(15);
  if (le.length >= 3) maybeAchv('poeta');
  renderCultura();
}

// ================= CUADERNO PERSONAL =================
let cuFiltro = 'todas', cuBusca = '';
const CU_ETI = { dominada: '✅ dominada', aprendiendo: '📘 aprendiendo', debil: '🎯 débil', nueva: '⬜ nueva' };
function cuSetFiltro(f) {
  cuFiltro = f;
  document.querySelectorAll('#cu-filtros .chip-f').forEach(b => b.classList.toggle('active', b.dataset.f === f));
  renderCuaderno();
}
function renderCuaderno() {
  const c = contadores(VOCAB);
  const total = VOCAB.length;
  document.getElementById('cu-stats').innerHTML =
    `<div class="cu-stat"><b>${c.dominada}</b><span>✅ dominadas</span></div>` +
    `<div class="cu-stat"><b>${c.aprendiendo}</b><span>📘 aprendiendo</span></div>` +
    `<div class="cu-stat"><b>${c.debil}</b><span>🎯 débiles</span></div>` +
    `<div class="cu-stat"><b>${c.nueva}</b><span>⬜ nuevas</span></div>`;
  const pct = Math.round(c.dominada / total * 100);
  document.getElementById('cu-bar-fill').style.width = pct + '%';
  document.getElementById('cu-bar-txt').textContent = `Has dominado ${c.dominada} de ${total} palabras (${pct}%)`;
  const q = (cuBusca || '').toLowerCase().trim();
  const orden = { debil: 0, aprendiendo: 1, nueva: 2, dominada: 3 };
  let rows = VOCAB.map(w => ({ w, st: wordStatus(w.hanzi) }));
  if (cuFiltro !== 'todas') rows = rows.filter(r => r.st === cuFiltro);
  if (q) rows = rows.filter(r =>
    r.w.hanzi.includes(cuBusca.trim()) ||
    r.w.pinyin.toLowerCase().includes(q) ||
    r.w.es.toLowerCase().includes(q));
  rows.sort((a, b) => orden[a.st] - orden[b.st] || a.w.hsk - b.w.hsk || a.w.hanzi.localeCompare(b.w.hanzi));
  document.getElementById('cu-list').innerHTML = rows.length ? rows.map(r => {
    const e = S.words[r.w.hanzi];
    return `<div class="cu-row st-${r.st}">` +
      `<button class="cu-hz" onclick="speak('${r.w.hanzi}')" title="Escuchar">${r.w.hanzi}<small>${r.w.pinyin}</small></button>` +
      `<div class="cu-mid"><b>${r.w.es}</b><small>HSK${r.w.hsk} · ${r.w.cat} · ${e ? `✅${e.ok} ❌${e.fail} · caja ${e.box || 1}` : 'sin practicar'}</small></div>` +
      `<span class="st-badge">${CU_ETI[r.st]}</span></div>`;
  }).join('') : '<p class="pg-empty">Nada por aquí con ese filtro 🔍</p>';
}

// ================= TIENDA 🪙 (pay-to-win con monedas del juego) =================
const TIENDA = [
  { id: 'corazon', emoji: '❤️', nombre: '+1 corazón', costo: 30, desc: 'Si estás en una lección, recupera 1 corazón al instante. Si no, guarda +1 para tu próxima lección (máx 2).' },
  { id: 'freeze', emoji: '🧊', nombre: 'Protector de racha', costo: 50, desc: 'Si un día no practicas, tu racha 🔥 no se rompe. Un uso por día perdido.' },
  { id: 'xp2', emoji: '⚡', nombre: 'XP x2', costo: 40, desc: 'Tus próximas 10 ganancias de XP cuentan doble.' },
  { id: 'pista', emoji: '💡', nombre: 'Pack 3 pistas pro', costo: 25, desc: 'En ⌨️ Pinyin, la pista te revela la 1ª sílaba completa con tono.' },
];
function comprar(id) {
  const it = TIENDA.find(x => x.id === id);
  if (!it) return;
  if ((S.coins || 0) < it.costo) { toastLogro('🪙 Te faltan monedas: gana jugando lecciones y sesiones'); return; }
  S.coins -= it.costo;
  S.gastado = (S.gastado || 0) + it.costo;
  S.items = S.items || {};
  S.items[id] = (S.items[id] || 0) + 1;
  if (id === 'corazon') {
    if (lec && lec.hearts < 3) { lec.hearts++; pintarHearts(); toastLogro('❤️ +1 corazón ahora mismo'); }
    else { S.corazonesExtra = Math.min((S.corazonesExtra || 0) + 1, 2); toastLogro('❤️ Guardado para tu próxima lección'); }
  }
  if (id === 'freeze') { S.freeze = (S.freeze || 0) + 1; toastLogro('🧊 Protector activo (' + S.freeze + ' en reserva)'); }
  if (id === 'xp2') { S.xp2 = (S.xp2 || 0) + 10; toastLogro('⚡ XP x2 durante 10 ganancias'); }
  if (id === 'pista') { S.pistas = (S.pistas || 0) + 3; toastLogro('💡 +3 pistas pro (' + S.pistas + ' disponibles)'); }
  save();
  if (S.gastado >= 100) maybeAchv('mercader');
  actualizarMenu();
  renderTienda();
}
function renderTienda() {
  const wrap = document.getElementById('shop-items');
  if (!wrap) return;
  wrap.innerHTML = TIENDA.map(it => {
    const stock = it.id === 'freeze' ? (S.freeze || 0) : it.id === 'xp2' ? (S.xp2 || 0) : it.id === 'pista' ? (S.pistas || 0) : (S.corazonesExtra || 0);
    return `<div class="card shop-item">
      <div class="card-emoji">${it.emoji}</div>
      <h3>${it.nombre}</h3>
      <p>${it.desc}</p>
      <div class="record">En reserva: <b>${stock}</b> · comprados: ${S.items[it.id] || 0}</div>
      <button class="btn ${S.coins >= it.costo ? '' : 'ghost'}" onclick="comprar('${it.id}')" ${S.coins >= it.costo ? '' : 'disabled'}>🪙 ${it.costo}</button>
    </div>`;
  }).join('');
  document.getElementById('shop-coins').textContent = '🪙 Tienes ' + (S.coins || 0) + ' monedas · gastadas: ' + (S.gastado || 0);
}

// ===== Onboarding (primera visita) =====
const OB_PASOS = [
  { e: '🐼', t: 'Bienvenido a NiHao Lab', p: 'Aprenderás chino jugando: 5 minutos al día bastan. Tu progreso se guarda en este dispositivo.' },
  { e: '🛤️', t: 'Sigue la ruta', p: 'Unidades por niveles con corazones y estrellas, como un juego. La 🎯 sesión diaria repasa justo lo que estás por olvidar.' },
  { e: '🔔', t: 'Tonos y caracteres', p: 'El tono lo cambia todo (mā ≠ mǎ). Entrena el oído, traza los caracteres con ✍️ y habla con 🎤 desde el día 1.' },
];
let obIdx = 0;
function mostrarOnboard() {
  if (S.ui.onboard) return;
  obIdx = 0;
  pintarOnboard();
  document.getElementById('onboard').classList.remove('hidden');
}
function pintarOnboard() {
  const p = OB_PASOS[obIdx];
  document.getElementById('ob-emoji').textContent = p.e;
  document.getElementById('ob-t').textContent = p.t;
  document.getElementById('ob-p').textContent = p.p;
  document.getElementById('ob-dots').innerHTML = OB_PASOS.map((_, i) => `<span class="${i === obIdx ? 'on' : ''}"></span>`).join('');
  document.getElementById('ob-next').textContent = obIdx === OB_PASOS.length - 1 ? '¡Empezar! 🚀' : 'Siguiente →';
}
function sigOnboard() {
  if (obIdx < OB_PASOS.length - 1) { obIdx++; pintarOnboard(); }
  else cerrarOnboard();
}
function cerrarOnboard() {
  S.ui.onboard = true;
  save();
  document.getElementById('onboard').classList.add('hidden');
}
// ===== Meta diaria configurable =====
function setGoal(n) {
  S.goal = n;
  save();
  renderProgreso();
  toastLogro('🎯 Meta diaria: ' + n + ' XP');
}

// ===== Import / Reset granular =====
function onImportFile(input) {
  const f = input.files && input.files[0];
  if (!f) return;
  if (!confirm('¿Sobrescribir tu progreso actual con el archivo?')) { input.value = ''; return; }
  importarProgreso(f, err => {
    input.value = '';
    if (err) alert('Archivo no válido 😅');
    else location.reload();
  });
}
function resetDato(tipo) {
  const nombres = { quiz: 'Quiz', escucha: 'Escucha', pinyin: 'Pinyin', tonos: 'Tonos', pronuncia: 'Pronuncia', escribe: 'Escribe', mem: 'Memorama', flash: 'Flashcards', palabras: 'stats de palabras', cultura: 'poemas leídos' };
  if (!confirm('¿Resetear ' + (nombres[tipo] || tipo) + '?')) return;
  if (tipo === 'palabras') resetPalabras();
  else if (tipo === 'cultura') { S.cultura = { leidos: [] }; save(); }
  else resetJuego(tipo);
  actualizarMenu(); renderProgreso();
  toastLogro('🧹 Reseteado: ' + (nombres[tipo] || tipo));
}

// ===== init =====
// PWA: registra el service worker solo en http(s), no en file://
if ('serviceWorker' in navigator && /^https?:$/.test(location.protocol)) {
  window.addEventListener('load', () => navigator.serviceWorker.register('sw.js').catch(() => {}));
}
// Atajo ?juego=quiz|escucha|flash... (usado por los shortcuts de la PWA)
(function juegoDesdeURL() {
  const j = new URLSearchParams(location.search).get('juego');
  if (j && document.getElementById('view-' + j)) {
    showView(j);
    if (j === 'quiz') nuevaPregunta();
    if (j === 'escucha') nuevaEscucha();
    if (j === 'pinyin') nuevaPinyin();
    if (j === 'tonos') { if (tModo === 'pares') nuevoPar(); else nuevoTono(); }
    if (j === 'pronuncia') nuevaPronuncia();
  }
})();
aplicarTema(S.ui.tema || 'light');
initCats();
initTonoBtns();
(function initSonido() {
  const b = document.getElementById('btn-sonido');
  b.textContent = S.ui.sonido ? '🔊' : '🔇';
  b.classList.toggle('off', !S.ui.sonido);
})();
registrarVisita();
actualizarMenu();
iniciarMemorama();
nuevoMazoFlash();
nuevoTono();
nuevaPronuncia();
nuevaEscribe();
prRec = initRec();
mostrarOnboard();
document.getElementById('f-vistas').textContent = S.games.flash.vistas;
