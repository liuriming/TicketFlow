import http from './http'

export function listAttachments(params) {
  return http.get('/attachments', { params })
}

export function uploadAttachment(file, businessType, businessId) {
  const form = new FormData()
  form.append('file', file)
  if (businessType) form.append('businessType', businessType)
  if (businessId) form.append('businessId', businessId)
  return http.post('/attachments/upload', form)
}

export function downloadAttachment(id) {
  return http.get(`/attachments/${id}/download`, { responseType: 'blob' })
}

export function deleteAttachment(id) {
  return http.delete(`/attachments/${id}`)
}
