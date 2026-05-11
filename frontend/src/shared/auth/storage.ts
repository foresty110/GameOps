export type AdminRole = 'SUPER_ADMIN' | 'GM' | 'CS' | 'VIEWER'

const ACCESS_KEY = 'gameops.accessToken'
const REFRESH_KEY = 'gameops.refreshToken'
const ROLE_KEY = 'gameops.role'
const DISPLAY_NAME_KEY = 'gameops.displayName'

export interface StoredSession {
  accessToken: string
  refreshToken: string
  role: AdminRole
  displayName: string
}

export function saveAuth(session: StoredSession): void {
  localStorage.setItem(ACCESS_KEY, session.accessToken)
  localStorage.setItem(REFRESH_KEY, session.refreshToken)
  localStorage.setItem(ROLE_KEY, session.role)
  localStorage.setItem(DISPLAY_NAME_KEY, session.displayName)
}

export function clearAuth(): void {
  localStorage.removeItem(ACCESS_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(ROLE_KEY)
  localStorage.removeItem(DISPLAY_NAME_KEY)
}

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_KEY)
}

export function getStoredSession(): StoredSession | null {
  const accessToken = localStorage.getItem(ACCESS_KEY)
  const refreshToken = localStorage.getItem(REFRESH_KEY)
  const role = localStorage.getItem(ROLE_KEY) as AdminRole | null
  const displayName = localStorage.getItem(DISPLAY_NAME_KEY)
  if (!accessToken || !refreshToken || !role || !displayName) return null
  return { accessToken, refreshToken, role, displayName }
}
