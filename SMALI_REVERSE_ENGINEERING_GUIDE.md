# SMALI Reverse Engineering Guide - Bug Fixes

This guide shows you exactly how to reverse engineer and fix each bug using SMALI bytecode modification.

## Prerequisites

1. **Decompile APK**: Use APKTool to decompile the APK
   ```bash
   apktool d your_app.apk
   ```

2. **Locate SMALI files**:
   - `FlappyView.smali` in `smali/com/example/forensics_project/`
   - `LevelManager.smali` in `smali/com/example/forensics_project/`

## Bug Fix 1: Progressive Gravity Bug

### Current Buggy Code (Java):
```java
float currentGravity = gravity + (gameTime * 0.1f);
```

### SMALI Location:
Look for the `update` method in `FlappyView.smali`

### SMALI Fix:
**Find this pattern:**
```smali
# Current buggy SMALI (approximate)
iget v0, p0, Lcom/example/forensics_project/FlappyView;->gravity:F
iget v1, p0, Lcom/example/forensics_project/FlappyView;->gameTime:F
const v2, 0x3dcccccd    # 0.1f
mul-float v1, v1, v2
add-float v0, v0, v1
```

**Replace with:**
```smali
# Fixed SMALI - just use original gravity
iget v0, p0, Lcom/example/forensics_project/FlappyView;->gravity:F
```

### What to look for:
- Search for `0x3dcccccd` (0.1f constant)
- Look for `mul-float` and `add-float` instructions
- Remove the multiplication and addition, just use the original gravity value

---

## Bug Fix 2: Invisible Hitbox Bug

### Current Buggy Code (Java):
```java
float invisibleOffset = 20f;
// Applied to bird coordinates in collision detection
```

### SMALI Location:
Look for collision detection in the `update` method

### SMALI Fix:
**Find this pattern:**
```smali
# Current buggy SMALI (approximate)
const v0, 0x41a00000    # 20.0f
iget v1, p0, Lcom/example/forensics_project/FlappyView;->birdX:F
add-float v1, v1, v0    # birdX + offset
```

**Replace with:**
```smali
# Fixed SMALI - remove offset
iget v1, p0, Lcom/example/forensics_project/FlappyView;->birdX:F
# No offset added
```

### What to look for:
- Search for `0x41a00000` (20.0f constant)
- Look for `add-float` or `sub-float` with this constant
- Remove the offset calculations

---

## Bug Fix 3: Pipe Spacing Bug

### Current Buggy Code (Java):
```java
useBuggyGap = random.nextFloat() < 0.3f;
gapSizePx = useBuggyGap ? BUGGY_GAP_SIZE : GAP_SIZE_PX;
```

### SMALI Location:
Look for the pipe reset logic in the `update` method

### SMALI Fix:
**Find this pattern:**
```smali
# Current buggy SMALI (approximate)
const v0, 0x3e99999a    # 0.3f
# Random check and conditional assignment
const v1, 0x43480000    # 200.0f (BUGGY_GAP_SIZE)
const v2, 0x43e10000    # 450.0f (GAP_SIZE_PX)
```

**Replace with:**
```smali
# Fixed SMALI - always use normal gap size
const v1, 0x43e10000    # 450.0f (GAP_SIZE_PX)
iput v1, p0, Lcom/example/forensics_project/FlappyView;->gapSizePx:F
```

### What to look for:
- Search for `0x3e99999a` (0.3f probability)
- Look for conditional logic with `if-eq` or `if-ne`
- Replace with direct assignment of normal gap size

---

## Bug Fix 4: Score Multiplier Bug

### Current Buggy Code (Java):
```java
int scoreMultiplier = (score > 10) ? 3 : ((score > 5) ? 2 : 1);
score += scoreMultiplier;
```

### SMALI Location:
Look for scoring logic in the `update` method

### SMALI Fix:
**Find this pattern:**
```smali
# Current buggy SMALI (approximate)
iget v0, p0, Lcom/example/forensics_project/FlappyView;->score:I
const v1, 0xa    # 10
if-le v0, v1, :label1
const v2, 0x1    # multiplier = 1
goto :label3
:label1
const v1, 0x5    # 5
if-le v0, v1, :label2
const v2, 0x2    # multiplier = 2
goto :label3
:label2
const v2, 0x3    # multiplier = 3
:label3
add-int v0, v0, v2
iput v0, p0, Lcom/example/forensics_project/FlappyView;->score:I
```

**Replace with:**
```smali
# Fixed SMALI - always add 1
iget v0, p0, Lcom/example/forensics_project/FlappyView;->score:I
const v1, 0x1    # Always add 1
add-int v0, v0, v1
iput v0, p0, Lcom/example/forensics_project/FlappyView;->score:I
```

### What to look for:
- Search for score comparison logic with `if-le`
- Look for multiple `const` values (1, 2, 3)
- Replace with simple increment by 1

---

## Bug Fix 5: LevelManager Logic Bugs

### Bug 5a: Centipede Unlock Logic

