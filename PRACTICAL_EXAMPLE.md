# Practical SMALI Reverse Engineering Example

Let's walk through fixing the **Progressive Gravity Bug** step by step.

## Step 1: Decompile the APK

```bash
# Decompile your APK
apktool d forensics_project.apk

# Navigate to the SMALI files
cd forensics_project/smali/com/example/forensics_project/
```

## Step 2: Locate the Bug in FlappyView.smali

Open `FlappyView.smali` and search for the `update` method:

```smali
.method private update(F)V
    .locals 3
    .param p1, "dt"    # F

    # ... existing code ...
    
    # Look for this pattern (the buggy gravity calculation):
    iget v0, p0, Lcom/example/forensics_project/FlappyView;->gravity:F
    iget v1, p0, Lcom/example/forensics_project/FlappyView;->gameTime:F
    const v2, 0x3dcccccd    # 0.1f constant
    mul-float v1, v1, v2    # gameTime * 0.1f
    add-float v0, v0, v1    # gravity + (gameTime * 0.1f)
    
    # ... rest of method ...
.end method
```

## Step 3: Identify the Exact SMALI Code

The buggy code will look something like this:

```smali
.method private update(F)V
    .locals 4
    .param p1, "dt"    # F

    # Game state check
    iget v0, p0, Lcom/example/forensics_project/FlappyView;->gameState:Lcom/example/forensics_project/FlappyView$GameState;
    sget-object v1, Lcom/example/forensics_project/FlappyView$GameState;->RUNNING:Lcom/example/forensics_project/FlappyView$GameState;
    if-eq v0, v1, :cond_0
    return-void

    :cond_0
    # Increment game time
    iget v0, p0, Lcom/example/forensics_project/FlappyView;->gameTime:F
    add-float v0, v0, p1
    iput v0, p0, Lcom/example/forensics_project/FlappyView;->gameTime:F

    # BUGGY GRAVITY CALCULATION - THIS IS WHAT WE NEED TO FIX
    iget v0, p0, Lcom/example/forensics_project/FlappyView;->gravity:F
    iget v1, p0, Lcom/example/forensics_project/FlappyView;->gameTime:F
    const v2, 0x3dcccccd    # 0.1f
    mul-float v1, v1, v2
    add-float v0, v0, v1
    # v0 now contains the buggy gravity value

    # Apply gravity to bird velocity
    iget v1, p0, Lcom/example/forensics_project/FlappyView;->birdVelocity:F
    add-float v1, v1, v0
    iput v1, p0, Lcom/example/forensics_project/FlappyView;->birdVelocity:F

    # ... rest of method ...
.end method
```

## Step 4: Fix the SMALI Code

Replace the buggy gravity calculation with the fixed version:

```smali
.method private update(F)V
    .locals 4
    .param p1, "dt"    # F

    # Game state check
    iget v0, p0, Lcom/example/forensics_project/FlappyView;->gameState:Lcom/example/forensics_project/FlappyView$GameState;
    sget-object v1, Lcom/example/forensics_project/FlappyView$GameState;->RUNNING:Lcom/example/forensics_project/FlappyView$GameState;
    if-eq v0, v1, :cond_0
    return-void

    :cond_0
    # Increment game time (keep this)
    iget v0, p0, Lcom/example/forensics_project/FlappyView;->gameTime:F
    add-float v0, v0, p1
    iput v0, p0, Lcom/example/forensics_project/FlappyView;->gameTime:F

    # FIXED GRAVITY CALCULATION - JUST USE ORIGINAL GRAVITY
    iget v0, p0, Lcom/example/forensics_project/FlappyView;->gravity:F
    # No more progressive increase!

    # Apply gravity to bird velocity
    iget v1, p0, Lcom/example/forensics_project/FlappyView;->birdVelocity:F
    add-float v1, v1, v0
    iput v1, p0, Lcom/example/forensics_project/FlappyView;->birdVelocity:F

    # ... rest of method ...
.end method
```

## Step 5: What We Changed

**Before (Buggy):**
```smali
iget v0, p0, Lcom/example/forensics_project/FlappyView;->gravity:F
iget v1, p0, Lcom/example/forensics_project/FlappyView;->gameTime:F
const v2, 0x3dcccccd    # 0.1f
mul-float v1, v1, v2    # gameTime * 0.1f
add-float v0, v0, v1    # gravity + (gameTime * 0.1f)
```

**After (Fixed):**
```smali
iget v0, p0, Lcom/example/forensics_project/FlappyView;->gravity:F
# That's it! No progressive increase
```

## Step 6: Recompile and Test

```bash
# Go back to the project root
cd ../../../

# Recompile the APK
apktool b forensics_project

# Sign the APK (you'll need a keystore)
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.keystore forensics_project/dist/forensics_project.apk alias_name

# Or use apksigner (recommended)
apksigner sign --ks my-release-key.keystore forensics_project/dist/forensics_project.apk

# Install and test
adb install forensics_project/dist/forensics_project.apk
```

## Step 7: Verify the Fix

1. **Launch the game**
2. **Play for more than 30 seconds**
3. **Verify**: Gravity should remain constant, not increase over time
4. **Expected behavior**: Game should remain playable indefinitely

## Key SMALI Concepts Used

1. **Field Access**: `iget v0, p0, Lcom/example/forensics_project/FlappyView;->gravity:F`
   - Gets the `gravity` field from `this` object into register `v0`

2. **Constants**: `const v2, 0x3dcccccd`
   - Loads the float constant 0.1f into register `v2`

3. **Arithmetic**: `mul-float v1, v1, v2`
   - Multiplies `v1` by `v2` and stores result in `v1`

4. **Register Management**: `.locals 4`
   - Declares 4 local registers (v0, v1, v2, v3)

## Common Mistakes to Avoid

1. **Wrong register count**: Make sure `.locals` matches the number of registers used
2. **Missing semicolons**: SMALI is case-sensitive and requires proper syntax
3. **Wrong field names**: Double-check the exact field names from the original code
4. **Incorrect constants**: Use a hex converter to verify float constants

## Tools to Help

1. **Hex Converter**: Convert float values to hex (0.1f = 0x3dcccccd)
2. **SMALI Syntax Checker**: Use online SMALI validators
3. **APKTool**: Essential for decompiling/recompiling
4. **JADX**: Alternative decompiler for reference

This example shows the complete process from identification to fix. Apply the same methodology to the other bugs!

