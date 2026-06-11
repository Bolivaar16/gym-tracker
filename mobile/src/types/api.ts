export type MuscleGroup =
  | 'CHEST' | 'BACK' | 'SHOULDERS' | 'BICEPS' | 'TRICEPS' | 'FOREARMS'
  | 'QUADS' | 'HAMSTRINGS' | 'GLUTES' | 'CALVES' | 'CORE' | 'FULL_BODY';

export const MUSCLE_GROUPS: MuscleGroup[] = [
  'CHEST', 'BACK', 'SHOULDERS', 'BICEPS', 'TRICEPS', 'FOREARMS',
  'QUADS', 'HAMSTRINGS', 'GLUTES', 'CALVES', 'CORE', 'FULL_BODY',
];

export interface AuthResponse {
  access_token: string;
  token_type: string;
  expires_in: number;
}

export interface Exercise {
  id: number;
  name: string;
  muscleGroups: MuscleGroup[];
  defaultRestSeconds: number;
  notes?: string | null;
  archived: boolean;
}

export interface WorkoutSet {
  id: number;
  setNumber: number;
  reps: number;
  weightKg: number;
  rpe?: number | null;
  completedAt: string;
}

export interface WorkoutExercise {
  exercise: Exercise;
  position: number;
  sets: WorkoutSet[];
}

export interface Workout {
  id: number;
  startedAt: string;
  finishedAt?: string | null;
  notes?: string | null;
  exercises: WorkoutExercise[];
}

export interface WorkoutSummary {
  id: number;
  startedAt: string;
  finishedAt?: string | null;
  notes?: string | null;
  exerciseCount: number;
  totalSets: number;
  totalVolumeKg: number;
}

export interface WorkoutPage {
  content: WorkoutSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface TemplateExerciseItem {
  exercise: Exercise;
  position: number;
  targetSets: number;
  targetReps: number;
  targetWeightKg?: number | null;
}

export interface Template {
  id: number;
  name: string;
  notes?: string | null;
  createdAt: string;
  exercises: TemplateExerciseItem[];
}

export interface TemplateSummary {
  id: number;
  name: string;
  notes?: string | null;
  exerciseCount: number;
}

export interface StatsSummary {
  workoutsThisWeek: number;
  workoutsThisMonth: number;
  totalVolumeThisWeekKg: number;
  currentStreakDays: number;
}

export interface VolumePoint {
  period: string;
  volumeKg: number;
}

export interface CoachResponse {
  reply: string;
  contextSummary: string;
}
