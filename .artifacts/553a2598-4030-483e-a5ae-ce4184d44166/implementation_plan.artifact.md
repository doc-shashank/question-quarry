# Implementation Plan - Fix Library Incompatibility and UI Crashes

This plan addresses the critical crash issue where the "New Session" button and "Calendar Date" button cause crashes due to library version mismatches. We will stabilize the project dependencies and update the UI components to use stable Material 3 APIs.

## User Review Required

> [!IMPORTANT]
> I am downgrading the futuristic/placeholder versions (`2026.08.00`, `9.3.1`, `2.4.10`) to the latest stable Android versions to ensure binary compatibility. This will trigger a Gradle sync.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/gradle/libs.versions.toml)
- Revert `agp` to `8.7.3` (Stable).
- Revert `kotlin` to `2.1.0` (Stable).
- Set `composeBom` to `2025.01.01` (Stable).
- Pin `material3` to `1.3.1` explicitly to ensure both `AlertDialog` and `ModalBottomSheet` use stable signatures.
- Update `activityCompose` and `lifecycle` versions to match stable targets.

### UI Components

#### [MODIFY] [CalendarScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/calendar/CalendarScreen.kt)
- Ensure `ModalBottomSheet` usage matches the stable 1.3.1 API.
- Update `sheetState.hide()` logic to safely dismiss the sheet using `currentValue` instead of the potentially inconsistent `isVisible`.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/home/HomeScreen.kt)
- Verify `AlertDialog` parameters match the stable API.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify the build succeeds with stable versions.

### Manual Verification
- Deploy to the device/emulator.
- **Test 1**: Click the "New Session" FAB on the Home screen. Verify the naming dialog appears without crashing.
- **Test 2**: Navigate to the Calendar. Click a date with sessions. Verify the `ModalBottomSheet` appears without crashing.
- **Test 3**: In the Calendar sheet, click "View". Verify it navigates to the daily list correctly.
