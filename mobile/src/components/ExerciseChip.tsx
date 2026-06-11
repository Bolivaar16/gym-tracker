import React from 'react';
import { Pressable, StyleSheet, Text } from 'react-native';
import { colors } from '../theme';

interface Props {
  label: string;
  selected: boolean;
  onPress: () => void;
}

export default function ExerciseChip({ label, selected, onPress }: Props) {
  return (
    <Pressable
      onPress={onPress}
      style={[styles.chip, selected && styles.chipSelected]}
    >
      <Text style={[styles.text, selected && styles.textSelected]}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  chip: {
    paddingHorizontal: 14, paddingVertical: 7, borderRadius: 16, marginRight: 8,
    backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border,
  },
  chipSelected: { backgroundColor: colors.primary, borderColor: colors.primary },
  text: { fontSize: 13, color: colors.text },
  textSelected: { color: '#FFFFFF', fontWeight: '600' },
});
