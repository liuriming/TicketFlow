import http from './http'

export function getReportOverview() {
  return http.get('/reports/overview')
}

export function getWorkload() {
  return http.get('/reports/workload')
}
