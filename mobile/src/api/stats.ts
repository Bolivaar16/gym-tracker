import { client } from './client';
import { StatsSummary, VolumePoint } from '../types/api';

export async function getSummary(): Promise<StatsSummary> {
  const { data } = await client.get<StatsSummary>('/api/stats/summary');
  return data;
}

export async function getWeeklyVolume(weeksBack = 8): Promise<VolumePoint[]> {
  const from = new Date();
  from.setDate(from.getDate() - weeksBack * 7);
  const { data } = await client.get<VolumePoint[]>('/api/stats/volume', {
    params: { groupBy: 'week', from: from.toISOString().slice(0, 10) },
  });
  return data.slice(-weeksBack);
}
