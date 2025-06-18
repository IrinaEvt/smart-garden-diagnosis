import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'

export default function SensorChart({ parameter, data }) {
  // 🔍 Филтрираме само стойностите за текущия параметър (temperature, humidity и т.н.)
  const filtered = data
    .filter(d => d.parameter === parameter)
    .map(d => ({
      ...d,
      readingValue: parseFloat(d.readingValue), // гарантираме числово
      timestamp: new Date(d.timestamp).toLocaleTimeString() // по-кратко време
    }))

    console.log("filtered data", filtered) 
  return (
    <div>
      <h4 className="font-semibold">{parameter}</h4>
      <ResponsiveContainer width="100%" height={200}>
        <LineChart data={filtered}>
          <XAxis dataKey="timestamp" />
          <YAxis />
          <Tooltip />
          <Line type="monotone" dataKey="readingValue" stroke="#8884d8" dot={true} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}
