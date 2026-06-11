import React from 'react';
import {
  ActivityIndicator, Alert, FlatList, StyleSheet, Text, TouchableOpacity, View,
} from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { getWorkouts } from '../../api/workouts';
import { WorkoutSummary } from '../../types/api';
import { colors } from '../../theme';

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString(undefined, {
    weekday: 'short', day: 'numeric', month: 'short',
  });
}

function WorkoutCard({ item }: { item: WorkoutSummary }) {
  return (
    <TouchableOpacity
      style={styles.card}
      onPress={() => Alert.alert('Coming soon', 'Workout detail not yet implemented')}
    >
      <Text style={styles.date}>{formatDate(item.startedAt)}</Text>
      <View style={styles.row}>
        <Text style={styles.meta}>{item.exerciseCount} exercises</Text>
        <Text style={styles.meta}>{item.totalSets} sets</Text>
        <Text style={styles.volume}>{Math.round(item.totalVolumeKg)} kg</Text>
      </View>
      {item.notes ? <Text style={styles.notes} numberOfLines={1}>{item.notes}</Text> : null}
    </TouchableOpacity>
  );
}

export default function WorkoutListScreen() {
  const { data, isPending, isError, refetch, isRefetching } = useQuery({
    queryKey: ['workouts', 0],
    queryFn: () => getWorkouts(0, 10),
  });

  if (isPending) {
    return <View style={styles.center}><ActivityIndicator size="large" color={colors.primary} /></View>;
  }
  if (isError) {
    return <View style={styles.center}><Text style={styles.error}>Could not load workouts.</Text></View>;
  }

  return (
    <FlatList
      style={{ backgroundColor: colors.bg }}
      contentContainerStyle={styles.list}
      data={data.content}
      keyExtractor={(w) => String(w.id)}
      renderItem={({ item }) => <WorkoutCard item={item} />}
      refreshing={isRefetching}
      onRefresh={refetch}
      ListEmptyComponent={<Text style={styles.empty}>No workouts yet. Start one from a template.</Text>}
    />
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colors.bg },
  list: { padding: 16, gap: 12 },
  card: {
    backgroundColor: colors.card, borderRadius: 12, padding: 16,
    borderWidth: 1, borderColor: colors.border,
  },
  date: { fontSize: 16, fontWeight: '700', color: colors.text },
  row: { flexDirection: 'row', gap: 16, marginTop: 8 },
  meta: { fontSize: 13, color: colors.textMuted },
  volume: { fontSize: 13, fontWeight: '700', color: colors.primary, marginLeft: 'auto' },
  notes: { fontSize: 13, color: colors.textMuted, marginTop: 6, fontStyle: 'italic' },
  empty: { textAlign: 'center', color: colors.textMuted, marginTop: 48 },
  error: { color: colors.error },
});
