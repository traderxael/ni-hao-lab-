package com.example.data

import com.example.data.model.*

object VocabData {

    val CATEGORIAS_TEMATICAS = listOf(
        CategoryItem("todas", "🌐", "Todas", "Todo el vocabulario disponible"),
        CategoryItem("comida", "🍎", "Comida", "Alimentos, bebidas y mesa"),
        CategoryItem("familia", "👪", "Familia", "Padres, hermanos y amistades"),
        CategoryItem("tiempo", "⏰", "Tiempo", "Días, meses, horas y momentos"),
        CategoryItem("saludos", "👋", "Saludos", "Cortesía y modales sociales"),
        CategoryItem("numeros", "🔢", "Números", "Cifras, cantidades y orden"),
        CategoryItem("escuela", "🏫", "Escuela", "Colegio, profes y libros"),
        CategoryItem("lugares", "🏙️", "Lugares", "Ciudades, transporte y sitios"),
        CategoryItem("animales", "🐼", "Animales", "Fauna, pandas y mascotas"),
        CategoryItem("verbos", "⚡", "Verbos", "Acciones y actividades del día"),
        CategoryItem("adjetivos", "🌈", "Adjetivos", "Cualidades, aspectos y estados"),
        CategoryItem("ocio", "🎨", "Ocio", "Música, deportes y entretenimiento"),
        CategoryItem("objetos", "📦", "Objetos", "Cosas de la casa, ropa y compras"),
        CategoryItem("preguntas", "❓", "Preguntas", "¿Qué?, ¿dónde?, ¿cómo? y conectores")
    )

    val UNIDADES = listOf(
        UnitInfo("saludos", "👋", "Saludos y Cortesía", "Hola, gracias, adiós y modales", listOf("saludos", "pronombres")),
        UnitInfo("numeros", "🔢", "Números y Cifras", "Del 1 al 100 y cantidades", listOf("numeros")),
        UnitInfo("familia", "👪", "Familia y Personas", "Padres, hermanos y amigos", listOf("familia")),
        UnitInfo("comida", "🍎", "Comida y Bebida", "Platos, fruta, té y alimentos", listOf("comida")),
        UnitInfo("tiempo", "⏰", "Tiempo y Fechas", "Días, meses, horas y momentos", listOf("tiempo")),
        UnitInfo("escuela", "🏫", "Escuela y Estudio", "Cole, profesores y libros", listOf("escuela")),
        UnitInfo("lugares", "🏙️", "Lugares y Viajes", "Ciudades, transporte y destinos", listOf("lugares")),
        UnitInfo("animales", "🐼", "Animales y Fauna", "Pandas, mascotas y naturaleza", listOf("animales")),
        UnitInfo("verbos", "⚡", "Acciones y Verbos", "Acciones diarias y actividades", listOf("verbos")),
        UnitInfo("adjetivos", "🌈", "Adjetivos y Colores", "Estados, formas y descripciones", listOf("adjetivos")),
        UnitInfo("ocio", "🎨", "Ocio y Deportes", "Películas, música y entretenimiento", listOf("ocio")),
        UnitInfo("objetos", "📦", "Objetos y Cosas", "Compras, ropa y cosas de casa", listOf("objetos")),
        UnitInfo("preguntas", "❓", "Preguntas y Conexión", "Partículas de pregunta y conectores", listOf("preguntas", "general")),
        UnitInfo("hsk2", "🚀", "HSK2 Plus", "El gran salto al nivel 2", hsk = 2)
    )

    val NIVEL_LEN = listOf(5, 7, 9)

    val PARES_TONO = listOf(
        TonePair("ma", listOf(
            ToneOption("妈", "mā", 1, "mamá"),
            ToneOption("麻", "má", 2, "cáñamo"),
            ToneOption("马", "mǎ", 3, "caballo"),
            ToneOption("骂", "mà", 4, "regañar")
        )),
        TonePair("shi", listOf(
            ToneOption("诗", "shī", 1, "poema"),
            ToneOption("十", "shí", 2, "diez"),
            ToneOption("史", "shǐ", 3, "historia"),
            ToneOption("是", "shì", 4, "ser")
        )),
        TonePair("yi", listOf(
            ToneOption("衣", "yī", 1, "ropa"),
            ToneOption("姨", "yí", 2, "tía"),
            ToneOption("椅", "yǐ", 3, "silla"),
            ToneOption("意", "yì", 4, "significado")
        )),
        TonePair("ba", listOf(
            ToneOption("八", "bā", 1, "ocho"),
            ToneOption("拔", "bá", 2, "arrancar"),
            ToneOption("把", "bǎ", 3, "agarrar"),
            ToneOption("爸", "bà", 4, "papá")
        )),
        TonePair("tu", listOf(
            ToneOption("突", "tū", 1, "repentino"),
            ToneOption("图", "tú", 2, "mapa"),
            ToneOption("土", "tǔ", 3, "tierra"),
            ToneOption("兔", "tù", 4, "conejo")
        ))
    )

