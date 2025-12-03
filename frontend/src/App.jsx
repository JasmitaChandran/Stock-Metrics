import React, { useEffect } from 'react'
import { Route, Routes, useNavigate } from 'react-router-dom'
import LoginPage from './components/LoginPage'
import Watchlist from './components/Watchlist'
import LiveChart from './components/LiveChart'
import { useDispatch, useSelector } from 'react-redux'
import { initKeycloak, selectIsAuthenticated } from './store/authSlice'

const App = () => {
  const dispatch = useDispatch()
  const isAuthenticated = useSelector(selectIsAuthenticated)
  const navigate = useNavigate()

  useEffect(() => {
    dispatch(initKeycloak())
  }, [dispatch])

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/watchlist')
    }
  }, [isAuthenticated, navigate])

  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/watchlist" element={<Watchlist />} />
      <Route path="/chart/:symbol" element={<LiveChart />} />
    </Routes>
  )
}

export default App
