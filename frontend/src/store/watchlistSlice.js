import { createSlice } from '@reduxjs/toolkit'

const slice = createSlice({
  name: 'watchlist',
  initialState: { symbols: ['AAPL', 'MSFT'] },
  reducers: {
    addSymbol(state, action) {
      const sym = action.payload
      if (!state.symbols.includes(sym)) {
        state.symbols.push(sym)
      }
    }
  }
})

export const { addSymbol } = slice.actions
export const selectWatchlist = state => state.watchlist.symbols
export default slice.reducer