### Current Buggy Code (Java):
```java
return level < 3; // Should be level >= 3
```

### SMALI Fix:
**Find this pattern:**
```smali
# Current buggy SMALI
iget v0, p1, Lcom/example/forensics_project/LevelManager;->level:I
const v1, 0x3
if-ge v0, v1, :label1
const v0, 0x1    # return true
return v0
:label1
const v0, 0x0    # return false
return v0
```

**Replace with:**
```smali
# Fixed SMALI
iget v0, p1, Lcom/example/forensics_project/LevelManager;->level:I
const v1, 0x3
if-lt v0, v1, :label1    # Changed from if-ge to if-lt
const v0, 0x1    # return true
return v0
:label1
const v0, 0x0    # return false
return v0
```

### Bug 5b: Asteroid Unlock Logic

### Current Buggy Code (Java):
```java
return level < 2; // Should be level >= 2
```

### SMALI Fix:
**Find this pattern:**
```smali
# Current buggy SMALI
iget v0, p1, Lcom/example/forensics_project/LevelManager;->level:I
const v1, 0x2
if-ge v0, v1, :label1
```

**Replace with:**
```smali
# Fixed SMALI
iget v0, p1, Lcom/example/forensics_project/LevelManager;->level:I
const v1, 0x2
if-lt v0, v1, :label1    # Changed from if-ge to if-lt
```

### Bug 5c: Level Increment Bug

### Current Buggy Code (Java):
```java
setCurrentLevel(context, currentLevel - 1); // Should be +1
```

### SMALI Fix:
**Find this pattern:**
```smali
# Current buggy SMALI
iget v0, p1, Lcom/example/forensics_project/LevelManager;->currentLevel:I
const v1, 0x1
sub-int v0, v0, v1    # Subtraction
```

**Replace with:**
```smali
# Fixed SMALI
iget v0, p1, Lcom/example/forensics_project/LevelManager;->currentLevel:I
const v1, 0x1
add-int v0, v0, v1    # Addition instead of subtraction
```

### Bug 5d: Level Validation Bug

### Current Buggy Code (Java):
```java
return level <= 0 || level > 10; // Should be level > 0 && level <= 10
```

### SMALI Fix:
**Find this pattern:**
```smali
# Current buggy SMALI
iget v0, p1, Lcom/example/forensics_project/LevelManager;->level:I
const v1, 0x0
if-le v0, v1, :label1    # level <= 0
const v0, 0x1
return v0
:label1
const v1, 0xa    # 10
if-le v0, v1, :label2    # level > 10
const v0, 0x1
return v0
:label2
const v0, 0x0
return v0
```

**Replace with:**
```smali
# Fixed SMALI
iget v0, p1, Lcom/example/forensics_project/LevelManager;->level:I
const v1, 0x0
if-gt v0, v1, :label1    # level > 0 (inverted)
const v0, 0x0
return v0
:label1
const v1, 0xa    # 10
if-gt v0, v1, :label2    # level <= 10 (inverted)
const v0, 0x0
return v0
:label2
const v0, 0x1
return v0
```

---

## Bug Fix 6: Memory Leak Bug

### Current Buggy Code (Java):
```java
// Missing bitmap.recycle() calls before creating new bitmaps
```

### SMALI Location:
Look for the `createScaledBitmaps` method

### SMALI Fix:
**Add before each bitmap creation:**
```smali
# Add this before creating new bitmaps
iget-object v0, p0, Lcom/example/forensics_project/FlappyView;->backgroundBitmapScaled:Landroid/graphics/Bitmap;
if-eqz v0, :skip_recycle1
invoke-virtual {v0}, Landroid/graphics/Bitmap;->recycle()V
:skip_recycle1
```

**Repeat for all bitmap fields:**
- `pipeBottomBitmapScaled`
- `pipeTopBitmapScaled` 
- `birdBitmapScaled`

---

## SMALI Instruction Reference

### Comparison Instructions:
- `if-eq`: if equal
- `if-ne`: if not equal
- `if-lt`: if less than
- `if-le`: if less than or equal
- `if-gt`: if greater than
- `if-ge`: if greater than or equal

### Arithmetic Instructions:
- `add-int`: integer addition
- `sub-int`: integer subtraction
- `mul-float`: float multiplication
- `add-float`: float addition
- `sub-float`: float subtraction

### Field Access:
- `iget`: get instance field
- `iput`: put instance field
- `sget`: get static field
- `sput`: put static field

## Testing Your Fixes

1. **Recompile**: `apktool b your_app_folder`
2. **Sign**: Use jarsigner or apksigner
3. **Install**: `adb install your_app.apk`
4. **Test**: Verify each bug is fixed

## Common SMALI Patterns to Search For

- **Float constants**: `0x3dcccccd` (0.1f), `0x41a00000` (20.0f)
- **Integer constants**: `0x1`, `0x2`, `0x3`, `0xa` (10)
- **Comparison jumps**: `if-ge`, `if-lt`, `if-le`, `if-gt`
- **Arithmetic**: `add-float`, `sub-int`, `mul-float`

