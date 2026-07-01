import http from './http'

export function pageMessages(params) {
  return http.get('/messages', { params })
}

export function fetchUnreadCount() {
  return http.get('/messages/unread-count')
}

export function markMessageRead(id) {
  return http.post(`/messages/${id}/read`)
}

export function markAllMessagesRead(data = {}) {
  return http.post('/messages/read-all', data)
}
