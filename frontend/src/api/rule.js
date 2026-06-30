import http from './http'

export function listCategories() {
  return http.get('/rules/categories')
}

export function saveCategory(data, id) {
  return id ? http.put(`/rules/categories/${id}`, data) : http.post('/rules/categories', data)
}

export function listDispatchRules() {
  return http.get('/rules/dispatch')
}

export function saveDispatchRule(data, id) {
  return id ? http.put(`/rules/dispatch/${id}`, data) : http.post('/rules/dispatch', data)
}

export function listSlaRules() {
  return http.get('/rules/sla')
}

export function saveSlaRule(data, id) {
  return id ? http.put(`/rules/sla/${id}`, data) : http.post('/rules/sla', data)
}
