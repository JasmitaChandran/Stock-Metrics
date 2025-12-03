import { createSlice } from '@reduxjs/toolkit'
import Keycloak from 'keycloak-js'

let keycloak

const slice = createSlice({
  name: 'auth',
  initialState: { isAuthenticated: false, token: null, profile: null },
  reducers: {
    setAuthenticated(state, action) {
      state.isAuthenticated = true
      state.token = action.payload.token
      state.profile = action.payload.profile
    },
    clearAuth(state) {
      state.isAuthenticated = false
      state.token = null
      state.profile = null
    }
  }
})

export const { setAuthenticated, clearAuth } = slice.actions

export const initKeycloak = () => dispatch => {
  keycloak = new Keycloak({
    url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080',
    realm: import.meta.env.VITE_KEYCLOAK_REALM || 'stock-metrics',
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT || 'stock-metrics-ui'
  })
  keycloak.init({ onLoad: 'check-sso', pkceMethod: 'S256', checkLoginIframe: false }).then(auth => {
    if (auth) {
      dispatch(setAuthenticated({ token: keycloak.token, profile: keycloak.tokenParsed }))
    }
  })
}

export const triggerLogin = () => () => {
  if (keycloak) {
    keycloak.login()
  }
}

export const selectIsAuthenticated = state => state.auth.isAuthenticated
export const selectToken = state => state.auth.token

export default slice.reducer
