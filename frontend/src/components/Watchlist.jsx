import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { addSymbol, selectWatchlist } from '../store/watchlistSlice'
import { subscribeToPriceStream, unsubscribe } from '../utils/websocketClient'
import { Link } from 'react-router-dom'

const Watchlist = () => {
  const dispatch = useDispatch()
  const watchlist = useSelector(selectWatchlist)
  const [input, setInput] = useState('AAPL')
  const [prices, setPrices] = useState({})

  useEffect(() => {
    const handlers = watchlist.map(symbol =>
      subscribeToPriceStream(symbol, message => {
        setPrices(prev => ({ ...prev, [symbol]: message.last }))
      })
    )
    return () => handlers.forEach(unsubscribe)
  }, [watchlist])

  return (
    <div className="container py-4">
      <div className="d-flex mb-3">
        <input className="form-control me-2" value={input} onChange={e => setInput(e.target.value)} />
        <button className="btn btn-success" onClick={() => dispatch(addSymbol(input.toUpperCase()))}>Add</button>
      </div>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Last</th>
            <th>Chart</th>
          </tr>
        </thead>
        <tbody>
          {watchlist.map(symbol => (
            <tr key={symbol}>
              <td>{symbol}</td>
              <td>{prices[symbol] ?? '...'}</td>
              <td>
                <Link to={`/chart/${symbol}`}>View</Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default Watchlist
