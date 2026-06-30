import http from './http'

export function getReportOverview() {
  return http.get('/reports/overview')
}

export function getDashboardSummary() {
  return http.get('/reports/dashboard')
}

export function getCategoryDistribution() {
  return http.get('/reports/category-distribution')
}

export function getWorkload() {
  return http.get('/reports/workload')
}