    val VOCAB: List<Word> = listOf(
        // HSK 1 (50)
        Word("一", "yī", "uno", 1, "numeros", rad = "一", mnemo = "un solo trazo horizontal: el 1"),
        Word("二", "èr", "dos", 1, "numeros", rad = "一", mnemo = "dos trazos: el 2"),
        Word("三", "sān", "tres", 1, "numeros", rad = "一", mnemo = "tres trazos: el 3"),
        Word("四", "sì", "cuatro", 1, "numeros", rad = "囗", mnemo = "un recinto (囗) con patas dentro"),
        Word("五", "wǔ", "cinco", 1, "numeros", rad = "一", mnemo = "trazos cruzados entre dos líneas"),
        Word("六", "liù", "seis", 1, "numeros", rad = "八", mnemo = "una tapa sobre 八 (ocho)"),
        Word("七", "qī", "siete", 1, "numeros", rad = "一", mnemo = "una cruz torcida"),
        Word("八", "bā", "ocho", 1, "numeros", rad = "八", mnemo = "dos trazos que se separan"),
        Word("九", "jiǔ", "nueve", 1, "numeros", rad = "丿", mnemo = "un codo con gancho"),
        Word("十", "shí", "diez", 1, "numeros", rad = "十", mnemo = "una cruz completa: 10"),
        Word("零", "líng", "cero", 1, "numeros", rad = "雨", mnemo = "lluvia (雨) + 令"),
        Word("百", "bǎi", "cien", 1, "numeros", rad = "白", mnemo = "un trazo sobre blanco (白)"),
        Word("两", "liǎng", "dos (cantidad)", 1, "numeros", rad = "一", mnemo = "un par o dos unidades"),
        Word("你好", "nǐ hǎo", "hola", 1, "saludos", fr = ExampleSentence("你好！我叫小明。", "Nǐ hǎo! Wǒ jiào Xiǎomíng.", "¡Hola! Me llamo Xiaoming.")),
        Word("谢谢", "xièxie", "gracias", 1, "saludos", fr = ExampleSentence("谢谢你的帮助。", "Xièxie nǐ de bāngzhù.", "Gracias por tu ayuda.")),
        Word("对不起", "duìbuqǐ", "perdón", 1, "saludos", fr = ExampleSentence("对不起，我迟到了。", "Duìbuqǐ, wǒ chídào le.", "Perdón, llegué tarde.")),
        Word("再见", "zàijiàn", "adiós", 1, "saludos", fr = ExampleSentence("明天见，再见！", "Míngtiān jiàn, zàijiàn!", "¡Hasta mañana, adiós!")),
        Word("请", "qǐng", "por favor", 1, "saludos", rad = "讠", mnemo = "palabra (讠) + azul: invitar"),
        Word("不客气", "bú kèqi", "de nada", 1, "saludos", fr = ExampleSentence("不客气，慢慢吃。", "Bú kèqi, mànmàn chī.", "De nada, ¡buen provecho!")),
        Word("没关系", "méi guānxi", "no pasa nada", 1, "saludos", fr = ExampleSentence("没关系，下次注意。", "Méi guānxi, xiàcì zhùyì.", "No pasa nada, fíjate la próxima vez.")),
        Word("早上好", "zǎoshang hǎo", "buenos días", 1, "saludos"),
        Word("我", "wǒ", "yo", 1, "pronombres", rad = "戈", mnemo = "una mano que sostiene un arma (戈): yo mismo", fr = ExampleSentence("我是学生。", "Wǒ shì xuésheng.", "Soy estudiante.")),
        Word("你", "nǐ", "tú", 1, "pronombres", rad = "亻", mnemo = "persona (亻) + 尔: tú", fr = ExampleSentence("你叫什么名字？", "Nǐ jiào shénme míngzi?", "¿Cómo te llamas?")),
        Word("他", "tā", "él", 1, "pronombres", rad = "亻", mnemo = "persona (亻) + 也: él", fr = ExampleSentence("他是我朋友。", "Tā shì wǒ péngyou.", "Él es mi amigo.")),
        Word("她", "tā", "ella", 1, "pronombres", rad = "女", mnemo = "mujer (女) + 也: ella", fr = ExampleSentence("她是我妈妈。", "Tā shì wǒ māma.", "Ella es mi mamá.")),
        Word("我们", "wǒmen", "nosotros", 1, "pronombres", fr = ExampleSentence("我们是学生。", "Wǒmen shì xuésheng.", "Somos estudiantes.")),
        Word("你们", "nǐmen", "vosotros", 1, "pronombres"),
        Word("他们", "tāmen", "ellos", 1, "pronombres"),
        Word("爸爸", "bàba", "papá", 1, "familia", rad = "父", mnemo = "padre (父) + 巴 (sonido bā)", fr = ExampleSentence("爸爸工作忙。", "Bàba gōngzuò máng.", "Papá está ocupado con el trabajo.")),
        Word("妈妈", "māma", "mamá", 1, "familia", rad = "女", mnemo = "mujer (女) + 马 (sonido mǎ)", fr = ExampleSentence("妈妈喝茶。", "Māma hē chá.", "Mamá bebe té.")),
        Word("儿子", "érzi", "hijo", 1, "familia", rad = "子", mnemo = "子 es niño/hijo"),
        Word("女儿", "nǚér", "hija", 1, "familia", rad = "女", mnemo = "mujer (女) + 儿: hija"),
        Word("哥哥", "gēge", "hermano mayor", 1, "familia", rad = "口", mnemo = "dos pisos (可可) para el mayor"),
        Word("姐姐", "jiějie", "hermana mayor", 1, "familia", rad = "女", mnemo = "mujer (女) + 且"),
        Word("弟弟", "dìdi", "hermano menor", 1, "familia", rad = "弓", mnemo = "arco enrollado con hilo"),
        Word("妹妹", "mèimei", "hermana menor", 1, "familia", rad = "女", mnemo = "mujer (女) + 未 (joven)"),
        Word("朋友", "péngyou", "amigo", 1, "familia", rad = "月", mnemo = "dos lunas (月) juntas: compañeros", fr = ExampleSentence("我有三个朋友。", "Wǒ yǒu sān ge péngyou.", "Tengo tres amigos.")),
        Word("水", "shuǐ", "agua", 1, "comida", rad = "水", mnemo = "un chorro con gotas: agua", fr = ExampleSentence("我想喝水。", "Wǒ xiǎng hē shuǐ.", "Quiero beber agua.")),
        Word("茶", "chá", "té", 1, "comida", rad = "艹", mnemo = "hierba (艹) sobre un árbol: té", fr = ExampleSentence("中国茶很有名。", "Zhōngguó chá hěn yǒumíng.", "El té chino es famoso.")),
        Word("米饭", "mǐfàn", "arroz cocido", 1, "comida", rad = "米", mnemo = "米 es grano de arroz"),
        Word("苹果", "píngguǒ", "manzana", 1, "comida", rad = "艹", mnemo = "hierba (艹) + 果 (fruta)"),
        Word("鸡蛋", "jīdàn", "huevo", 1, "comida", rad = "虫", mnemo = "gallina (鸡) + huevo (蛋)"),
        Word("牛奶", "niúnǎi", "leche", 1, "comida", rad = "牛", mnemo = "vaca (牛) + leche (奶)"),
        Word("菜", "cài", "plato / verdura", 1, "comida", rad = "艹", mnemo = "hierba (艹) + recoger (采)"),
        Word("水果", "shuǐguǒ", "fruta", 1, "comida", rad = "水", mnemo = "agua (水) + fruto (果)"),
        Word("学校", "xuéxiào", "escuela", 1, "escuela", rad = "子", mnemo = "aprender (学) + escuela (校)", fr = ExampleSentence("学校很大。", "Xuéxiào hěn dà.", "La escuela es grande.")),
        Word("老师", "lǎoshī", "profesor", 1, "escuela", rad = "老", mnemo = "viejo (老) + maestro (师): el profe", fr = ExampleSentence("老师很好。", "Lǎoshī hěn hǎo.", "El profe es muy bueno.")),
        Word("学生", "xuésheng", "estudiante", 1, "escuela", rad = "子", mnemo = "aprender (学) + vida (生)"),
        Word("书", "shū", "libro", 1, "escuela", rad = "乛", mnemo = "forma de hojas de un libro"),
        Word("汉语", "Hànyǔ", "idioma chino", 1, "escuela", rad = "讠", mnemo = "río Han (汉) + lengua (语)"),
        Word("中国", "Zhōngguó", "China", 1, "lugares", rad = "囗", mnemo = "recinto (囗) con jade (玉) dentro: el país del medio", fr = ExampleSentence("我去中国。", "Wǒ qù Zhōngguó.", "Voy a China.")),
        Word("西班牙", "Xībānyá", "España", 1, "lugares"),
        Word("北京", "Běijīng", "Pekín", 1, "lugares", rad = "亠", mnemo = "norte (北) + capital (京)"),
        Word("家", "jiā", "casa / hogar", 1, "lugares", rad = "宀", mnemo = "techo (宀) con cerdo (豕)"),
        Word("是", "shì", "ser", 1, "verbos", rad = "日", mnemo = "sol (日) + correcto: ser/afirmar"),
        Word("有", "yǒu", "tener", 1, "verbos", rad = "月", mnemo = "mano (𠂇) sobre carne (月): poseer"),
        Word("爱", "ài", "amar", 1, "verbos", rad = "爫", mnemo = "simplificado: amar sin corazón", fr = ExampleSentence("我爱你。", "Wǒ ài nǐ.", "Te quiero.")),
        Word("吃", "chī", "comer", 1, "verbos", rad = "口", mnemo = "boca (口) que pide (乞): comer", fr = ExampleSentence("我吃米饭。", "Wǒ chī mǐfàn.", "Como arroz.")),
        Word("喝", "hē", "beber", 1, "verbos", rad = "口", mnemo = "boca (口) + 曷: beber", fr = ExampleSentence("他喝牛奶。", "Tā hē niúnǎi.", "Él bebe leche.")),
        Word("好", "hǎo", "bueno", 1, "adjetivos", rad = "女", mnemo = "mujer (女) + niño (子) = bueno"),
        Word("大", "dà", "grande", 1, "adjetivos", rad = "大", mnemo = "persona (人) con brazos abiertos: grande"),
        Word("小", "xiǎo", "pequeño", 1, "adjetivos", rad = "小", mnemo = "algo partido en dos mitades: pequeño"),
        Word("冷", "lěng", "frío", 1, "adjetivos", rad = "冫", mnemo = "hielo (冫) + orden (令)"),
        Word("热", "rè", "calor / caliente", 1, "adjetivos", rad = "灬", mnemo = "fuego abajo (灬)"),
        Word("多", "duō", "mucho", 1, "adjetivos", rad = "夕", mnemo = "dos lunas (夕) apiladas"),
        Word("少", "shǎo", "poco", 1, "adjetivos", rad = "小", mnemo = "pequeño (小) con trazo extra"),
        Word("今天", "jīntiān", "hoy", 1, "tiempo", rad = "人", mnemo = "ahora (今) + día (天)"),
        Word("明天", "míngtiān", "mañana", 1, "tiempo", rad = "日", mnemo = "sol (日) + luna (月) = luminoso: mañana"),
        Word("昨天", "zuótiān", "ayer", 1, "tiempo", rad = "日", mnemo = "sol (日) + 乍 (hace un momento)"),
        Word("年", "nián", "año", 1, "tiempo", rad = "干", mnemo = "ciclo de las estaciones"),
        Word("月", "yuè", "mes / luna", 1, "tiempo", rad = "月", mnemo = "forma de luna creciente"),
        Word("日", "rì", "día / sol", 1, "tiempo", rad = "日", mnemo = "forma del disco solar"),
        Word("星期", "xīngqī", "semana", 1, "tiempo", rad = "日", mnemo = "estrellas (星) + período (期)"),
        Word("点", "diǎn", "hora / en punto", 1, "tiempo", rad = "灬", mnemo = "marca o punto en el reloj"),
        Word("什么", "shénme", "qué", 1, "preguntas", rad = "亻", mnemo = "persona (亻) + 十 + 么"),
        Word("谁", "shéi", "quién", 1, "preguntas", rad = "讠", mnemo = "palabra (讠) + 隹 (pájaro): quién"),
        Word("这", "zhè", "esto", 1, "preguntas", rad = "辶", mnemo = "caminar (辶) + 文: esto"),
        Word("那", "nà", "eso", 1, "preguntas", rad = "阝", mnemo = "阝 + 冉: eso (lejano)"),
        Word("哪儿", "nǎr", "dónde", 1, "preguntas", rad = "口"),
        Word("怎么", "zěnme", "cómo", 1, "preguntas", rad = "心"),
        Word("多少", "duōshao", "cuánto", 1, "preguntas", rad = "夕"),
        Word("几", "jǐ", "cuántos", 1, "preguntas", rad = "几"),
        Word("人", "rén", "persona", 1, "general", rad = "人", mnemo = "una persona de perfil caminando"),

        // HSK 2 (100)
        Word("学习", "xuéxí", "estudiar", 2, "verbos", rad = "子", mnemo = "aprender (学) + practicar (习)", fr = ExampleSentence("我学习中文。", "Wǒ xuéxí Zhōngwén.", "Estudio chino.")),
        Word("工作", "gōngzuò", "trabajar", 2, "verbos", fr = ExampleSentence("她在医院工作。", "Tā zài yīyuàn gōngzuò.", "Ella trabaja en el hospital.")),
        Word("考试", "kǎoshì", "examen", 2, "escuela", rad = "耂", mnemo = "examinar (考) + probar (试)"),
        Word("慢", "màn", "lento", 2, "adjetivos", rad = "忄", mnemo = "corazón (忄) + 曼: lento"),
        Word("快", "kuài", "rápido", 2, "adjetivos", rad = "忄", mnemo = "corazón (忄) + 夬: rápido"),
        Word("漂亮", "piàoliang", "bonito", 2, "adjetivos", rad = "氵", mnemo = "agua (氵) + 亮 (brillante): hermoso"),
        Word("高兴", "gāoxìng", "contento", 2, "adjetivos", fr = ExampleSentence("我很高兴。", "Wǒ hěn gāoxìng.", "Estoy muy contento.")),
        Word("便宜", "piányi", "barato", 2, "adjetivos", rad = "亻", mnemo = "persona (亻) + 更: barato"),
        Word("贵", "guì", "caro", 2, "adjetivos", rad = "贝", mnemo = "贝 (concha=dinero) abajo: caro"),
        Word("医院", "yīyuàn", "hospital", 2, "lugares", rad = "疒", mnemo = "enfermedad (疒) + patio (院)"),
        Word("火车", "huǒchē", "tren", 2, "lugares", rad = "火", mnemo = "fuego (火) + carro (车): tren"),
        Word("飞机", "fēijī", "avión", 2, "lugares", rad = "飞", mnemo = "volar (飞) + máquina (机): avión"),
        Word("运动", "yùndòng", "deporte", 2, "ocio", rad = "辶", mnemo = "moverse (运) + fuerza (动)"),
        Word("电影", "diànyǐng", "película", 2, "ocio", rad = "雨", mnemo = "electricidad (电) + sombra (影)", fr = ExampleSentence("我们看电影吧。", "Wǒmen kàn diànyǐng ba.", "Veamos una película.")),
        Word("音乐", "yīnyuè", "música", 2, "ocio", rad = "音", mnemo = "sonido (音) + 乐 (alegre): música"),
        Word("电脑", "diànnǎo", "ordenador", 2, "objetos", rad = "电", mnemo = "electricidad (电) + cerebro (脑)"),
        Word("手机", "shǒujī", "móvil", 2, "objetos", rad = "手", mnemo = "mano (手) + máquina (机): móvil", fr = ExampleSentence("我手机没电了。", "Wǒ shǒujī méi diàn le.", "Mi móvil se quedó sin batería.")),
        Word("衣服", "yīfu", "ropa", 2, "objetos", rad = "衣", mnemo = "衣 es ropa; 服 = vestir"),
        Word("房间", "fángjiān", "habitación", 2, "lugares", rad = "户", mnemo = "puerta (户) + 间 (espacio)"),
        Word("机场", "jīchǎng", "aeropuerto", 2, "lugares", rad = "木", mnemo = "máquina (机) + campo (场)"),
        Word("帮助", "bāngzhù", "ayudar", 2, "verbos", fr = ExampleSentence("我帮助你。", "Wǒ bāngzhù nǐ.", "Te ayudo.")),
        Word("准备", "zhǔnbèi", "preparar", 2, "verbos", rad = "冫", mnemo = "冫 + 隹 + 备: preparar"),
        Word("觉得", "juéde", "opinar", 2, "verbos", rad = "见", mnemo = "sentir (觉) + 得"),
        Word("知道", "zhīdào", "saber", 2, "verbos", rad = "矢", mnemo = "flecha (矢) + camino (道): saber", fr = ExampleSentence("我知道了。", "Wǒ zhīdào le.", "Entendido, ya lo sé.")),
        Word("希望", "xīwàng", "desear", 2, "verbos", fr = ExampleSentence("我希望去中国。", "Wǒ xīwàng qù Zhōngguó.", "Espero ir a China.")),
        Word("笑", "xiào", "reír", 2, "verbos", rad = "竹", mnemo = "bambú (竹) + 夭: reír"),
        Word("哭", "kū", "llorar", 2, "verbos", rad = "口", mnemo = "dos bocas (口口) + lágrimas: llorar"),
        Word("远", "yuǎn", "lejos", 2, "adjetivos", rad = "辶", mnemo = "caminar (辶) + 元: lejos"),
        Word("近", "jìn", "cerca", 2, "adjetivos", rad = "辶", mnemo = "caminar (辶) + 斤: cerca"),
        Word("忙", "máng", "ocupado", 2, "adjetivos", rad = "忄", mnemo = "corazón (忄) + 亡 (muerto): ocupadísimo"),
        Word("已经", "yǐjīng", "ya", 2, "tiempo"),
        Word("因为", "yīnwèi", "porque", 2, "general"),
        Word("所以", "suǒyǐ", "por lo tanto", 2, "general"),
        Word("但是", "dànshì", "pero", 2, "general"),
        Word("如果", "rúguǒ", "si (condición)", 2, "general"),
        Word("再", "zài", "otra vez", 2, "tiempo"),
        Word("一直", "yìzhí", "todo el tiempo", 2, "tiempo"),
        Word("一起", "yìqǐ", "juntos", 2, "general"),
        Word("自己", "zìjǐ", "uno mismo", 2, "pronombres"),
        Word("别人", "biérén", "otra persona", 2, "pronombres"),
        Word("大家", "dàjiā", "todos", 2, "pronombres"),
        Word("东西", "dōngxi", "cosa", 2, "objetos"),
        Word("地方", "dìfang", "lugar", 2, "lugares"),
        Word("时间", "shíjiān", "tiempo", 2, "tiempo"),
        Word("时候", "shíhou", "momento", 2, "tiempo"),
        Word("现在", "xiànzài", "ahora", 2, "tiempo"),
        Word("昨天", "zuótiān", "ayer", 2, "tiempo"),
        Word("早上", "zǎoshang", "por la mañana", 2, "tiempo"),
        Word("晚上", "wǎnshang", "por la noche", 2, "tiempo"),
        Word("中午", "zhōngwǔ", "mediodía", 2, "tiempo"),
        Word("分钟", "fēnzhōng", "minuto", 2, "tiempo"),
        Word("钱", "qián", "dinero", 2, "objetos"),
        Word("买", "mǎi", "comprar", 2, "verbos"),
        Word("卖", "mài", "vender", 2, "verbos"),
        Word("送", "sòng", "regalar", 2, "verbos"),
        Word("穿", "chuān", "llevar puesto", 2, "verbos"),
        Word("红", "hóng", "rojo", 2, "adjetivos"),
        Word("白", "bái", "blanco", 2, "adjetivos"),
        Word("黑", "hēi", "negro", 2, "adjetivos"),
        Word("走", "zǒu", "caminar", 2, "verbos"),
        Word("跑", "pǎo", "correr", 2, "verbos"),
        Word("游泳", "yóuyǒng", "nadar", 2, "ocio"),
        Word("唱歌", "chànggē", "cantar", 2, "ocio"),
        Word("跳舞", "tiàowǔ", "bailar", 2, "ocio"),
        Word("玩", "wán", "jugar / divertirse", 2, "ocio"),
        Word("休息", "xiūxi", "descansar", 2, "verbos"),
        Word("睡觉", "shuìjiào", "dormir", 2, "verbos"),
        Word("起床", "qǐchuáng", "levantarse", 2, "verbos"),
        Word("回", "huí", "volver", 2, "verbos"),
        Word("进", "jìn", "entrar", 2, "verbos"),
        Word("开", "kāi", "abrir", 2, "verbos"),
        Word("关", "guān", "cerrar", 2, "verbos"),
        Word("等", "děng", "esperar", 2, "verbos"),
        Word("找", "zhǎo", "buscar", 2, "verbos"),
        Word("看", "kàn", "ver / mirar", 2, "verbos"),
        Word("听", "tīng", "escuchar", 2, "verbos"),
        Word("说", "shuō", "hablar / decir", 2, "verbos"),
        Word("读", "dú", "leer en voz alta", 2, "verbos"),
        Word("写", "xiě", "escribir", 2, "verbos"),
        Word("想", "xiǎng", "pensar / querer", 2, "verbos"),
        Word("喜欢", "xǐhuan", "gustar", 2, "verbos"),
        Word("认识", "rènshi", "conocer", 2, "verbos"),
        Word("会", "huì", "saber hacer", 2, "verbos"),
        Word("能", "néng", "poder", 2, "verbos"),
        Word("可以", "kěyǐ", "poder (permiso)", 2, "verbos"),
        Word("打篮球", "dǎ lánqiú", "jugar al baloncesto", 2, "ocio"),
        Word("足球", "zúqiú", "fútbol", 2, "ocio"),
        Word("自行车", "zìxíngchē", "bicicleta", 2, "objetos"),
        Word("汽车", "qìchē", "coche", 2, "objetos"),
        Word("银行", "yínháng", "banco", 2, "lugares"),
        Word("商店", "shāngdiàn", "tienda", 2, "lugares"),
        Word("饭店", "fàndiàn", "restaurante", 2, "lugares"),
        Word("公园", "gōngyuán", "parque", 2, "lugares"),
        Word("图书馆", "túshūguǎn", "biblioteca", 2, "lugares"),
        Word("路", "lù", "calle / camino", 2, "lugares", rad = "足"),
        Word("车站", "chēzhàn", "estación / parada", 2, "lugares", rad = "车"),
        Word("教室", "jiàoshì", "aula", 2, "escuela"),
        Word("问题", "wèntí", "pregunta / problema", 2, "escuela"),
        Word("课", "kè", "clase / lección", 2, "escuela", rad = "讠"),
        Word("笔", "bǐ", "bolígrafo / lápiz", 2, "escuela", rad = "竹"),
        Word("同学", "tóngxué", "compañero", 2, "escuela", rad = "口"),
        Word("生日", "shēngrì", "cumpleaños", 2, "general"),
        Word("礼物", "lǐwù", "regalo", 2, "objetos"),
        Word("桌子", "zhuōzi", "mesa", 2, "objetos", rad = "木"),
        Word("椅子", "yǐzi", "silla", 2, "objetos", rad = "木"),
        Word("杯子", "bēizi", "taza / vaso", 2, "objetos", rad = "木"),
        Word("手表", "shǒubiǎo", "reloj", 2, "objetos", rad = "手"),
        Word("面条", "miàntiáo", "fideos", 2, "comida", rad = "面"),
        Word("包子", "bāozi", "bollo al vapor", 2, "comida", rad = "勹"),
        Word("肉", "ròu", "carne", 2, "comida", rad = "肉"),
        Word("咖啡", "kāfēi", "café", 2, "comida", rad = "口"),
        Word("面包", "miànbāo", "pan", 2, "comida", rad = "面"),
        Word("孩子", "háizi", "hijo / niño", 2, "familia", rad = "子"),
        Word("丈夫", "zhàngfu", "esposo", 2, "familia", rad = "夫"),
        Word("妻子", "qīzi", "esposa", 2, "familia", rad = "女"),
        Word("爷爷", "yéye", "abuelo", 2, "familia", rad = "父"),
        Word("奶奶", "nǎinai", "abuela", 2, "familia", rad = "女"),
        Word("猫", "māo", "gato", 2, "animales", rad = "犭"),
        Word("狗", "gǒu", "perro", 2, "animales", rad = "犭"),
        Word("鸟", "niǎo", "pájaro", 2, "animales", rad = "鸟"),
        Word("马", "mǎ", "caballo", 2, "animales", rad = "马"),
        Word("羊", "yáng", "oveja / cabra", 2, "animales", rad = "羊"),
        Word("牛", "niú", "vaca / toro", 2, "animales", rad = "牛"),
        Word("熊猫", "xióngmāo", "oso panda", 2, "animales", rad = "灬"),
        Word("小时", "xiǎoshí", "hora (duración)", 2, "tiempo", rad = "小"),
        Word("下午", "xiàwǔ", "por la tarde", 2, "tiempo", rad = "一"),
        Word("去年", "qùnián", "el año pasado", 2, "tiempo", rad = "土"),
        Word("新", "xīn", "nuevo", 2, "adjetivos", rad = "斤"),
        Word("欢迎", "huānyíng", "bienvenido", 2, "saludos", rad = "辶"),
        Word("千", "qiān", "mil", 2, "numeros", rad = "十"),
        Word("第一", "dì-yī", "primero", 2, "numeros", rad = "竹"),
        Word("旅游", "lǚyóu", "viajar / turismo", 2, "ocio", rad = "方"),
        Word("游戏", "yóuxì", "juego", 2, "ocio", rad = "氵"),
        Word("为什么", "wèishénme", "por qué", 2, "preguntas", rad = "丶")
    )

