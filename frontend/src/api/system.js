import http from './http'

export function pageUsers(params) {
  return http.get('/system/users', { params })
}

export function getUser(id) {
  return http.get(`/system/users/${id}`)
}

export function saveUser(data, id) {
  return id ? http.put(`/system/users/${id}`, data) : http.post('/system/users', data)
}

export function listRoles() {
  return http.get('/system/roles')
}

export function getRole(id) {
  return http.get(`/system/roles/${id}`)
}

export function saveRole(data, id) {
  return id ? http.put(`/system/roles/${id}`, data) : http.post('/system/roles', data)
}

export function listMenus() {
  return http.get('/system/menus')
}

export function getMenu(id) {
  return http.get(`/system/menus/${id}`)
}

export function saveMenu(data, id) {
  return id ? http.put(`/system/menus/${id}`, data) : http.post('/system/menus', data)
}

export function listDepts() {
  return http.get('/system/depts')
}

export function getDept(id) {
  return http.get(`/system/depts/${id}`)
}

export function saveDept(data, id) {
  return id ? http.put(`/system/depts/${id}`, data) : http.post('/system/depts', data)
}
