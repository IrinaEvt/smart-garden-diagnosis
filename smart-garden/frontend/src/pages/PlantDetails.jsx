import { useEffect, useState, Fragment } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import axios from '../api/axios'
import { useAuth } from '../auth/AuthContext'
import { Listbox, Transition } from '@headlessui/react'
import { ChevronUpDownIcon, CheckIcon } from '@heroicons/react/20/solid'
import SensorChart from '../components/SensorChart'
import SidebarNavigation from '../components/SidebarNavigation'
import { getReadableSymptomName } from '../utils/symptomNameMap'
import { getReadableCauseName } from '../utils/causeNameMap'
import { getReadableCareAction } from '../utils/careActionNameMap'
import { sensorParameterLabels,  parameterUnits } from '../utils/parameterLabels'
import { useFloating, offset, flip, shift } from '@floating-ui/react';


export default function PlantDetails() {
  const { id } = useParams();
  const { token } = useAuth()

  const [plant, setPlant] = useState(null)
  const [plantNeeds, setPlantNeeds] = useState({});
  const [suggestions, setSuggestions] = useState([])
  const [symptoms, setSymptoms] = useState([])
  const [reasoning, setReasoning] = useState([])
  const [symptomOptions, setSymptomOptions] = useState({})
  const [tab, setTab] = useState('info')
  const [selectedGroup, setSelectedGroup] = useState('')
  const [selectedSymptom, setSelectedSymptom] = useState(null)
  const [sensorHistory, setSensorHistory] = useState([])
  const [sensorAlerts, setSensorAlerts] = useState([])
  const [easySuggestion, setEasySuggestion] = useState(null)
  const [recognizedSymptoms, setRecognizedSymptoms] = useState([]);
  const [selectedImage, setSelectedImage] = useState(null);
  const [imageBase64, setImageBase64] = useState(null);
  const [loading, setLoading] = useState(false);
const [plantRisks, setPlantRisks] = useState([])




  const imageMap = {
    "Ctenanthe":"/images/ctenanthe.png",
    "Calathea": "/images/calathea.png",
    "Cyclamen": "/images/cyclamen.jpg",
    "Primula": "/images/primula.jpg",
    "Orchid": "/images/orchid.jpg",
    "SnakePlant": "/images/fern.jpg",
    "Dracaena": "/images/dracaena.jpg",
    "Opuntia": "/images/opuntia.jpg",
    "EchinocactusGrusonii": "/images/echinocactus-grusonii.jpg"
  }

  const { refs, floatingStyles } = useFloating({
  placement: 'bottom-start',
  middleware: [offset(4), flip(), shift()],
});


  
  useEffect(() => {
    fetchPlant()
    fetchSymptoms()
    fetchReasoning()
    fetchSymptomOptions()
  }, [])

  const fetchPlant = async () => {
    const res = await axios.get(`/plants/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    console.log("Data", res.data)
    setPlant(res.data)
     fetchNeeds(res.data.type);
    fetchSuggestions(res.data.type)
  }

  const fetchNeeds = async (type) => {
  try {
    const res = await axios.get(`/plants/${type}/needs`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    setPlantNeeds(res.data);
  } catch (err) {
    console.error('Грешка при зареждане на нуждите:', err);
  }
};

const fetchRiskAssessment = async () => {
  try {
    const res = await axios.get(`/alerts`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const allRisks = res.data;
    const currentPlantRisks = allRisks[plant.name] || [];
    setPlantRisks(currentPlantRisks);
  } catch (err) {
    console.error("Грешка при оценка на риска:", err);
  }
}




  const fetchSuggestions = async (type) => {
    
    try {
      const res = await axios.get(`/plants/types/suggestions/${type}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      setSuggestions(res.data)
    } catch (err) {
      console.error('Грешка при зареждане на предложенията:', err)
    }
  }

  const fetchSensorHistory = async () => {
    const res = await axios.get(`/plants/${id}/sensors/history`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    console.log(res.data)
    const parsed = res.data.readings.map(r => ({
      ...r,
      readingValue: parseFloat(r.readingValue)
    }))
    setSensorHistory(parsed)
    setSensorAlerts(res.data.alerts)
     setShowRiskButton(true) 

    if (res.data.alerts.length > 2) {
      fetchEasyCareSuggestion(res.data.alerts.length)
    } else {
      setEasySuggestion(null)
    }
  }

  const fetchEasyCareSuggestion = async (issueCount) => {
    try {
      const res = await axios.get(`/plants/suggestions/easy-care?issueCount=${issueCount}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      if (res.data && typeof res.data === 'string') {
        setEasySuggestion(res.data)
      } else {
        setEasySuggestion(null)
      }
    } catch (err) {
      console.error("Грешка при зареждане на лесно растение:", err)
    }
  }

  const fetchSymptoms = async () => {
    const res = await axios.get(`/reasoning/${id}/symptoms`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    setSymptoms(res.data)
  }

  const fetchReasoning = async () => {
    try {
      const res = await axios.get(`/reasoning/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      console.log(res.data.reasoning)
      setReasoning(res.data.reasoning || [])
    } catch (err) {
      console.error('Грешка при зареждане на reasoning:', err)
      setReasoning([])
    }
  }

  const fetchSymptomOptions = async () => {
    try {
      const res = await axios.get('/reasoning/symptom-options', {
        headers: { Authorization: `Bearer ${token}` }
      })
      setSymptomOptions(res.data)
    } catch (err) {
      console.error('Грешка при зареждане на симптомите:', err)
    }
  }

  const deleteSymptom = async (symptomId) => {
    await axios.delete(`/reasoning/${id}/symptoms/${symptomId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    await fetchSymptoms()
    await fetchReasoning()
  }

  const addSymptom = async (e) => {
    e.preventDefault()
    if (!selectedSymptom?.trim()) return

    await axios.post(`/reasoning/${id}/symptoms`, { name: selectedSymptom }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    setSelectedGroup('')
    setSelectedSymptom(null)
    await fetchSymptoms()
    await fetchReasoning()
  }

  const addRecognizedSymptoms = async () => {
  try {
    const added = await Promise.all(
      recognizedSymptoms.map(symptom =>
        axios.post(`/reasoning/${id}/symptoms`, { name: symptom }, {
          headers: { Authorization: `Bearer ${token}` }
        })
      )
    );
    await fetchSymptoms(); 
    await fetchReasoning();
  } catch (err) {
    console.error("Грешка при добавяне на разпознати симптоми:", err);
  }
};

  const handleImageSelect = (e) => {
  const file = e.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onloadend = () => {
    setSelectedImage(reader.result); // показване на preview
    setImageBase64(reader.result);  // изпращане към API
  };
  reader.readAsDataURL(file);
};

const analyzeImage = async () => {
  if (!imageBase64) return;
  setLoading(true); 
  try {
    const res = await axios.post('/llm/health', {
      url: "https://api.openai.com/v1/chat/completions",
      apiKey: 'api-key',
      model: 'gpt-4o-mini',
      image: imageBase64,
      plantName: plant.type 
    });

    if (Array.isArray(res.data)) {
      setRecognizedSymptoms(res.data);
    } else {
      console.error("Грешен отговор:", res.data);
    }
  } catch (error) {
    console.error("Грешка при анализ:", error);
  } finally {
    setLoading(false); 
  }
};



  if (!plant) return <p className="text-white p-4">Зареждане...</p>

  return (
    <div className="flex bg-[#0f1e13] text-white min-h-screen">
      <SidebarNavigation />
      <main className="flex-1 py-8 px-4 space-y-8">
        <div className="flex space-x-4 border-b border-green-800 pb-2 mb-4">
  <button
    onClick={() => setTab('info')}
    className={`px-4 py-2 rounded-t-md font-semibold ${
      tab === 'info' ? 'bg-green-600 text-white' : 'bg-[#1a2a1f] text-gray-400'
    }`}
  >
     Инфо
  </button>
  <button
    onClick={() => setTab('reasoning')}
    className={`px-4 py-2 rounded-t-md font-semibold ${
      tab === 'reasoning' ? 'bg-green-600 text-white' : 'bg-[#1a2a1f] text-gray-400'
    }`}
  >
     Причини
  </button>
  <button
    onClick={() => setTab('sensors')}
    className={`px-4 py-2 rounded-t-md font-semibold ${
      tab === 'sensors' ? 'bg-green-600 text-white' : 'bg-[#1a2a1f] text-gray-400'
    }`}
  >
     Сензори
  </button>
</div>

{tab === 'info' && (
  <div className="flex flex-col md:flex-row gap-6 items-start">
  
    {/* Лява колона: снимка */}
    <div className="w-full md:w-64 shrink-0">
      <img
        src={plant.imageUrl || '/images/default.jpg'}
        alt={plant.name}
        className="object-cover rounded-xl w-full h-48 md:h-64"
      />
    </div>

   
    <div className="flex-1 space-y-6">
      <div className="border-b border-green-800 pb-4">
        <h3 className="text-lg font-semibold mb-2">Основна информация</h3>
        <p><strong>Тип:</strong> {plant.type}</p>
        <p><strong>Семейство:</strong> {plant.family}</p>
      </div>




{plantNeeds && Object.keys(plantNeeds).length > 0 && (
  <div className="border-b border-green-800 pb-4">
    <h3 className="text-lg font-semibold mb-2">🌿 Нужди на растението</h3>
    <div className="flex flex-wrap gap-6 text-sm text-white">
      {["light", "humidity", "temperature", "soilMoisture"].map((param) => (
        <div key={param} className="flex flex-col items-center">
          <span className="font-semibold mb-1">
            {sensorParameterLabels[param] || param}
          </span>
          <div className="flex gap-2">
            <span className={plantNeeds[param] === "low" ? "text-green-400 underline" : "text-gray-400"}>low</span>
            <span className={plantNeeds[param] === "high" ? "text-green-400 underline" : "text-gray-400"}>high</span>
          </div>
        </div>
      ))}
    </div>
  </div>
)}




      {suggestions.length > 0 && (
        <div className="border-b border-green-800 pb-4">
          <h3 className="text-lg font-semibold mb-4">
            🌱 Други растения от семейство <span className="italic text-green-400">{plant.family}</span>
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {suggestions.map((sugg, i) => (
              <div key={i} className="bg-[#16291e] border border-green-600 rounded-lg p-4 flex flex-col items-center text-center shadow hover:shadow-lg">
                <img
                  src={imageMap[sugg] || '/images/default.jpg'} 
                  alt={sugg}
                  className="w-32 h-24 object-cover rounded mb-2"
                />
                <p className="text-white font-medium">{sugg}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {symptoms.length > 0 && (
        <div className="border-b border-green-800 pb-4">
          <h3 className="text-lg font-semibold mb-2">📋 История на симптомите</h3>
          <ul className="list-disc list-inside mt-2">
            {symptoms.map(s => (
              <li key={s.id} className="flex justify-between items-center">
                <span>{getReadableSymptomName(s.name)}</span>
                <button onClick={() => deleteSymptom(s.id)} className="text-red-400 hover:text-red-600 text-sm">Изтрий</button>
              </li>
            ))}
          </ul>
        </div>
      )}

      <form onSubmit={addSymptom} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Listbox value={selectedGroup} onChange={(val) => {
            setSelectedGroup(val);
            setSelectedSymptom(null);
          }}>
            <div ref={refs.setReference}>
    <Listbox.Button className="relative w-full bg-[#1a2a1f] border border-green-600 text-left py-2 pl-3 pr-10 rounded-md shadow-md text-white">
      {selectedGroup ? getReadableSymptomName(selectedGroup) : 'Избери категория'}
    </Listbox.Button>
  </div>

  <Listbox.Options
    ref={refs.setFloating}
    style={floatingStyles}
    className="z-50 w-full max-h-64 overflow-y-auto rounded-md bg-[#1a2a1f] text-white ring-1 ring-green-500"
  >
    {Object.keys(symptomOptions).map((group, i) => (
      <Listbox.Option key={i} value={group} className={({ active }) =>
        `cursor-pointer select-none px-4 py-2 ${active ? 'bg-green-600' : ''}`}>
        {getReadableSymptomName(group)}
      </Listbox.Option>
    ))}
  </Listbox.Options>
</Listbox>

          {selectedGroup && (
            <Listbox value={selectedSymptom} onChange={setSelectedSymptom}>
              <div className="relative">
                <Listbox.Button className="relative w-full bg-[#1a2a1f] border border-green-600 text-left py-2 pl-3 pr-10 rounded-md shadow-md text-white">
                  {selectedSymptom ? getReadableSymptomName(selectedSymptom) : 'Избери симптом'}
                  <ChevronUpDownIcon className="absolute inset-y-0 right-0 w-5 h-5 text-gray-400 mr-2" />
                </Listbox.Button>
                <Transition as={Fragment} leave="transition ease-in duration-100" leaveFrom="opacity-100" leaveTo="opacity-0">
                 <Listbox.Options className="absolute z-10 mt-1 w-full max-h-64 overflow-y-auto rounded-md bg-[#1a2a1f] text-white ring-1 ring-green-500">
                    {symptomOptions[selectedGroup]?.map((symptom, i) => (
                      <Listbox.Option key={i} value={symptom} className={({ active }) =>
                        `cursor-pointer select-none px-4 py-2 ${active ? 'bg-green-600' : ''}`}>
                        {({ selected }) => (
                          <span className={`block ${selected ? 'font-semibold' : ''}`}>
                            {getReadableSymptomName(symptom)}
                            {selected && <CheckIcon className="w-4 h-4 inline ml-2" />}
                          </span>
                        )}
                      </Listbox.Option>
                    ))}
                 </Listbox.Options>
                </Transition>
              </div>
            </Listbox>
          )}
        </div>
        <button
          type="submit"
          className="bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 px-4 py-2 rounded-full font-semibold"
          disabled={!selectedSymptom}
        >
          Добави симптом
        </button>
      </form>
    </div>

    {/* Дясна колона: само анализ на изображение */}
    <div className="w-full md:w-80 shrink-0 pr-2 md:pr-6 space-y-4">
 <div className="space-y-4">
      {/* Качване на изображение */}
      <label className="block">
        <span className="text-sm text-gray-400">Качи изображение:</span>
        <input
          type="file"
          accept="image/*"
          onChange={handleImageSelect}
          className="block w-full mt-2 text-sm text-white file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:bg-green-600 file:text-white hover:file:bg-green-700"
        />
      </label>

      {/* Преглед на снимката */}
      {selectedImage && (
        <div className="border border-green-700 rounded overflow-hidden">
          <img
            src={selectedImage}
            alt="Преглед"
            className="w-full object-cover"
          />
        </div>
      )}

      {/* Бутон за анализ */}
      {selectedImage && (
        <button
          onClick={analyzeImage}
          className="w-full bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 px-4 py-2 rounded-full font-semibold"
        >
          Анализирай изображение
        </button>
      )}

        {loading && (
          <div className="flex items-center justify-center py-4">
            <svg className="animate-spin h-6 w-6 text-green-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
            </svg>
            <span className="ml-2 text-sm text-gray-300">Анализиране на изображението...</span>
          </div>
        )}


      {/* Разпознати симптоми */}
      {recognizedSymptoms.length > 0 && (
        <div className="pt-4">
          <h4 className="text-sm font-semibold text-green-400 mb-1">Разпознати симптоми:</h4>
          <ul className="list-disc list-inside text-sm text-white space-y-1 mb-4">
            {recognizedSymptoms.map((symptom, i) => (
              <li key={i}>{getReadableSymptomName(symptom)}</li>
              ))}
          </ul>
           <button
      onClick={addRecognizedSymptoms}
      className="w-full bg-green-700 hover:bg-green-800 px-4 py-2 rounded-full text-sm font-medium"
    >
      ➕ Добави към симптомите
    </button>
        </div>
      )}
    </div>
  </div>
</div>
 )}

           {tab === 'reasoning' && (
  <div className="space-y-6">
    {reasoning.length === 0 ? (
      <p className="text-gray-400 italic">Няма съвети за показване.</p>
    ) : (
      reasoning.map((block, i) => (
        <div key={i} className="bg-[#16291e] border border-green-700 rounded-xl p-4 shadow space-y-3">
          <h3 className="text-xl font-semibold text-green-300">🧠 Възможна причина: {getReadableCauseName(block.cause)}</h3>

          <div>
            <h4 className="font-medium text-white">🔍 Свързани симптоми:</h4>
            <ul className="list-disc list-inside ml-4 text-white">
             {block.symptoms.map((s, idx) => {
              const pureSymptom = s.split('_').pop();
              return <li key={idx}>{getReadableSymptomName(pureSymptom)}</li>;
              })}
            </ul>
          </div>

          {block.actions.length > 0 && (
            <div>
              <h4 className="font-medium text-white">💡 Препоръчани действия:</h4>
              <ul className="list-disc list-inside ml-4 text-white">
                {block.actions.map((a, idx) => (
                  <li key={idx}>{getReadableCareAction(a)}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      ))
    )}
  </div>
)}

{tab === 'sensors' && (
  <div className="space-y-6">
    <div className="border-b border-green-800 pb-4">
      <h3 className="text-lg font-semibold mb-2">📡 Сензорни стойности</h3>
      <button
        onClick={fetchSensorHistory}
        className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-full text-sm"
      >
        Обнови сензори
      </button>

      {(sensorAlerts.length > 0 || plantRisks.length > 0) && (
        <div className="mt-4 flex gap-8 items-start">
          {/* Вляво: сензорни предупреждения */}
          <div className="text-red-400 flex-1">
            <h4 className="font-medium">⚠️ Проблеми със сензорите:</h4>
            <ul className="list-disc list-inside text-white">
              {sensorAlerts.map((alert, i) => {
                const match = alert.match(/в (\w+): нужно '(\w+)', стойност: ([\d.]+)/);
                if (!match) return <li key={i}>{alert}</li>;

                const [_, param, level, rawValue] = match;
                const value = parseFloat(rawValue);
                const unit = parameterUnits[param] || '';
                const localizedParam = sensorParameterLabels[param] || param;

                let valueClass = '';
                if (level === 'high') valueClass = 'text-blue-400 font-semibold';
                if (level === 'low') valueClass = 'text-red-400 font-semibold';

                const highlightedValue = `<span class="${valueClass}">${Math.round(value)}${unit}</span>`;
                const alertWithLabel = alert.replace(param, localizedParam);
                const finalAlert = alertWithLabel.replace(rawValue, highlightedValue);

                return (
                  <li
                    key={i}
                    dangerouslySetInnerHTML={{ __html: finalAlert }}
                  />
                );
              })}
            </ul>
          </div>

          {/* Вдясно: рискове и бутон за оценка */}
          <div className="flex flex-col gap-4 flex-1 text-orange-400">
            {sensorAlerts.length > 2 && (
              <button
                onClick={fetchRiskAssessment}
                className="px-4 py-2 bg-orange-500 hover:bg-orange-600 text-white rounded-full text-sm self-start"
              >
                🧠 Оцени риска
              </button>
            )}

            {plantRisks.length > 0 && (
              <div>
                <h4 className="font-medium">🧠 Рискове:</h4>
                <ul className="list-disc list-inside text-white">
                  {plantRisks.map((r, i) => (
                    <li key={i}>{r}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Няма проблеми */}
      {sensorAlerts.length === 0 && (
        <p className="text-green-400 mt-4">✅ Всичко е в норма!</p>
      )}

      {/* Лесна препоръка */}
      {easySuggestion && (
        <div className="mt-6 border-t border-green-800 pt-4">
          <h3 className="text-lg font-semibold text-green-300">🌿 Препоръка</h3>
          <p className="text-white mt-2">
            Забелязахме, че растението има няколко проблеми със сензорите.
            Може да обмислиш растение от клас <strong>{easySuggestion}</strong>, което е по-лесно за отглеждане. 🌱
          </p>
        </div>
      )}
    </div>

    {/* Графики */}
    {["temperature", "light", "humidity", "soilMoisture"].map(param => (
      <SensorChart
        key={param}
        parameter={param}
        title={sensorParameterLabels[param]} 
        data={sensorHistory}
      />
    ))}
  </div>
)}

      </main>
    </div>
  )
}
