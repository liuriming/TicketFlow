import http from './http'

export function pageOperationLogs(params) {
  return http.get('/audit/logs', { params })
}
