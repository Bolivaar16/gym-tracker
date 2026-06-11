import { client } from './client';
import { CoachResponse } from '../types/api';

export async function askCoach(message: string): Promise<CoachResponse> {
  const { data } = await client.post<CoachResponse>('/api/coach/ask', {
    message,
    includeRecentWorkouts: true,
    includePersonalRecords: true,
  });
  return data;
}
