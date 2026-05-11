import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { clearAuth, getStoredSession } from '../auth/storage'

const NAV_ITEMS = [
  { to: '/dashboard', label: '대시보드' },
  { to: '/users', label: '유저 관리' },
  { to: '/mails', label: '메일' },
  { to: '/products', label: '상품' },
]

export default function Layout() {
  const navigate = useNavigate()
  const session = getStoredSession()

  const logout = () => {
    clearAuth()
    navigate('/login', { replace: true })
  }

  return (
    <div className="flex min-h-screen bg-slate-50 text-slate-800">
      <aside className="w-56 shrink-0 border-r border-slate-200 bg-white">
        <div className="px-6 py-5 text-lg font-semibold text-slate-900">GameOps</div>
        <nav className="px-3">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `block rounded px-3 py-2 text-sm ${
                  isActive
                    ? 'bg-slate-900 text-white'
                    : 'text-slate-600 hover:bg-slate-100'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex flex-1 flex-col">
        <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-3">
          <div className="text-sm text-slate-500">
            {session?.displayName} <span className="text-slate-300">·</span>{' '}
            <span className="font-mono text-xs">{session?.role}</span>
          </div>
          <button
            onClick={logout}
            className="rounded border border-slate-300 px-3 py-1 text-sm hover:bg-slate-100"
          >
            로그아웃
          </button>
        </header>
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
