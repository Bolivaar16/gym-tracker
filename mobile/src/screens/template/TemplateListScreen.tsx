import React from 'react';
import {
  ActivityIndicator, Alert, FlatList, StyleSheet, Text, TouchableOpacity, View,
} from 'react-native';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { getTemplates, startWorkout } from '../../api/templates';
import { TemplateSummary } from '../../types/api';
import { TemplateStackParamList } from '../../navigation/MainTabs';
import { colors } from '../../theme';

type Props = NativeStackScreenProps<TemplateStackParamList, 'TemplateList'>;

export default function TemplateListScreen({ navigation }: Props) {
  const queryClient = useQueryClient();
  const { data, isPending, isError } = useQuery({
    queryKey: ['templates'],
    queryFn: getTemplates,
  });

  const startMutation = useMutation({
    mutationFn: (template: TemplateSummary) => startWorkout(template.id),
    onSuccess: (workout, template) => {
      queryClient.invalidateQueries({ queryKey: ['workouts'] });
      navigation.navigate('WorkoutSession', { workoutId: workout.id, templateName: template.name });
    },
    onError: () => Alert.alert('Error', 'Could not start the workout. Is the backend running?'),
  });

  if (isPending) {
    return <View style={styles.center}><ActivityIndicator size="large" color={colors.primary} /></View>;
  }
  if (isError) {
    return <View style={styles.center}><Text style={styles.error}>Could not load templates.</Text></View>;
  }

  return (
    <FlatList
      style={{ backgroundColor: colors.bg }}
      contentContainerStyle={styles.list}
      data={data}
      keyExtractor={(t) => String(t.id)}
      renderItem={({ item }) => (
        <View style={styles.card}>
          <View style={{ flex: 1 }}>
            <Text style={styles.name}>{item.name}</Text>
            <Text style={styles.meta}>{item.exerciseCount} exercises</Text>
          </View>
          <TouchableOpacity
            style={styles.startButton}
            disabled={startMutation.isPending}
            onPress={() => startMutation.mutate(item)}
          >
            {startMutation.isPending && startMutation.variables?.id === item.id
              ? <ActivityIndicator color="#FFFFFF" size="small" />
              : <Text style={styles.startText}>Start Workout</Text>}
          </TouchableOpacity>
        </View>
      )}
      ListEmptyComponent={<Text style={styles.empty}>No templates yet.</Text>}
    />
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colors.bg },
  list: { padding: 16, gap: 12 },
  card: {
    backgroundColor: colors.card, borderRadius: 12, padding: 16, flexDirection: 'row',
    alignItems: 'center', borderWidth: 1, borderColor: colors.border,
  },
  name: { fontSize: 16, fontWeight: '700', color: colors.text },
  meta: { fontSize: 13, color: colors.textMuted, marginTop: 4 },
  startButton: {
    backgroundColor: colors.primary, borderRadius: 8, paddingVertical: 10,
    paddingHorizontal: 14, minWidth: 110, alignItems: 'center',
  },
  startText: { color: '#FFFFFF', fontWeight: '700', fontSize: 13 },
  empty: { textAlign: 'center', color: colors.textMuted, marginTop: 48 },
  error: { color: colors.error },
});