    val POEMAS = listOf(
        Poem(
            id = "jingyesi",
            emoji = "🌙",
            titulo = "静夜思",
            pinyinT = "Jìng Yè Sī",
            tituloEs = "Pensando en la noche",
            poeta = "李白",
            poetaP = "Lǐ Bái",
            dinastia = "Dinastía Tang (siglo VIII)",
            nota = "El poema que todo niño chino memoriza. Habla de la nostalgia por el hogar.",
            lineas = listOf(
                PoemLine("床前明月光", "chuáng qián míng yuè guāng", "Ante mi cama, la luz de la luna"),
                PoemLine("疑是地上霜", "yí shì dì shàng shuāng", "parece escarcha sobre el suelo"),
                PoemLine("举头望明月", "jǔ tóu wàng míng yuè", "Levanto la cabeza y miro la luna"),
                PoemLine("低头思故乡", "dī tóu sī gù xiāng", "la bajo, y pienso en mi tierra")
            )
        ),
        Poem(
            id = "chunxiao",
            emoji = "🌸",
            titulo = "春晓",
            pinyinT = "Chūn Xiǎo",
            tituloEs = "Amanecer de primavera",
            poeta = "孟浩然",
            poetaP = "Mèng Hàorán",
            dinastia = "Dinastía Tang",
            nota = "Una mañana de primavera entre pájaros y flores caídas.",
            lineas = listOf(
                PoemLine("春眠不觉晓", "chūn mián bù jué xiǎo", "Dormido en primavera, no sentí el amanecer"),
                PoemLine("处处闻啼鸟", "chù chù wén tí niǎo", "por todas partes se oyen pájaros"),
                PoemLine("夜来风雨声", "yè lái fēng yǔ shēng", "anoche hubo viento y lluvia"),
                PoemLine("花落知多少", "huā luò zhī duō shǎo", "¿cuántas flores habrán caído?")
            )
        ),
        Poem(
            id = "dengguanquelou",
            emoji = "🏔️",
            titulo = "登鹳雀楼",
            pinyinT = "Dēng Guànquè Lóu",
            tituloEs = "Subiendo la torre del Cigüeñón",
            poeta = "王之涣",
            poetaP = "Wáng Zhīhuàn",
            dinastia = "Dinastía Tang",
            nota = "Su moraleja se usa como refrán: para ver más lejos, hay que subir más alto.",
            lineas = listOf(
                PoemLine("白日依山尽", "bái rì yī shān jìn", "El sol se pone tras las montañas"),
                PoemLine("黄河入海流", "huáng hé rù hǎi liú", "el Río Amarillo fluye hacia el mar"),
                PoemLine("欲穷千里目", "yù qióng qiān lǐ mù", "para ver mil lis más lejos"),
                PoemLine("更上一层楼", "gèng shàng yì céng lóu", "sube un piso más")
            )
        ),
        Poem(
            id = "xiangsi",
            emoji = "❤️",
            titulo = "相思",
            pinyinT = "Xiāngsī",
            tituloEs = "Añoranza",
            poeta = "王维",
            poetaP = "Wáng Wéi",
            dinastia = "Dinastía Tang",
            nota = "Las judías rojas (红豆) son en China símbolo del amor y la añoranza.",
            lineas = listOf(
                PoemLine("红豆生南国", "hóng dòu shēng nán guó", "Las judías rojas crecen en el sur"),
                PoemLine("春来发几枝", "chūn lái fā jǐ zhī", "en primavera brotan sus ramas"),
                PoemLine("愿君多采撷", "yuàn jūn duō cǎi xié", "ojalá recojas muchas"),
                PoemLine("此物最相思", "cǐ wù zuì xiāng sī", "pues son símbolo de la añoranza")
            )
        ),
        Poem(
            id = "minnong",
            emoji = "🌾",
            titulo = "悯农",
            pinyinT = "Mǐn Nóng",
            tituloEs = "Compasión por el labrador",
            poeta = "李绅",
            poetaP = "Lǐ Shēn",
            dinastia = "Dinastía Tang",
            nota = "Enseña a no desperdiciar la comida: cada grano costó esfuerzo.",
            lineas = listOf(
                PoemLine("锄禾日当午", "chú hé rì dāng wǔ", "Al mediodía siega el grano"),
                PoemLine("汗滴禾下土", "hàn dī hé xià tǔ", "su sudor cae a la tierra"),
                PoemLine("谁知盘中餐", "shéi zhī pán zhōng cān", "¿quién piensa que en cada plato"),
                PoemLine("粒粒皆辛苦", "lì lì jiē xīn kǔ", "cada grano costó esfuerzo?")
            )
        )
    )

