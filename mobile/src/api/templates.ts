import { client } from './client';
import { TemplateSummary, Workout } from '../types/api';

export async function getTemplates(): Promise<TemplateSummary[]> {
  const { data } = await client.get<TemplateSummary[]>('/api/templates');
  return data;
}

export async function startWorkout(templateId: number): Promise<Workout> {
  const { data } = await client.post<Workout>(`/api/templates/${templateId}/start`);
  return data;
}
