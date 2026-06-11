import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { TemplateStackParamList } from '../../navigation/MainTabs';
import { colors } from '../../theme';

type Props = NativeStackScreenProps<TemplateStackParamList, 'WorkoutSession'>;

export default function WorkoutSessionScreen({ route }: Props) {
  const { workoutId, templateName } = route.params;
  return (
    <View style={styles.container}>
      <Ionicons name="barbell" size={48} color={colors.primary} />
      <Text style={styles.title}>Workout #{workoutId} started</Text>
      <Text style={styles.subtitle}>From template "{templateName}"</Text>
      <Text style={styles.note}>Live session logging arrives in Phase 1.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colors.bg, padding: 24 },
  title: { fontSize: 20, fontWeight: '700', color: colors.text, marginTop: 16 },
  subtitle: { fontSize: 15, color: colors.textMuted, marginTop: 4 },
  note: { fontSize: 13, color: colors.textMuted, marginTop: 24, fontStyle: 'italic' },
});
