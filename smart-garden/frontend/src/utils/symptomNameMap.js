export const symptomNameMap = {

  // Категории (вътрешни възли)
  'LeafSymptom': 'Симптоми по листата',
  'ColorChangeSymptom': 'Промяна в цвета на листата',
  'LeafFormDeformation': 'Деформации на формата на листата',
  'MarginAndEdgeDamage': 'Повреди по краищата на листата',
  'PestRelatedLeafSymptom': 'Симптоми от вредители по листата',
  'RootOrSoilSymptom': 'Симптоми по корени или почва',
  'StemSymptom': 'Симптоми по стъблото',

  // Leaf Symptoms - Color Change
  'InterveinalChlorosis': 'Хлороза между жилките',
  'LeafPaleAndElongated': 'Избледнели и удължени листа',
  'LeafReddishPurpleTinge': 'Червено-виолетов отенък на листа',
  'LeafYellowing': 'Пожълтяване на листата',
  'OldLeavesYellowNewGreen': 'Старите листа пожълтяват, новите са зелени',
  'SpreadingBrownYellowPatches': 'Разпространяващи се кафяво-жълти петна',
  'YoungLeavesYellowDeformed': 'Млади жълти и деформирани листа',

  // Leaf Symptoms - Form Deformation
  'LeafCurling': 'Навити листа',
  'WiltedLeaves': 'Увяхнали листа',

  // Leaf Symptoms - Margin & Edge
  'BrownLeafMargins': 'Кафяви краища на листа',
  'LeafEdgeBurns': 'Изгаряния по ръбовете на листата',

  // Pest-related Leaf Symptoms
  'BlackMoldOnLeaves': 'Черен мухъл по листата',
  'LeafHoles': 'Дупки по листата',
  'PowderyWhiteSubstanceOnLeaves': 'Бяло прахообразно покритие по листата',
  'PunctureMarksOnLeavesAndStems': 'Дупчици по листата и стъблата',
  'ShellBugsOnLeaves': 'Щитоносни насекоми по листата',
  'StickyLeavesWithBlackMold': 'Лепкави листа с черен мухъл',
  'WebbingAndSpotsOnLeaves': 'Паяжини и петна по листата',
  'WhitefliesFlyingFromLeaves': 'Бели мухи, излитащи от листата',

  // Root or Soil Symptoms
  'BaseRotFromSoil': 'Гниене в основата от почвата',
  'LarvaeinSoil': 'Ларви в почвата',

  // Stem Symptoms
  'StemBlackeningAtBase': 'Почерняване на стъблото в основата',
  'StemCracking': 'Напукване на стъблото',
  'StemSofteningAndCollapse': 'Омекване и срутване на стъблото',
}

export const getReadableSymptomName = (rawName) => {
  if (!rawName) return ''

  return rawName
    .split('/') 
    .map(part => {
      const trimmed = part.trim()
      return symptomNameMap[trimmed] ||
        trimmed
          .replace(/([a-z])([A-Z])/g, '$1 $2')
          .replace(/[_\-]/g, ' ')
          .replace(/\b\w/g, (l) => l.toUpperCase())
    })
    .join(' / ')
}



