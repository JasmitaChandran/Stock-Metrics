import React, { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Line } from 'react-chartjs-2'
import { subscribeToPriceStream, unsubscribe } from '../utils/websocketClient'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Legend
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend)

const LiveChart = () => {
  const { symbol } = useParams()
  const [points, setPoints] = useState([])

  useEffect(() => {
    const handle = subscribeToPriceStream(symbol, message => {
      setPoints(prev => [...prev.slice(-30), { ts: new Date(message.timestamp || Date.now()), price: message.last }])
    })
    return () => unsubscribe(handle)
  }, [symbol])

  const data = {
    labels: points.map(p => p.ts.toLocaleTimeString()),
    datasets: [
      {
        label: symbol,
        data: points.map(p => p.price),
        borderColor: '#0d6efd',
        tension: 0.3
      }
    ]
  }

  return (
    <div className="container py-4">
      <h3>{symbol} live</h3>
      <Line data={data} />
    </div>
  )
}

export default LiveChart
