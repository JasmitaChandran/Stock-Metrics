import React from 'react'
import { useDispatch } from 'react-redux'
import { triggerLogin } from '../store/authSlice'

const LoginPage = () => {
  const dispatch = useDispatch()

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card shadow-sm">
            <div className="card-body text-center">
              <h3 className="card-title mb-3">Stock Metrics</h3>
              <p className="text-muted">Sign in with Keycloak to access your watchlist.</p>
              <button className="btn btn-primary" onClick={() => dispatch(triggerLogin())}>
                Login with Keycloak
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default LoginPage
