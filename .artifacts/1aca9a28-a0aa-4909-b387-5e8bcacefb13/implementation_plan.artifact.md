# Implementation Plan - Fix ModalBottomSheet and Deprecated APIs

This plan addresses the `NoSuchMethodError` for `ModalBottomSheet` and cleans up deprecated Compose/Material 3 APIs across the project.

## User Review Required

> [!IMPORTANT]
> I will be aligning the Material 3 version to a stable release (1.3.1) and ensuring all Compose libraries follow the Compose BOM version to prevent binary incompatibilities like `NoSuchMethodError`.
> I will also migrate the "New Session" naming dialog in `HomeScreen.kt` to a `ModalBottomSheet` as it aligns better with the reported crash context and modern Material 3 design guidelines.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/gradle/libs.versions.toml)
- Update `material3` version to `1.3.1`.
- Update `composeBom` to a stable 2024/2025 version if `2026.08.00` is causing issues, or simply ensure `androidx-compose-material3` follows the BOM.
- **Action**: Remove the explicit version from `androidx-compose-material3` to let the BOM manage it.

---

### UI Components & Screens

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/home/HomeScreen.kt)
- Replace the `AlertDialog` used for session naming with a `ModalBottomSheet`.
- Use `rememberModalBottomSheetState` and `ModalBottomSheet` correctly.
- Fix the `NoSuchMethodError` by using the correct signature for the aligned Material 3 version.

#### [MODIFY] [NewSessionScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/NewSessionScreen.kt)
- Replace deprecated `LinearProgressIndicator(progress = Float)` with the lambda-based `progress = { Float }`.
- Ensure other components like `Scaffold` and `TopAppBar` are using non-deprecated overloads.

#### [MODIFY] [CalendarScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/calendar/CalendarScreen.kt)
- Ensure `ModalBottomSheet` signature is up to date and compatible.
- Check for any other deprecated M3 usages.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure compilation success.
- Run `./gradlew :app:testDebugUnitTest` to ensure no regressions in logic.

### Manual Verification
- Verify the "New Session" button in `HomeScreen` opens a Bottom Sheet instead of a crash.
- Verify the `LinearProgressIndicator` in `NewSessionScreen` displays correctly.
- Verify the Calendar Bottom Sheet works without issues.
