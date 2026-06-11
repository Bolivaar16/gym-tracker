import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import WorkoutListScreen from '../screens/workout/WorkoutListScreen';
import ExerciseListScreen from '../screens/exercise/ExerciseListScreen';
import TemplateListScreen from '../screens/template/TemplateListScreen';
import WorkoutSessionScreen from '../screens/template/WorkoutSessionScreen';
import StatsScreen from '../screens/stats/StatsScreen';
import CoachScreen from '../screens/coach/CoachScreen';
import { colors } from '../theme';

export type TemplateStackParamList = {
  TemplateList: undefined;
  WorkoutSession: { workoutId: number; templateName: string };
};

const Tab = createBottomTabNavigator();
const TemplateStack = createNativeStackNavigator<TemplateStackParamList>();

function TemplatesNavigator() {
  return (
    <TemplateStack.Navigator>
      <TemplateStack.Screen
        name="TemplateList" component={TemplateListScreen} options={{ title: 'Templates' }} />
      <TemplateStack.Screen
        name="WorkoutSession" component={WorkoutSessionScreen} options={{ title: 'Workout Session' }} />
    </TemplateStack.Navigator>
  );
}

const ICONS: Record<string, keyof typeof Ionicons.glyphMap> = {
  Workout: 'barbell-outline',
  Exercises: 'list-outline',
  Templates: 'copy-outline',
  Stats: 'stats-chart-outline',
  Coach: 'chatbubble-ellipses-outline',
};

export default function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textMuted,
        tabBarIcon: ({ color, size }) => (
          <Ionicons name={ICONS[route.name] ?? 'ellipse-outline'} size={size} color={color} />
        ),
      })}
    >
      <Tab.Screen name="Workout" component={WorkoutListScreen} />
      <Tab.Screen name="Exercises" component={ExerciseListScreen} />
      <Tab.Screen name="Templates" component={TemplatesNavigator} options={{ headerShown: false }} />
      <Tab.Screen name="Stats" component={StatsScreen} />
      <Tab.Screen name="Coach" component={CoachScreen} />
    </Tab.Navigator>
  );
}
