import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'

export default function SensorChart({ parameter, title, data }) {
  const filtered = data
    .filter(d => d.parameter === parameter)
    .map(d => ({
      ...d,
      readingValue: parseFloat(d.readingValue),
      timestamp: new Date(d.timestamp).toLocaleTimeString()
    }));

  return (
    <div>
      <h4 className="font-semibold">{title}</h4>
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
