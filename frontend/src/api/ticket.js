import http from './http'

export function pageTickets(params) {
  return http.get('/tickets', { params })
}

export function getTicketDetail(id) {
  return http.get(`/tickets/${id}`)
}

export function createTicket(data) {
  return http.post('/tickets', data)
}

export function acceptTicket(id) {
  return http.post(`/tickets/${id}/accept`)
}

export function processTicket(id, result) {
  return http.post(`/tickets/${id}/process`, { result })
}

export function transferTicket(id, data) {
  return http.post(`/tickets/${id}/transfer`, data)
}

export function confirmCloseTicket(id, remark) {
  return http.post(`/tickets/${id}/confirm-close`, { remark })
}

export function rejectTicket(id, remark) {
  return http.post(`/tickets/${id}/reject`, { remark })
}

export function cancelTicket(id, remark) {
  return http.post(`/tickets/${id}/cancel`, { remark })
}

export function addTicketComment(id, data) {
  return http.post(`/tickets/${id}/comments`, data)
}
