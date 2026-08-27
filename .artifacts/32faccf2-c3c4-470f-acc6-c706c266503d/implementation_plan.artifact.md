# Implementation Plan - Fix Critical Crashes

This plan addresses two critical crashes reported during final verification: a `FileProvider` configuration error causing camera crashes and a Room database integrity issue.

## Proposed Changes

### 1. Fix Camera Crash (FileProvider Configuration)

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/AndroidManifest.xml)
- Correct the meta-data name for `FileProvider` from `android.support.FILE_PATHS` to `android.support.FILE_PROVIDER_PATHS`.

---

### 2. Fix Database Integrity Crash (Room Versioning)

#### [MODIFY] [QuestionQuarryDatabase.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/data/local/QuestionQuarryDatabase.kt)
- Increment the database version from `2` to `6` to trigger a schema update.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/MainActivity.kt)
- Fix the `fallbackToDestructiveMigration()` call by removing the `true` argument, ensuring it uses the standard parameterless method.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project still builds correctly.
- (Optional) Run any available tests if they cover database or storage.

### Manual Verification
- The user will verify the fixes on the device to confirm the crashes no longer occur.