    val CULTURA = listOf(
        CulturalItem("🧧", "春节", "Chūnjié", "Año Nuevo chino", "La fiesta más importante. La familia se reúne, hay cena especial, petardos y sobres rojos (红包 hóngbāo) con dinero para la suerte. Cada año tiene un animal: 2026 es el año del Caballo."),
        CulturalItem("🏮", "中秋节", "Zhōngqiūjié", "Fiesta del Medio Otoño", "Se celebra con luna llena en otoño. Las familias se reúnen, encienden farolillos y comen pasteles de luna (月饼 yuèbǐng). Es la fiesta de la unión familiar."),
        CulturalItem("🐲", "端午节", "Duānwǔjié", "Fiesta del Bote del Dragón", "Carreras de barcos con forma de dragón y tamales de arroz (粽子 zòngzi). Honra al poeta Qu Yuan, que se dice que murió por su país hace 2000 años."),
        CulturalItem("🧧", "红包", "hóngbāo", "El sobre rojo", "Sobre rojo con dinero que los mayores regalan a niños y jóvenes en Año Nuevo y bodas. El rojo (红 hóng) ahuyenta la mala suerte."),
        CulturalItem("🥢", "筷子", "kuàizi", "Los palillos", "Se usan desde hace 3000 años. Regla de oro: nunca los claves verticales en el arroz, porque recuerda al incienso de los funerales."),
        CulturalItem("🍵", "茶", "chá", "El té", "China es la cuna del té: verde (绿茶 lǜchá), rojo, oolong… Ofrecer té es señal de respeto, y hay toda una ceremonia (茶道 chádào) a su alrededor.")
    )

