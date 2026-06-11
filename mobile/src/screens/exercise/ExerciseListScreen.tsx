import React, { useMemo, useState } from 'react';
import {
  ActivityIndicator, FlatList, ScrollView, StyleSheet, Text, TextInput, View,
} from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { getExercises } from '../../api/exercises';
import { Exercise, MUSCLE_GROUPS, MuscleGroup } from '../../types/api';
import ExerciseChip from '../../components/ExerciseChip';
import { colors } from '../../theme';

function pretty(group: MuscleGroup): string {
  return group.replace('_', ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
}

export default function ExerciseListScreen() {
  const [group, setGroup] = useState<MuscleGroup | null>(null);
  const [search, setSearch] = useState('');

  const { data, isPending, isError } = useQuery({
    queryKey: ['exercises', group],
    queryFn: () => getExercises(group ?? undefined),
  });

  const filtered = useMemo(
    () => (data ?? []).filter((e) => e.name.toLowerCase().includes(search.trim().toLowerCase())),
    [data, search]
  );

  return (
    <View style={styles.container}>
      <TextInput
        style={styles.search} placeholder="Search exercises..."
        value={search} onChangeText={setSearch} autoCorrect={false}
      />
      <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.chips}>
        <ExerciseChip label="All" selected={group === null} onPress={() => setGroup(null)} />
        {MUSCLE_GROUPS.map((g) => (
          <ExerciseChip key={g} label={pretty(g)} selected={group === g} onPress={() => setGroup(g)} />
        ))}
      </ScrollView>
      {isPending ? (
        <ActivityIndicator size="large" color={colors.primary} style={{ marginTop: 48 }} />
      ) : isError ? (
        <Text style={styles.error}>Could not load exercises.</Text>
      ) : (
        <FlatList
          contentContainerStyle={styles.list}
          data={filtered}
          keyExtractor={(e: Exercise) => String(e.id)}
          renderItem={({ item }) => (
            <View style={styles.card}>
              <Text style={styles.name}>{item.name}</Text>
              <Text style={styles.groups}>{item.muscleGroups.map(pretty).join(' · ')}</Text>
            </View>
          )}
          ListEmptyComponent={<Text style={styles.empty}>No exercises match.</Text>}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg },
  search: {
    margin: 16, marginBottom: 8, backgroundColor: colors.card, borderRadius: 10,
    borderWidth: 1, borderColor: colors.border, padding: 12, fontSize: 15,
  },
  chips: { paddingLeft: 16, flexGrow: 0, marginBottom: 8 },
  list: { padding: 16, paddingTop: 8, gap: 10 },
  card: {
    backgroundColor: colors.card, borderRadius: 12, padding: 14,
    borderWidth: 1, borderColor: colors.border,
  },
  name: { fontSize: 15, fontWeight: '600', color: colors.text },
  groups: { fontSize: 12, color: colors.textMuted, marginTop: 4 },
  empty: { textAlign: 'center', color: colors.textMuted, marginTop: 48 },
  error: { color: colors.error, textAlign: 'center', marginTop: 48 },
});
