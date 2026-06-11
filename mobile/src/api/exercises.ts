import { client } from './client';
import { Exercise, MuscleGroup } from '../types/api';

export async function getExercises(muscleGroup?: MuscleGroup): Promise<Exercise[]> {
  const { data } = await client.get<Exercise[]>('/api/exercises', {
    params: muscleGroup ? { muscleGroup } : {},
  });
  return data;
}
