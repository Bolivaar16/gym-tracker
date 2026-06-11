import axios from 'axios';
import * as SecureStore from 'expo-secure-store';

export const TOKEN_KEY = 'gym_tracker_jwt';

const API_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8080';

export const client = axios.create({
  baseURL: API_URL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

let onUnauthorized: (() => void) | null = null;
export function setOnUnauthorized(cb: (() => void) | null): void {
  onUnauthorized = cb;
}

client.interceptors.request.use(async (config) => {
  const token = await SecureStore.getItemAsync(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status: number | undefined = error.response?.status;
    const url: string = error.config?.url ?? '';
    if (status === 401 && !url.includes('/api/auth/token')) {
      await SecureStore.deleteItemAsync(TOKEN_KEY);
      onUnauthorized?.();
    }
    return Promise.reject(error);
  }
);
