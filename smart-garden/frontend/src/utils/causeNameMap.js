export const causeNameMap = {
  // Environment
  HighHumidity: 'Висока влажност',
  HighLight: 'Прекомерна светлина',
  HighTemperature: 'Висока температура',
  LowHumidity: 'Ниска влажност',
  LowLight: 'Недостатъчно осветление',
  LowTemperature: 'Ниска температура',
  Overwatering: 'Прекомерно поливане',
  WaterDeficiency: 'Недостиг на вода',

  // Nutrition
  CalciumDeficiency: 'Недостиг на калций',
  IronDeficiency: 'Недостиг на желязо',
  MagnesiumDeficiency: 'Недостиг на магнезий',
  NitrogenDeficiency: 'Недостиг на азот',
  PhosphorusDeficiency: 'Недостиг на фосфор',
  PotassiumDeficiency: 'Недостиг на калий',

  // Pests
  Aphids: 'Листни въшки',
  Beetles: 'Бръмбари',
  Caterpillars: 'Гъсеници',
  FungalInfection: 'Гъбична инфекция',
  FungusGnats: 'Гъбни комарчета',
  PowderyMildew: 'Брашнеста мана',
  Pythium: 'Питиум (кореново гниене)',
  ScaleInsects: 'Щитоносни въшки',
  SpiderMites: 'Паяжинообразуващи акари',
  Thrips: 'Трипсове',
  Whiteflies: 'Бели мухи',
  Wireworms: 'Телени червеи',
}

export const getReadableCauseName = (rawName) => {
  if (!rawName) return ''

  return causeNameMap[rawName] ||
    rawName
      .replace(/([a-z])([A-Z])/g, '$1 $2')
      .replace(/[_\-]/g, ' ')
      .replace(/\b\w/g, (l) => l.toUpperCase())
}

