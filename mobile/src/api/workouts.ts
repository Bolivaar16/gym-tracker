import { client } from './client';
import { WorkoutPage } from '../types/api';

export async function getWorkouts(page = 0, size = 10): Promise<WorkoutPage> {
  const { data } = await client.get<WorkoutPage>('/api/workouts', { params: { page, size } });
  return data;
}
