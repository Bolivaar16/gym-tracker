INSERT INTO exercise (name, muscle_groups, default_rest_seconds, notes) VALUES
-- Chest
('Barbell Bench Press',         ARRAY['CHEST','TRICEPS','SHOULDERS'], 180, NULL),
('Dumbbell Bench Press',        ARRAY['CHEST','TRICEPS','SHOULDERS'], 120, NULL),
('Incline Barbell Bench Press', ARRAY['CHEST','TRICEPS','SHOULDERS'], 180, NULL),
('Incline Dumbbell Bench Press',ARRAY['CHEST','TRICEPS','SHOULDERS'], 120, NULL),
('Cable Fly',                   ARRAY['CHEST'],                       90,  NULL),
('Push-Up',                     ARRAY['CHEST','TRICEPS','SHOULDERS'],  60, NULL),

-- Back
('Barbell Deadlift',            ARRAY['BACK','HAMSTRINGS','GLUTES'],  240, NULL),
('Pull-Up',                     ARRAY['BACK','BICEPS'],               120, NULL),
('Lat Pulldown',                ARRAY['BACK','BICEPS'],               120, NULL),
('Seated Cable Row',            ARRAY['BACK','BICEPS'],               120, NULL),
('Barbell Bent-Over Row',       ARRAY['BACK','BICEPS'],               180, NULL),
('Dumbbell Single-Arm Row',     ARRAY['BACK'],                         90, NULL),
('Face Pull',                   ARRAY['BACK','SHOULDERS'],             60, NULL),

-- Shoulders
('Barbell Overhead Press',      ARRAY['SHOULDERS','TRICEPS'],         180, NULL),
('Dumbbell Shoulder Press',     ARRAY['SHOULDERS','TRICEPS'],         120, NULL),
('Lateral Raise',               ARRAY['SHOULDERS'],                    60, NULL),
('Front Raise',                 ARRAY['SHOULDERS'],                    60, NULL),
('Reverse Fly',                 ARRAY['SHOULDERS','BACK'],             60, NULL),

-- Arms
('Barbell Curl',                ARRAY['BICEPS'],                       90, NULL),
('Dumbbell Curl',               ARRAY['BICEPS'],                       60, NULL),
('Hammer Curl',                 ARRAY['BICEPS','FOREARMS'],            60, NULL),
('Preacher Curl',               ARRAY['BICEPS'],                       90, NULL),
('Triceps Pushdown',            ARRAY['TRICEPS'],                      60, NULL),
('Skull Crusher',               ARRAY['TRICEPS'],                      90, NULL),
('Triceps Overhead Extension',  ARRAY['TRICEPS'],                      60, NULL),
('Wrist Curl',                  ARRAY['FOREARMS'],                     45, NULL),

-- Legs
('Barbell Back Squat',          ARRAY['QUADS','HAMSTRINGS','GLUTES'], 240, NULL),
('Barbell Front Squat',         ARRAY['QUADS','GLUTES'],              240, NULL),
('Romanian Deadlift',           ARRAY['HAMSTRINGS','GLUTES'],         180, NULL),
('Leg Press',                   ARRAY['QUADS','GLUTES'],              180, NULL),
('Leg Extension',               ARRAY['QUADS'],                        90, NULL),
('Leg Curl',                    ARRAY['HAMSTRINGS'],                   90, NULL),
('Hip Thrust',                  ARRAY['GLUTES','HAMSTRINGS'],         120, NULL),
('Bulgarian Split Squat',       ARRAY['QUADS','GLUTES'],              120, NULL),
('Lunge',                       ARRAY['QUADS','GLUTES'],               90, NULL),
('Standing Calf Raise',         ARRAY['CALVES'],                       60, NULL),
('Seated Calf Raise',           ARRAY['CALVES'],                       60, NULL),

-- Core
('Plank',                       ARRAY['CORE'],                         60, 'Weight in kg = duration in minutes'),
('Ab Wheel Rollout',            ARRAY['CORE'],                         90, NULL),
('Cable Crunch',                ARRAY['CORE'],                         60, NULL),
('Hanging Leg Raise',           ARRAY['CORE'],                         60, NULL),

-- Full body
('Power Clean',                 ARRAY['FULL_BODY'],                   240, NULL),
('Farmers Carry',               ARRAY['FULL_BODY','FOREARMS'],        120, 'Weight = per hand in kg');
