package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

data class StreakCelebrationData(
    val streak: Int,
    val isNewToday: Boolean,
    val bonusCoins: Int = 15,
    val bonusXp: Int = 25
)

data class DayStreakInfo(
    val dateStr: String,
    val dayOfMonth: Int,
    val dayOfWeek: String,
    val fullDayName: String,
    val isActive: Boolean,
    val xpEarned: Int,
    val isToday: Boolean
)

data class AppState(
    val xp: Int = 0,
    val racha: Int = 0,
    val lastDay: String? = null,
    val goal: Int = 50,
    val coins: Int = 0,
    val gastado: Int = 0,
    val freeze: Int = 0,
    val xp2: Int = 0,
    val pistas: Int = 0,
    val corazonesExtra: Int = 0,
    val sonido: Boolean = true,
    val isDarkTheme: Boolean = false,
    val filtroCat: String = "todas",
    val filtroHsk: Int = 0,
    val memPairs: Int = 8,
    val onboardDone: Boolean = false
)

class StoreRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nihao_lab_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadInitialState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // Transient event flows for Toasts & Confetti
    val toastEvents = MutableStateFlow<String?>(null)
    val confettiTrigger = MutableStateFlow(0)
    val streakCelebrationEvent = MutableStateFlow<StreakCelebrationData?>(null)

    val wordProgressMap = mutableMapOf<String, WordProgress>()
    val gameStatsMap = mutableMapOf<String, GameStats>()
    val memBestMap = mutableMapOf<Int, String>()
    var memWins: Int = 0
    var flashVistas: Int = 0
    val achievementsMap = mutableMapOf<String, String>()
    val caminoMap = mutableMapOf<String, UnitProgress>()
    val poemasLeidos = mutableSetOf<String>()
    val xpByDay = mutableMapOf<String, Int>()
    val itemsPurchased = mutableMapOf<String, Int>()
    val streakDays = mutableSetOf<String>()

    init {
        loadDetailedData()
        registrarVisita()
    }

    private fun loadInitialState(): AppState {
        return AppState(
            xp = prefs.getInt("xp", 0),
            racha = prefs.getInt("racha", 0),
            lastDay = prefs.getString("lastDay", null),
            goal = prefs.getInt("goal", 50),
            coins = prefs.getInt("coins", 0),
            gastado = prefs.getInt("gastado", 0),
            freeze = prefs.getInt("freeze", 0),
            xp2 = prefs.getInt("xp2", 0),
            pistas = prefs.getInt("pistas", 0),
            corazonesExtra = prefs.getInt("corazonesExtra", 0),
            sonido = prefs.getBoolean("sonido", true),
            isDarkTheme = prefs.getBoolean("isDarkTheme", false),
            filtroCat = prefs.getString("filtroCat", "todas") ?: "todas",
            filtroHsk = prefs.getInt("filtroHsk", 0),
            memPairs = prefs.getInt("memPairs", 8),
            onboardDone = prefs.getBoolean("onboardDone", false)
        )
    }

    private fun loadDetailedData() {
        // Words
        val wordsJson = prefs.getString("words_json", null)
        if (wordsJson != null) {
            try {
                val obj = JSONObject(wordsJson)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val wObj = obj.getJSONObject(k)
                    wordProgressMap[k] = WordProgress(
                        ok = wObj.optInt("ok", 0),
                        fail = wObj.optInt("fail", 0),
                        last = wObj.optLong("last", 0L),
                        box = wObj.optInt("box", 1),
                        due = wObj.optLong("due", 0L)
                    )
                }
            } catch (_: Exception) {}
        }

        // Game Stats
        val games = listOf("quiz", "escucha", "pinyin", "tonos", "pronuncia", "escribe")
        for (g in games) {
            gameStatsMap[g] = GameStats(
                best = prefs.getInt("game_${g}_best", 0),
                played = prefs.getInt("game_${g}_played", 0),
                ok = prefs.getInt("game_${g}_ok", 0)
            )
        }
        memWins = prefs.getInt("mem_wins", 0)
        flashVistas = prefs.getInt("flash_vistas", 0)
        val memBestJson = prefs.getString("mem_best_json", null)
        if (memBestJson != null) {
            try {
                val obj = JSONObject(memBestJson)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    memBestMap[k.toInt()] = obj.getString(k)
                }
            } catch (_: Exception) {}
        }

        // Achievements
        val achvJson = prefs.getString("achv_json", null)
        if (achvJson != null) {
            try {
                val obj = JSONObject(achvJson)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    achievementsMap[k] = obj.getString(k)
                }
            } catch (_: Exception) {}
        }

        // Camino
        val caminoJson = prefs.getString("camino_json", null)
        if (caminoJson != null) {
            try {
                val obj = JSONObject(caminoJson)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val cObj = obj.getJSONObject(k)
                    val done = cObj.optInt("done", 0)
                    val starsMap = mutableMapOf<Int, Int>()
                    val starsObj = cObj.optJSONObject("stars")
                    if (starsObj != null) {
                        val sKeys = starsObj.keys()
                        while (sKeys.hasNext()) {
                            val sk = sKeys.next()
                            starsMap[sk.toInt()] = starsObj.getInt(sk)
                        }
                    }
                    caminoMap[k] = UnitProgress(done, starsMap)
                }
            } catch (_: Exception) {}
        }

        // Cultura leídos
        val cultJson = prefs.getString("cultura_json", null)
        if (cultJson != null) {
            try {
                val arr = JSONArray(cultJson)
                for (i in 0 until arr.length()) {
                    poemasLeidos.add(arr.getString(i))
                }
            } catch (_: Exception) {}
        }

        // XP by Day
        val xpDayJson = prefs.getString("xp_day_json", null)
        if (xpDayJson != null) {
            try {
                val obj = JSONObject(xpDayJson)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    xpByDay[k] = obj.getInt(k)
                }
            } catch (_: Exception) {}
        }

        // Items
        val itemsJson = prefs.getString("items_json", null)
        if (itemsJson != null) {
            try {
                val obj = JSONObject(itemsJson)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    itemsPurchased[k] = obj.getInt(k)
                }
            } catch (_: Exception) {}
        }

        // Streak Days
        val streakDaysJson = prefs.getString("streak_days_json", null)
        if (streakDaysJson != null) {
            try {
                val arr = JSONArray(streakDaysJson)
                for (i in 0 until arr.length()) {
                    streakDays.add(arr.getString(i))
                }
            } catch (_: Exception) {}
        }
        val s = _state.value
        if (s.racha > 0) {
            val cal = Calendar.getInstance()
            for (i in 0 until s.racha.coerceAtMost(30)) {
                streakDays.add(diaLocal(cal.time))
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }
    }

    fun saveAll() {
        val s = _state.value
        val editor = prefs.edit()
            .putInt("xp", s.xp)
            .putInt("racha", s.racha)
            .putString("lastDay", s.lastDay)
            .putInt("goal", s.goal)
            .putInt("coins", s.coins)
            .putInt("gastado", s.gastado)
            .putInt("freeze", s.freeze)
            .putInt("xp2", s.xp2)
            .putInt("pistas", s.pistas)
            .putInt("corazonesExtra", s.corazonesExtra)
            .putBoolean("sonido", s.sonido)
            .putBoolean("isDarkTheme", s.isDarkTheme)
            .putString("filtroCat", s.filtroCat)
            .putInt("filtroHsk", s.filtroHsk)
            .putInt("memPairs", s.memPairs)
            .putBoolean("onboardDone", s.onboardDone)

        // Save words
        val wordsObj = JSONObject()
        for ((k, v) in wordProgressMap) {
            val o = JSONObject()
            o.put("ok", v.ok)
            o.put("fail", v.fail)
            o.put("last", v.last)
            o.put("box", v.box)
            o.put("due", v.due)
            wordsObj.put(k, o)
        }
        editor.putString("words_json", wordsObj.toString())

        // Save game stats
        for ((g, st) in gameStatsMap) {
            editor.putInt("game_${g}_best", st.best)
            editor.putInt("game_${g}_played", st.played)
            editor.putInt("game_${g}_ok", st.ok)
        }
        editor.putInt("mem_wins", memWins)
        editor.putInt("flash_vistas", flashVistas)

        val memObj = JSONObject()
        for ((k, v) in memBestMap) {
            memObj.put(k.toString(), v)
        }
        editor.putString("mem_best_json", memObj.toString())

        // Achievements
        val achvObj = JSONObject()
        for ((k, v) in achievementsMap) achvObj.put(k, v)
        editor.putString("achv_json", achvObj.toString())

        // Camino
        val caminoObj = JSONObject()
        for ((k, v) in caminoMap) {
            val c = JSONObject()
            c.put("done", v.doneLevel)
            val st = JSONObject()
            for ((lvl, stars) in v.stars) st.put(lvl.toString(), stars)
            c.put("stars", st)
            caminoObj.put(k, c)
        }
        editor.putString("camino_json", caminoObj.toString())

        // Cultura
        val cultArr = JSONArray()
        for (id in poemasLeidos) cultArr.put(id)
        editor.putString("cultura_json", cultArr.toString())

        // XP by day
        val xpObj = JSONObject()
        for ((k, v) in xpByDay) xpObj.put(k, v)
        editor.putString("xp_day_json", xpObj.toString())

        // Items
        val itemsObj = JSONObject()
        for ((k, v) in itemsPurchased) itemsObj.put(k, v)
        editor.putString("items_json", itemsObj.toString())

        // Streak Days
        val streakArr = JSONArray()
        for (d in streakDays) streakArr.put(d)
        editor.putString("streak_days_json", streakArr.toString())

        editor.apply()
    }

    private fun diaLocal(d: Date = Date()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(d)
    }

    fun checkDailyStreak() {
        registrarVisita()
    }

    private fun registrarVisita() {
        val hoy = diaLocal()
        val s = _state.value
        val isDifferentDay = s.lastDay != hoy
        if (isDifferentDay) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val ayer = diaLocal(cal.time)

            val newRacha = if (s.lastDay == ayer) {
                s.racha + 1
            } else if (s.freeze > 0) {
                _state.value = _state.value.copy(freeze = s.freeze - 1)
                toast("🧊 Tu protector salvó la racha")
                s.racha
            } else {
                1
            }
            streakDays.add(hoy)
            _state.value = _state.value.copy(racha = newRacha, lastDay = hoy)
            saveAll()
        } else {
            streakDays.add(hoy)
        }
        if (_state.value.racha >= 3) maybeAchv("racha3")
        if (_state.value.racha >= 7) maybeAchv("racha7")
        if (_state.value.racha >= 30) maybeAchv("racha30")

        val lastCelebrated = prefs.getString("lastCelebratedStreakDay", null)
        if (lastCelebrated != hoy) {
            val currentRacha = _state.value.racha.coerceAtLeast(1)
            streakCelebrationEvent.value = StreakCelebrationData(
                streak = currentRacha,
                isNewToday = true,
                bonusCoins = 15,
                bonusXp = 25
            )
            confettiTrigger.value += 1
        }
    }

    fun claimStreakBonus() {
        val event = streakCelebrationEvent.value ?: return
        val hoy = diaLocal()
        prefs.edit().putString("lastCelebratedStreakDay", hoy).apply()
        if (event.bonusCoins > 0) {
            val newCoins = _state.value.coins + event.bonusCoins
            _state.value = _state.value.copy(coins = newCoins)
        }
        if (event.bonusXp > 0) {
            val newXp = _state.value.xp + event.bonusXp
            _state.value = _state.value.copy(xp = newXp)
        }
        saveAll()
        toast("🎁 ¡Racha de ${event.streak} días celebrada! (+${event.bonusCoins}🪙, +${event.bonusXp} XP)")
        streakCelebrationEvent.value = null
    }

    fun dismissStreakCelebration() {
        val hoy = diaLocal()
        prefs.edit().putString("lastCelebratedStreakDay", hoy).apply()
        streakCelebrationEvent.value = null
    }

    fun triggerStreakCelebrationManual() {
        val currentRacha = _state.value.racha.coerceAtLeast(1)
        streakCelebrationEvent.value = StreakCelebrationData(
            streak = currentRacha,
            isNewToday = false,
            bonusCoins = 0,
            bonusXp = 0
        )
        confettiTrigger.value += 1
    }

    fun addXP(amount: Int) {
        var ganancia = amount
        var remainingXp2 = _state.value.xp2
        if (remainingXp2 > 0) {
            ganancia = amount * 2
            remainingXp2--
        }
        val hoy = diaLocal()
        val antes = xpByDay[hoy] ?: 0
        val nuevoDia = antes + ganancia
        xpByDay[hoy] = nuevoDia
        streakDays.add(hoy)

        val nuevoTotal = _state.value.xp + ganancia
        _state.value = _state.value.copy(xp = nuevoTotal, xp2 = remainingXp2)
        saveAll()

        toast("+$ganancia XP" + if (ganancia != amount) " ⚡x2" else "")

        if (nuevoTotal >= 10) maybeAchv("primer_paso")
        if (nuevoTotal >= 100) maybeAchv("cien")
        if (nuevoTotal >= 500) maybeAchv("quinientos")

        if (antes < _state.value.goal && nuevoDia >= _state.value.goal) {
            toast("🎯 ¡Meta diaria cumplida! (${_state.value.goal} XP)")
            confettiTrigger.value += 1
        }
    }

    fun getLast30DaysStreakHistory(): List<DayStreakInfo> {
        val list = mutableListOf<DayStreakInfo>()
        val hoy = diaLocal()
        val dayOfWeekLetters = mapOf(
            Calendar.MONDAY to "L",
            Calendar.TUESDAY to "M",
            Calendar.WEDNESDAY to "X",
            Calendar.THURSDAY to "J",
            Calendar.FRIDAY to "V",
            Calendar.SATURDAY to "S",
            Calendar.SUNDAY to "D"
        )
        val dayOfWeekFull = mapOf(
            Calendar.MONDAY to "Lunes",
            Calendar.TUESDAY to "Martes",
            Calendar.WEDNESDAY to "Miércoles",
            Calendar.THURSDAY to "Jueves",
            Calendar.FRIDAY to "Viernes",
            Calendar.SATURDAY to "Sábado",
            Calendar.SUNDAY to "Domingo"
        )

        // Past 29 days up to today (chronological order)
        for (i in 29 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val dStr = diaLocal(c.time)
            val dayOfMonth = c.get(Calendar.DAY_OF_MONTH)
            val dow = c.get(Calendar.DAY_OF_WEEK)
            val letter = dayOfWeekLetters[dow] ?: "D"
            val fullName = dayOfWeekFull[dow] ?: "Día"
            val xpEarned = xpByDay[dStr] ?: 0
            val isActive = streakDays.contains(dStr) || xpEarned > 0
            val isToday = dStr == hoy

            list.add(
                DayStreakInfo(
                    dateStr = dStr,
                    dayOfMonth = dayOfMonth,
                    dayOfWeek = letter,
                    fullDayName = fullName,
                    isActive = isActive,
                    xpEarned = xpEarned,
                    isToday = isToday
                )
            )
        }
        return list
    }

    fun addCoins(amount: Int) {
        val newCoins = _state.value.coins + amount
        _state.value = _state.value.copy(coins = newCoins)
        saveAll()
        toast("🪙 +$amount")
    }

    fun maybeAchv(id: String) {
        if (achievementsMap.containsKey(id)) return
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        achievementsMap[id] = sdf.format(Date())
        saveAll()
        val a = VocabData.LOGROS.find { it.id == id }
        if (a != null) {
            toast("🏆 Logro: ${a.emoji} ${a.nombre}")
            confettiTrigger.value += 1
        }
    }

    fun toast(msg: String) {
        toastEvents.value = msg
    }

    // SRS Leitner Logic
    fun cajaEntry(hanzi: String): WordProgress {
        return wordProgressMap.getOrPut(hanzi) {
            WordProgress(box = 1, due = 0L)
        }
    }

    fun trackWord(hanzi: String, ok: Boolean) {
        val w = cajaEntry(hanzi)
        if (ok) {
            w.ok++
            w.box = min(w.box + 1, 5)
        } else {
            w.fail++
            w.box = 1
        }
        w.last = System.currentTimeMillis()
        val days = VocabData.CAJAS_DIAS.getOrElse(w.box - 1) { 1 }
        w.due = w.last + days * 86400000L
        saveAll()
    }

    fun trackGame(game: String, ok: Boolean, score: Int = 0) {
        val st = gameStatsMap.getOrPut(game) { GameStats() }
        st.played++
        if (ok) st.ok++
        if (score > st.best) st.best = score
        saveAll()
    }

    fun wordStatus(hanzi: String): String {
        val e = wordProgressMap[hanzi] ?: return "nueva"
        if (e.fail > 0 && e.box <= 2) return "debil"
        if (e.box >= 4) return "dominada"
        return "aprendiendo"
    }

    fun triggerConfetti() {
        confettiTrigger.value += 1
    }

    fun getDebilWords(): List<Word> {
        return VocabData.VOCAB.filter { wordStatus(it.hanzi) == "debil" }
    }

    fun toggleMarcarDebil(hanzi: String) {
        val w = cajaEntry(hanzi)
        if (wordStatus(hanzi) == "debil") {
            w.fail = 0
            w.box = max(w.box, 3)
            toast("Palabra quitada de débiles")
        } else {
            w.fail = max(w.fail, 1)
            w.box = 1
            toast("Marcada como débil para práctica rápida")
        }
        saveAll()
    }

    fun seedDefaultDebilWords() {
        val seeds = listOf("谁", "哪", "喝", "读", "怎么")
        for (hz in seeds) {
            val w = cajaEntry(hz)
            w.fail = max(w.fail, 1)
            w.box = 1
        }
        saveAll()
        toast("Se agregaron palabras de ejemplo a débiles")
    }

    fun repasoDebido(): Int {
        val now = System.currentTimeMillis()
        return wordProgressMap.values.count { it.due <= now }
    }

    fun srsScore(hanzi: String): Double {
        val e = wordProgressMap[hanzi]
        val now = System.currentTimeMillis()
        if (e == null) return 2000.0 + Math.random() * 10.0
        if (e.fail > 0 || e.due <= now) {
            val overdue = min((now - e.due) / 86400000.0, 10.0)
            return 3000.0 + e.fail * 10.0 + overdue * 5.0 + Math.random() * 3.0
        }
        return -(e.due - now) / 86400000.0 + Math.random()
    }

    fun srsPick(list: List<Word>, n: Int): List<Word> {
        return list.sortedByDescending { srsScore(it.hanzi) }.take(n)
    }

    fun weakWords(list: List<Word>, n: Int = 10): List<Word> {
        return list.filter {
            val st = wordProgressMap[it.hanzi]
            st != null && st.fail > 0
        }.sortedByDescending {
            val st = wordProgressMap[it.hanzi]!!
            st.fail.toDouble() / (st.ok + st.fail).coerceAtLeast(1)
        }.take(n)
    }

    fun getFilteredPool(): List<Word> {
        val s = _state.value
        var p = if (s.filtroHsk != 0) VocabData.VOCAB.filter { it.hsk == s.filtroHsk } else VocabData.VOCAB
        if (s.filtroCat != "todas") {
            val c = p.filter { it.cat == s.filtroCat }
            if (c.isNotEmpty()) p = c
        }
        return p
    }

    fun setFiltros(hsk: Int, cat: String) {
        _state.value = _state.value.copy(filtroHsk = hsk, filtroCat = cat)
        saveAll()
    }

    fun toggleSonido() {
        _state.value = _state.value.copy(sonido = !_state.value.sonido)
        saveAll()
    }

    fun toggleTema() {
        _state.value = _state.value.copy(isDarkTheme = !_state.value.isDarkTheme)
        saveAll()
    }

    fun setGoal(n: Int) {
        _state.value = _state.value.copy(goal = n)
        saveAll()
        toast("🎯 Meta diaria: $n XP")
    }

    fun setMemPairs(n: Int) {
        _state.value = _state.value.copy(memPairs = n)
        saveAll()
    }

    fun completeOnboarding() {
        _state.value = _state.value.copy(onboardDone = true)
        saveAll()
    }

    fun comprar(id: String): Boolean {
        val item = VocabData.TIENDA.find { it.id == id } ?: return false
        val s = _state.value
        if (s.coins < item.costo) {
            toast("🪙 Te faltan monedas")
            return false
        }
        val newCoins = s.coins - item.costo
        val newGastado = s.gastado + item.costo
        itemsPurchased[id] = (itemsPurchased[id] ?: 0) + 1

        when (id) {
            "corazon" -> {
                val newExtra = min(s.corazonesExtra + 1, 2)
                _state.value = s.copy(coins = newCoins, gastado = newGastado, corazonesExtra = newExtra)
                toast("❤️ Guardado para tu próxima lección")
            }
            "freeze" -> {
                val newFreeze = s.freeze + 1
                _state.value = s.copy(coins = newCoins, gastado = newGastado, freeze = newFreeze)
                toast("🧊 Protector activo ($newFreeze en reserva)")
            }
            "xp2" -> {
                val newXp2 = s.xp2 + 10
                _state.value = s.copy(coins = newCoins, gastado = newGastado, xp2 = newXp2)
                toast("⚡ XP x2 durante 10 ganancias")
            }
            "pista" -> {
                val newPistas = s.pistas + 3
                _state.value = s.copy(coins = newCoins, gastado = newGastado, pistas = newPistas)
                toast("💡 +3 pistas pro ($newPistas disponibles)")
            }
        }
        saveAll()
        if (newGastado >= 100) maybeAchv("mercader")
        return true
    }

    fun recordMemoramaWin(pairs: Int, intentos: Int, seg: Int) {
        memWins++
        val best = memBestMap[pairs]
        val currBest = best?.split(" ")?.firstOrNull()?.toIntOrNull() ?: Int.MAX_VALUE
        if (intentos < currBest) {
            memBestMap[pairs] = "$intentos intentos · ${seg}s"
        }
        saveAll()
        maybeAchv("mem_win")
    }

    fun markPoemRead(id: String) {
        if (!poemasLeidos.contains(id)) {
            poemasLeidos.add(id)
            saveAll()
            if (poemasLeidos.size >= 3) maybeAchv("poeta")
        }
    }

    fun recordUnitLevelCompletion(unitId: String, level: Int, starsEarned: Int) {
        val up = caminoMap.getOrPut(unitId) { UnitProgress() }
        if (level > up.doneLevel) {
            up.doneLevel = level
        }
        val currentStars = up.stars.toMutableMap()
        val prevStars = currentStars[level] ?: 0
        if (starsEarned > prevStars) {
            currentStars[level] = starsEarned
            up.stars = currentStars
        }
        saveAll()
        maybeAchv("leccion1")
        if (up.doneLevel >= 3) maybeAchv("unidad1")
    }

    fun resetGame(game: String) {
        gameStatsMap[game] = GameStats()
        saveAll()
        toast("🧹 Reseteado: $game")
    }

    fun resetWords() {
        wordProgressMap.clear()
        saveAll()
        toast("🧹 Reseteado: palabras")
    }

    fun resetCultura() {
        poemasLeidos.clear()
        saveAll()
        toast("🧹 Reseteado: poemas")
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        _state.value = AppState()
        wordProgressMap.clear()
        gameStatsMap.clear()
        memBestMap.clear()
        memWins = 0
        flashVistas = 0
        achievementsMap.clear()
        caminoMap.clear()
        poemasLeidos.clear()
        xpByDay.clear()
        itemsPurchased.clear()
        streakDays.clear()
        toast("🗑️ Todo el progreso ha sido borrado")
    }

    companion object {
        @Volatile
        private var instance: StoreRepository? = null

        fun getInstance(context: Context): StoreRepository {
            return instance ?: synchronized(this) {
                instance ?: StoreRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
