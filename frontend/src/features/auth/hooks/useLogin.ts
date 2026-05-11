import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { login, type LoginPayload, type LoginResult } from '../api/login'
import { saveAuth } from '../../../shared/auth/storage'

export function useLogin() {
  const navigate = useNavigate()
  return useMutation<LoginResult, Error, LoginPayload>({
    mutationFn: login,
    onSuccess: (result) => {
      saveAuth(result)
      navigate('/dashboard', { replace: true })
    },
  })
}