    val LOGROS = listOf(
        Achievement("primer_paso", "👣", "Primer paso", "Gana tus primeros 10 XP"),
        Achievement("cien", "💯", "Centena", "Acumula 100 XP totales"),
        Achievement("quinientos", "🚀", "Despegue", "Acumula 500 XP totales"),
        Achievement("racha3", "🔥", "Constancia x3", "Practica 3 días seguidos"),
        Achievement("racha7", "🏮", "Semana china", "Practica 7 días seguidos"),
        Achievement("racha30", "🐉", "Mes chino", "Practica 30 días seguidos"),
        Achievement("quiz5", "⚡", "En racha", "5 aciertos seguidos en Quiz"),
        Achievement("mem_win", "🧩", "Memoria total", "Completa un Memorama"),
        Achievement("lector25", "📚", "Lector", "Repasa 25 flashcards"),
        Achievement("leccion1", "🛤️", "En camino", "Completa tu 1ª lección"),
        Achievement("unidad1", "👑", "Unidad top", "Termina una unidad entera"),
        Achievement("poeta", "🖋️", "Poeta", "Lee 3 poemas clásicos"),
        Achievement("voz", "🎤", "Buena voz", "Acierta 3 pronunciaciones con el micro"),
        Achievement("trazo", "✍️", "Calígrafo", "Completa 3 trazados con ≥60% de cobertura"),
        Achievement("mercader", "🪙", "Cliente frecuente", "Gasta 100 monedas en la tienda")
    )

