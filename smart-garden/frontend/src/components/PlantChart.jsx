import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'

export default function PlantChart({ plants }) {
  const data = plants.map(p => ({
    name: p.name,
    temperature: parseInt(p.temperature),
    humidity: parseInt(p.humidity),
    light: parseInt(p.light),
    moisture: parseInt(p.soilMoisture)
  }))

  return (
    <div className="w-full h-64">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data}>
          <XAxis dataKey="name" />
          <YAxis />
          <Tooltip />
          <Bar dataKey="temperature" fill="#f97316" />
          <Bar dataKey="humidity" fill="#60a5fa" />
          <Bar dataKey="light" fill="#facc15" />
          <Bar dataKey="moisture" fill="#34d399" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
