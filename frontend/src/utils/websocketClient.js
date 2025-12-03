const sockets = new Map()

export function subscribeToPriceStream(symbol, onMessage) {
  const url = `${import.meta.env.VITE_STREAM_URL || 'ws://localhost:8082'}/stream/prices/${symbol}`
  const socket = new WebSocket(url)
  socket.onmessage = event => {
    const data = JSON.parse(event.data)
    onMessage(data)
  }
  sockets.set(symbol, socket)
  return symbol
}

export function unsubscribe(symbol) {
  const socket = sockets.get(symbol)
  if (socket) {
    socket.close()
    sockets.delete(symbol)
  }
}
