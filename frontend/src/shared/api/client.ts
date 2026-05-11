import axios, { AxiosError } from 'axios'
import { clearAuth, getAccessToken } from '../auth/storage'

export interface ApiError {
  code: string
  message: string
}

export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: ApiError | null
}

export const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10_000,
})

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response?.status === 401) {
      clearAuth()
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

export function extractApiError(error: unknown): ApiError {
  const fallback: ApiError = { code: 'E_UNKNOWN', message: '알 수 없는 오류가 발생했습니다.' }
  if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
    const body = error.response?.data
    if (body?.error) return body.error
    if (error.message) return { code: 'E_NETWORK', message: error.message }
  }
  return fallback
}
