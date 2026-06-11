import React, {
  createContext, useCallback, useContext, useEffect, useMemo, useState,
} from 'react';
import { setOnUnauthorized } from '../api/client';
import { clearToken, getToken, login } from '../api/auth';

interface AuthContextValue {
  token: string | null;
  isLoading: boolean;
  signIn: (username: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getToken()
      .then(setToken)
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => {
    setOnUnauthorized(() => setToken(null));
    return () => setOnUnauthorized(null);
  }, []);

  const signIn = useCallback(async (username: string, password: string) => {
    const newToken = await login(username, password);
    setToken(newToken);
  }, []);

  const signOut = useCallback(async () => {
    await clearToken();
    setToken(null);
  }, []);

  const value = useMemo(
    () => ({ token, isLoading, signIn, signOut }),
    [token, isLoading, signIn, signOut]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
