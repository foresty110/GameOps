import { getStoredSession } from '../shared/auth/storage'

export default function DashboardPage() {
  const session = getStoredSession()

  return (
    <div className="space-y-3">
      <h1 className="text-2xl font-semibold text-slate-900">대시보드</h1>
      <p className="text-slate-600">
        Hello, <span className="font-medium">{session?.displayName}</span>{' '}
        <span className="text-slate-400">({session?.role})</span>
      </p>
      <p className="text-sm text-slate-500">
        Day 3에서 DAU/MAU/매출/리텐션 차트가 이 자리에 들어옵니다.
      </p>
    </div>
  )
}