---

## Bug Fix 7: Centipede Game Bugs (JavaScript/HTML)

### Current Buggy Code (JavaScript):
The Centipede game has 3 main bugs in the HTML/JavaScript:

1. **Player Movement Bug**: Movement is inverted
2. **Bullet Direction Bug**: Bullets go up instead of down  
3. **Centipede Speed Bug**: Speed increases with score instead of staying constant

### How to Fix (HTML/JavaScript Modification):

**Bug 7a: Player Movement Fix**
```javascript
// Current buggy code:
player.x -= dx * player.speed;  // Should be +=
player.y -= dy * player.speed;  // Should be +=

// Fixed code:
player.x += dx * player.speed;
player.y += dy * player.speed;
```

**Bug 7b: Bullet Direction Fix**
```javascript
// Current buggy code:
bullet.y += bullet.speed;  // Should be -=

// Fixed code:
bullet.y -= bullet.speed;
```

**Bug 7c: Centipede Speed Fix**
```javascript
// Current buggy code:
centipede.speed = 1 + (score * 0.1);  // Should be constant

// Fixed code:
centipede.speed = 1;  // Constant speed
```

### SMALI Approach (if you want to modify the HTML in SMALI):
Since the HTML is in assets, you would need to:
1. Decompile the APK
2. Modify the HTML file in `assets/centipede/index.html`
3. Recompile the APK

---

## Bug Fix 8: Asteroid Game Bugs (JavaScript/HTML)

### Current Buggy Code (JavaScript):
The Asteroid game has 3 main bugs:

1. **Ship Rotation Bug**: Ship rotates wrong direction
2. **Bullet Lifetime Bug**: Bullets disappear immediately
3. **Asteroid Movement Bug**: Asteroids move backwards

### How to Fix (HTML/JavaScript Modification):

**Bug 8a: Ship Rotation Fix**
```javascript
// Current buggy code:
ship.angle += direction * 0.1;  // Should be -=

// Fixed code:
ship.angle -= direction * 0.1;
```

**Bug 8b: Bullet Lifetime Fix**
```javascript
// Current buggy code:
if (bullet.lifetime > 0) {  // Should be <= 0
    bullets.splice(i, 1);
    continue;
}

// Fixed code:
if (bullet.lifetime <= 0) {
    bullets.splice(i, 1);
    continue;
}
```

**Bug 8c: Asteroid Movement Fix**
```javascript
// Current buggy code:
asteroid.x -= asteroid.velocity.x;  // Should be +=
asteroid.y -= asteroid.velocity.y;  // Should be +=

// Fixed code:
asteroid.x += asteroid.velocity.x;
asteroid.y += asteroid.velocity.y;
```

---

## Bug Fix 9: WebView Configuration Bugs

### Current Buggy Code (Java):
The WebView activities have configuration bugs:

1. **Centipede URL Bug**: Fixed - was `index_htmll` (typo)
2. **Asteroid JavaScript Bug**: Fixed - was `setJavaScriptEnabled(false)`

### SMALI Location:
Look for `WebCentipedeActivity.smali` and `WebAsteroidActivity.smali`

### SMALI Fix for JavaScript Bug:
**Find this pattern:**
```smali
# Current buggy SMALI
const/4 v0, 0x0    # false
invoke-virtual {v1, v0}, Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V
```

**Replace with:**
```smali
# Fixed SMALI
const/4 v0, 0x1    # true
invoke-virtual {v1, v0}, Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V
```

---

## Complete Bug Summary

### Flappy Bird Game (Java/SMALI):
1. ✅ Progressive Gravity Bug
2. ✅ Invisible Hitbox Bug  
3. ✅ Pipe Spacing Bug
4. ✅ Score Multiplier Bug
5. ✅ Memory Leak Bug

### Level Manager (Java/SMALI):
6. ✅ Level Logic Inversion Bugs (5 different bugs)

### Centipede Game (JavaScript/HTML):
7. ✅ Player Movement Bug
8. ✅ Bullet Direction Bug
9. ✅ Centipede Speed Bug

### Asteroid Game (JavaScript/HTML):
10. ✅ Ship Rotation Bug
11. ✅ Bullet Lifetime Bug
12. ✅ Asteroid Movement Bug

### WebView Configuration (Java/SMALI):
13. ✅ JavaScript Disabled Bug (Fixed)
14. ✅ URL Typo Bug (Fixed)

## Testing Your Fixes

### For Java/SMALI Bugs:
1. **Recompile**: `apktool b your_app_folder`
2. **Sign**: Use apksigner
3. **Install**: `adb install your_app.apk`
4. **Test**: Verify each bug is fixed

### For JavaScript/HTML Bugs:
1. **Modify HTML files** in `assets/` folder
2. **Recompile APK** with modified assets
3. **Test games** in WebView

This comprehensive guide covers all 14 bugs across Java, SMALI, and JavaScript code!
