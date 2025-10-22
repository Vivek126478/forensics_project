# Android Forensics Project - Bug Summary

This document outlines all the intentional bugs added to the Android forensics project for reverse engineering practice.

## Fixed Issues

### 1. Flappy Bird Scoring Bug (FIXED)
**Location**: `FlappyView.java` lines 206-216
**Issue**: Bird could score points even when going up/down through pipes
**Fix**: Added gap validation to ensure bird is actually in the safe zone between pipes before scoring

## Intentional Bugs Added

### 1. Progressive Gravity Bug
**Location**: `FlappyView.java` lines 169-174
**Description**: Gravity increases over time, making the game progressively harder and eventually impossible
**Code**: `float currentGravity = gravity + (gameTime * 0.1f);`
**Impact**: Game becomes unplayable after ~30 seconds

### 2. Invisible Hitbox Bug
**Location**: `FlappyView.java` lines 218-225
**Description**: Collision detection uses offset coordinates, creating invisible hitboxes
**Code**: `float invisibleOffset = 20f;` applied to bird coordinates
**Impact**: Unpredictable collision detection, bird can appear to pass through pipes

### 3. Pipe Spacing Bug
**Location**: `FlappyView.java` lines 150-163, 200-203
**Description**: Randomly creates impossible gaps (too small or at edges)
**Code**: 30% chance of buggy gap generation with `BUGGY_GAP_SIZE = 200f`
**Impact**: Some pipes become impossible to pass through

### 4. Score Multiplier Bug
**Location**: `FlappyView.java` lines 211-214
**Description**: Incorrect score calculation with wrong multipliers
**Code**: `score += scoreMultiplier` where multiplier increases with score
**Impact**: Score increases faster than expected (1, 2, 3 points per pipe)

### 5. LevelManager Bugs
**Location**: `LevelManager.java` lines 21-52
**Description**: Multiple logic inversion bugs in level management
**Bugs**:
- `isCentipedeUnlocked()`: Returns `level < 3` instead of `level >= 3`
- `isAsteroidUnlocked()`: Returns `level < 2` instead of `level >= 2`
- `incrementLevel()`: Decrements level instead of incrementing
- `isValidLevel()`: Inverted validation logic
- `meetsLevelRequirement()`: Uses `!=` instead of `>=`

### 6. Memory Leak Bug
**Location**: `FlappyView.java` lines 117-145
**Description**: Bitmaps are never recycled before creating new ones
**Code**: Missing `bitmap.recycle()` calls before `Bitmap.createScaledBitmap()`
**Impact**: Memory usage grows continuously, eventually causing OutOfMemoryError

## SMALI Reverse Engineering Opportunities

These bugs provide excellent opportunities for SMALI code analysis and modification:

1. **Gravity Bug**: Modify the gravity calculation in SMALI to fix the progressive increase
2. **Hitbox Bug**: Adjust the invisible offset values in SMALI bytecode
3. **Pipe Spacing**: Change the gap size constants and probability values
4. **Score Multiplier**: Fix the score calculation logic in SMALI
5. **Level Logic**: Correct the inverted boolean logic in LevelManager SMALI
6. **Memory Management**: Add bitmap recycling calls in SMALI

## How to Use for Forensics Training

1. **Build the APK** with these bugs
2. **Decompile** using tools like APKTool or JADX
3. **Analyze** the SMALI code to understand the bug implementations
4. **Modify** the SMALI bytecode to fix the bugs
5. **Recompile** and test the fixes
6. **Compare** original vs modified behavior

## Expected SMALI Modifications

- Change comparison operators (`<` to `>=`, `!=` to `>=`)
- Modify arithmetic operations (subtraction to addition)
- Adjust constant values (gravity, gap sizes, offsets)
- Fix boolean logic inversions
- Add method calls for bitmap recycling

This provides comprehensive practice with SMALI bytecode modification for Android reverse engineering.
