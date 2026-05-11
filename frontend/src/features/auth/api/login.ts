import { apiClient, type ApiResponse } from '../../../shared/api/client'
import type { AdminRole } from '../../../shared/auth/storage'

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  role: AdminRole
  displayName: string
}

export async function login(payload: LoginPayload): Promise<LoginResult> {
  const { data } = await apiClient.post<ApiResponse<LoginResult>>('/auth/login', payload)
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '로그인에 실패했습니다.')
  }
  return data.data
}
