// ===== Cultura china: poemas Tang + fiestas y costumbres =====
// Cada poema: líneas con hanzi, pinyin y traducción al español.
const POEMAS = [
  {
    id: 'jingyesi', emoji: '🌙',
    titulo: '静夜思', pinyinT: 'Jìng Yè Sī', tituloEs: 'Pensando en la noche',
    poeta: '李白', poetaP: 'Lǐ Bái', dinastia: 'Dinastía Tang (siglo VIII)',
    nota: 'El poema que todo niño chino memoriza. Habla de la nostalgia por el hogar.',
    lineas: [
      { hz: '床前明月光', py: 'chuáng qián míng yuè guāng', es: 'Ante mi cama, la luz de la luna' },
      { hz: '疑是地上霜', py: 'yí shì dì shàng shuāng', es: 'parece escarcha sobre el suelo' },
      { hz: '举头望明月', py: 'jǔ tóu wàng míng yuè', es: 'Levanto la cabeza y miro la luna' },
      { hz: '低头思故乡', py: 'dī tóu sī gù xiāng', es: 'la bajo, y pienso en mi tierra' },
    ]
  },
  {
    id: 'chunxiao', emoji: '🌸',
    titulo: '春晓', pinyinT: 'Chūn Xiǎo', tituloEs: 'Amanecer de primavera',
    poeta: '孟浩然', poetaP: 'Mèng Hàorán', dinastia: 'Dinastía Tang',
    nota: 'Una mañana de primavera entre pájaros y flores caídas.',
    lineas: [
      { hz: '春眠不觉晓', py: 'chūn mián bù jué xiǎo', es: 'Dormido en primavera, no sentí el amanecer' },
      { hz: '处处闻啼鸟', py: 'chù chù wén tí niǎo', es: 'por todas partes se oyen pájaros' },
      { hz: '夜来风雨声', py: 'yè lái fēng yǔ shēng', es: 'anoche hubo viento y lluvia' },
      { hz: '花落知多少', py: 'huā luò zhī duō shǎo', es: '¿cuántas flores habrán caído?' },
    ]
  },
  {
    id: 'dengguanquelou', emoji: '🏔️',
    titulo: '登鹳雀楼', pinyinT: 'Dēng Guànquè Lóu', tituloEs: 'Subiendo la torre del Cigüeñón',
    poeta: '王之涣', poetaP: 'Wáng Zhīhuàn', dinastia: 'Dinastía Tang',
    nota: 'Su moraleja se usa como refrán: para ver más lejos, hay que subir más alto.',
    lineas: [
      { hz: '白日依山尽', py: 'bái rì yī shān jìn', es: 'El sol se pone tras las montañas' },
      { hz: '黄河入海流', py: 'huáng hé rù hǎi liú', es: 'el Río Amarillo fluye hacia el mar' },
      { hz: '欲穷千里目', py: 'yù qióng qiān lǐ mù', es: 'para ver mil lis más lejos' },
      { hz: '更上一层楼', py: 'gèng shàng yì céng lóu', es: 'sube un piso más' },
    ]
  },
  {
    id: 'xiangsi', emoji: '❤️',
    titulo: '相思', pinyinT: 'Xiāngsī', tituloEs: 'Añoranza',
    poeta: '王维', poetaP: 'Wáng Wéi', dinastia: 'Dinastía Tang',
    nota: 'Las judías rojas (红豆) son en China símbolo del amor y la añoranza.',
    lineas: [
      { hz: '红豆生南国', py: 'hóng dòu shēng nán guó', es: 'Las judías rojas crecen en el sur' },
      { hz: '春来发几枝', py: 'chūn lái fā jǐ zhī', es: 'en primavera brotan sus ramas' },
      { hz: '愿君多采撷', py: 'yuàn jūn duō cǎi xié', es: 'ojalá recojas muchas' },
      { hz: '此物最相思', py: 'cǐ wù zuì xiāng sī', es: 'pues son símbolo de la añoranza' },
    ]
  },
  {
    id: 'minnong', emoji: '🌾',
    titulo: '悯农', pinyinT: 'Mǐn Nóng', tituloEs: 'Compasión por el labrador',
    poeta: '李绅', poetaP: 'Lǐ Shēn', dinastia: 'Dinastía Tang',
    nota: 'Enseña a no desperdiciar la comida: cada grano costó esfuerzo.',
    lineas: [
      { hz: '锄禾日当午', py: 'chú hé rì dāng wǔ', es: 'Al mediodía siega el grano' },
      { hz: '汗滴禾下土', py: 'hàn dī hé xià tǔ', es: 'su sudor cae a la tierra' },
      { hz: '谁知盘中餐', py: 'shéi zhī pán zhōng cān', es: '¿quién piensa que en cada plato' },
      { hz: '粒粒皆辛苦', py: 'lì lì jiē xīn kǔ', es: 'cada grano costó esfuerzo?' },
    ]
  },
];

const CULTURA = [
  { emoji: '🧧', zh: '春节', py: 'Chūnjié', es: 'Año Nuevo chino',
    texto: 'La fiesta más importante. La familia se reúne, hay cena especial, petardos y sobres rojos (红包 hóngbāo) con dinero para la suerte. Cada año tiene un animal: 2026 es el año del Caballo.' },
  { emoji: '🏮', zh: '中秋节', py: 'Zhōngqiūjié', es: 'Fiesta del Medio Otoño',
    texto: 'Se celebra con luna llena en otoño. Las familias se reúnen, encienden farolillos y comen pasteles de luna (月饼 yuèbǐng). Es la fiesta de la unión familiar.' },
  { emoji: '🐲', zh: '端午节', py: 'Duānwǔjié', es: 'Fiesta del Bote del Dragón',
    texto: 'Carreras de barcos con forma de dragón y tamales de arroz (粽子 zòngzi). Honra al poeta Qu Yuan, que se dice que murió por su país hace 2000 años.' },
  { emoji: '🧧', zh: '红包', py: 'hóngbāo', es: 'El sobre rojo',
    texto: 'Sobre rojo con dinero que los mayores regalan a niños y jóvenes en Año Nuevo y bodas. El rojo (红 hóng) ahuyenta la mala suerte.' },
  { emoji: '🥢', zh: '筷子', py: 'kuàizi', es: 'Los palillos',
    texto: 'Se usan desde hace 3000 años. Regla de oro: nunca los claves verticales en el arroz, porque recuerda al incienso de los funerales.' },
  { emoji: '🍵', zh: '茶', py: 'chá', es: 'El té',
    texto: 'China es la cuna del té: verde (绿茶 lǜchá), rojo, oolong… Ofrecer té es señal de respeto, y hay toda una ceremonia (茶道 chádào) a su alrededor.' },
];
