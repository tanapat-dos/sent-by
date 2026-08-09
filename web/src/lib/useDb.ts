import { useEffect, useState } from 'react'
import { getState, subscribe, type DbState } from './db'

export function useDb(): DbState {
  const [state, setState] = useState(getState)
  useEffect(() => subscribe(() => setState(getState())), [])
  return state
}
