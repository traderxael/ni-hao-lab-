# 🐼 NiHao Lab — Aprende Chino desde Español

App web para aprender chino mandarín (**HSK1 + HSK2**, 80 palabras) jugando. Sin build, sin backend: HTML + CSS + JS puros. Instalable como PWA y funciona **sin internet** tras la primera visita.

🌐 **Demo:** https://temporary-express-antimony-odu2zbz.vercel.app

## 🧠 Métodos de enseñanza aplicados

- **Repaso espaciado Leitner** (cajas 1-5 con fechas): cada palabra vuelve justo cuando estás por olvidarla
- **Radicales + mnemotecnias** para hànzì (妈 = 女 mujer + 马 sonido mǎ)
- **Pares mínimos de tonos** (mā/má/mǎ/mà) para entrenar el oído
- **Shadowing con reconocimiento de voz** (`SpeechRecognition zh-CN`) en 🎤 Pronuncia
- **Recuerdo activo** (testing effect) en quiz/escucha/pinyin y **frases en contexto** (input comprensible)
- **Cultura real**: poemas Tang y fiestas, porque la lengua vive en su cultura
- **🎯 Sesión diaria** de repaso mezclado (quiz+escucha+pinyin+tonos) con las palabras debidas
- **Onboarding** de 3 pasos y **meta diaria configurable** (20/50/100 XP)
- **🪙 Tienda pay-to-win sin dinero real**: monedas ganadas jugando → corazones extra, 🧊 protector de racha, ⚡ XP x2 y 💡 pistas pro

## 🎮 Juegos (6) + 🛤️ Ruta + 🏮 Cultura

| Juego | Qué entrena |
|---|---|
| 🃏 Memorama | Une hànzì ↔ español (niveles 6/8/12 parejas) |
| ✅ Quiz | Opción múltiple ES↔ZH, contrarreloj y rondas ×10 |
| 🔊 Escucha | Oído: elige el significado (normal/lento) |
| ⌨️ Pinyin | Escribe la pronunciación (teclado de tonos incluido) |
| 🔔 Tonos | El corazón del chino: distingue 1º–4º tono |
| 🎤 Pronuncia | Shadowing con reconocimiento de voz zh-CN |
| ✍️ Escribe | Traza el carácter: score por cobertura de tinta real |
| 📇 Flashcards | Repaso con SRS y modo débiles |
| 🛤️ Ruta | 8 unidades × 3 niveles estilo Duolingo, con corazones |
| 🏮 Cultura | 5 poemas Tang con audio + fiestas y costumbres |

## 🧠 Sistema de aprendizaje

- **SRS lite**: repite primero tus palabras falladas, luego las no vistas
- **XP, niveles y títulos** (🐣 Pollito → 🌟 Leyenda), racha diaria, meta 50 XP/día
- **8 logros**, estrellas por juego, dificultad adaptativa, modo 🎯 repaso
- Todo **100% local** (`localStorage` versionado) + exportar/importar JSON

## 📁 Estructura

```
index.html · styles.css · app.js · store.js · vocab.js · cultura.js
sw.js · manifest.webmanifest · vercel.json · icons/
```

## 🚀 Desplegar gratis

```bash
npx vercel --yes          # o conecta el repo en vercel.com → Add New
```

## 🔊 Voz

Usa la síntesis de voz del propio dispositivo (`zh-CN`), sin APIs ni claves.
