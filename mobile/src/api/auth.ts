import * as SecureStore from 'expo-secure-store';
import { client, TOKEN_KEY } from './client';
import { AuthResponse } from '../types/api';

export async function login(username: string, password: string): Promise<string> {
  const { data } = await client.post<AuthResponse>('/api/auth/token', { username, password });
  await SecureStore.setItemAsync(TOKEN_KEY, data.access_token);
  return data.access_token;
}

export function getToken(): Promise<string | null> {
  return SecureStore.getItemAsync(TOKEN_KEY);
}

export function clearToken(): Promise<void> {
  return SecureStore.deleteItemAsync(TOKEN_KEY);
}
