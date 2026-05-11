import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { getStoredSession, type AdminRole } from '../auth/storage'

interface Props {
  requiredRole?: AdminRole | AdminRole[]
  children: ReactNode
}

export default function ProtectedRoute({ requiredRole, children }: Props) {
  const location = useLocation()
  const session = getStoredSession()

  if (!session) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (requiredRole) {
    const allowed = Array.isArray(requiredRole)
      ? requiredRole.includes(session.role)
      : requiredRole === session.role
    if (!allowed) {
      return <Navigate to="/dashboard" replace />
    }
  }

  return <>{children}</>
}
