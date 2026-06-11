import React from 'react';
import { ActivityIndicator, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { BarChart } from 'react-native-gifted-charts';
import { getSummary, getWeeklyVolume } from '../../api/stats';
import MetricCard from '../../components/MetricCard';
import { colors } from '../../theme';

export default function StatsScreen() {
  const summary = useQuery({ queryKey: ['stats', 'summary'], queryFn: getSummary });
  const volume = useQuery({ queryKey: ['stats', 'volume', 'week'], queryFn: () => getWeeklyVolume(8) });

  if (summary.isPending || volume.isPending) {
    return <View style={styles.center}><ActivityIndicator size="large" color={colors.primary} /></View>;
  }
  if (summary.isError || volume.isError) {
    return <View style={styles.center}><Text style={styles.error}>Could not load stats.</Text></View>;
  }

  const s = summary.data;
  const bars = volume.data.map((p) => ({
    value: Math.round(p.volumeKg),
    label: p.period.split('-W')[1] ?? p.period,
    frontColor: colors.primary,
  }));

  return (
    <ScrollView style={{ backgroundColor: colors.bg }} contentContainerStyle={styles.content}>
      <View style={styles.grid}>
        <MetricCard label="Workouts this week" value={String(s.workoutsThisWeek)} />
        <MetricCard label="Workouts this month" value={String(s.workoutsThisMonth)} />
        <MetricCard label="Volume this week" value={`${Math.round(s.totalVolumeThisWeekKg)} kg`} />
        <MetricCard label="Current streak" value={`${s.currentStreakDays} days`} />
      </View>
      <Text style={styles.chartTitle}>Weekly volume (kg, last 8 weeks)</Text>
      <View style={styles.chartCard}>
        {bars.length === 0 ? (
          <Text style={styles.empty}>No volume data yet.</Text>
        ) : (
          <BarChart
            data={bars}
            barWidth={22}
            spacing={14}
            barBorderRadius={4}
            yAxisThickness={0}
            xAxisThickness={0}
            noOfSections={4}
            yAxisTextStyle={{ color: colors.textMuted, fontSize: 11 }}
            xAxisLabelTextStyle={{ color: colors.textMuted, fontSize: 11 }}
          />
        )}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colors.bg },
  content: { padding: 16 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12 },
  chartTitle: { fontSize: 15, fontWeight: '700', color: colors.text, marginTop: 24, marginBottom: 12 },
  chartCard: {
    backgroundColor: colors.card, borderRadius: 12, padding: 16,
    borderWidth: 1, borderColor: colors.border,
  },
  empty: { color: colors.textMuted, textAlign: 'center', padding: 24 },
  error: { color: colors.error },
});
