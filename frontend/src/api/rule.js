import http from './http'

export function listCategories() {
  return http.get('/rules/categories')
}

export function saveCategory(data, id) {
  return id ? http.put(`/rules/categories/${id}`, data) : http.post('/rules/categories', data)
}

export function updateCategoryEnabled(id, enabled) {
  return http.put(`/rules/categories/${id}/enabled`, { enabled })
}

export function listDispatchRules() {
  return http.get('/rules/dispatch')
}

export function saveDispatchRule(data, id) {
  return id ? http.put(`/rules/dispatch/${id}`, data) : http.post('/rules/dispatch', data)
}

export function updateDispatchRuleEnabled(id, enabled) {
  return http.put(`/rules/dispatch/${id}/enabled`, { enabled })
}

export function listSlaRules() {
  return http.get('/rules/sla')
}

export function saveSlaRule(data, id) {
  return id ? http.put(`/rules/sla/${id}`, data) : http.post('/rules/sla', data)
}

export function updateSlaRuleEnabled(id, enabled) {
  return http.put(`/rules/sla/${id}/enabled`, { enabled })
}
