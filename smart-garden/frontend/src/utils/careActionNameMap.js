export const careActionNameMap = {
  // Environment Adjustment
  AvoidOverwatering: 'Избягване на преполиване',
  CutOffDamagedRoots: 'Отрязване на увредени корени',
  ImproveDrainage: 'Подобряване на дренажа',
  IncreaseAirflow: 'Повишаване на въздушния поток',
  InsulateRootZone: 'Изолиране на кореновата зона',
  MoveIndoors: 'Преместване на закрито',
  MoveToBrighterArea: 'Преместване на по-светло място',
  MoveToCoolerPlace: 'Преместване на по-хладно място',
  MoveToIndirectLight: 'Преместване на индиректна светлина',
  Transplanting: 'Пресаждане',
  UseHumidifier: 'Използване на овлажнител',
  Watering: 'Поливане',

  // Nutrition
  ApplyCalcium: 'Прилагане на калций',
  ApplyIronChelate: 'Прилагане на желязо (хелат)',
  ApplyMagnesiumSalt: 'Прилагане на магнезий',
  ApplyNitrogenFertilizer: 'Прилагане на азотен тор',
  ApplyPhosphorusFertilizer: 'Прилагане на фосфорен тор',
  ApplyPotassiumFertilizer: 'Прилагане на калиев тор',
  FoliarSpray: 'Пръскане по листата',

  // Pest Control
  AlcoholSwab: 'Тампон с алкохол',
  ApplyFungicide: 'Пръскане с фунгицид',
  DryOutSoil: 'Изсушаване на почвата',
  Insecticide: 'Пръскане с инсектицид',
  IntroducePredator: 'Добавяне на естествен хищник',
  ManualRemoval: 'Ръчно премахване',
  NeemOilSpray: 'Пръскане с нийм',
  OilSpray: 'Пръскане с масло',
  RemoveDamagedLeaves: 'Премахване на повредени листа',
  RemoveInfectedParts: 'Премахване на заразени части',
  SoilDrench: 'Накисване на почвата',
  StickyTraps: 'Лепкави капани',
  VacuumInsects: 'Изсмукване на насекоми',
}

export const getReadableCareAction = (rawName) => {
  if (!rawName) return ''

  return careActionNameMap[rawName] ||
    rawName
      .replace(/([a-z])([A-Z])/g, '$1 $2')
      .replace(/[_\-]/g, ' ')
      .replace(/\b\w/g, (l) => l.toUpperCase())
}