    val TIENDA = listOf(
        ShopItem("corazon", "❤️", "+1 corazón", 30, "Si estás en una lección, recupera 1 corazón al instante. Si no, guarda +1 para tu próxima lección (máx 2)."),
        ShopItem("freeze", "🧊", "Protector de racha", 50, "Si un día no practicas, tu racha 🔥 no se rompe. Un uso por día perdido."),
        ShopItem("xp2", "⚡", "XP x2", 40, "Tus próximas 10 ganancias de XP cuentan doble."),
        ShopItem("pista", "💡", "Pack 3 pistas pro", 25, "En ⌨️ Pinyin, la pista te revela la 1ª sílaba completa con tono.")
    )

    val CAJAS_DIAS = listOf(0, 1, 3, 7, 15)

    fun unitWords(id: String): List<Word> {
        val u = UNIDADES.find { it.id == id }
        if (u != null) {
            if (u.cats.isNotEmpty()) {
                val words = VOCAB.filter { u.cats.contains(it.cat) }
                if (words.isNotEmpty()) return words
            }
            if (u.hsk != null) return VOCAB.filter { it.hsk == u.hsk }
        }
        val catWords = VOCAB.filter { it.cat == id }
        if (catWords.isNotEmpty()) return catWords
        return emptyList()
    }
}
