# Implementation Plan - Fix New Session Crash and Refine Calendar UI

This plan addresses the crash occurring when starting a new session and refines the Calendar UI components as requested.

## User Review Required

> [!NOTE]
> The "New Session" crash investigation suggests it might be related to how session loading is handled during navigation or restoration. I will move the loading logic to be more robust.

## Proposed Changes

### Session Logic & Data

#### [MODIFY] [SessionViewModel.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/SessionViewModel.kt)
- Add `getSessionCountOnDate(dateMillis: Long): Int` to efficiently retrieve the number of sessions for a specific day.
- Ensure `hasSessionsOnDate` uses the same efficient lookup.

### Calendar UI Refinement

#### [MODIFY] [CalendarScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/calendar/CalendarScreen.kt)
- **Date Box Layout**:
    - Increase the aspect ratio (making boxes taller).
    - Position the **Date Number** in the **top-right** corner.
    - Position the **Number of Sessions** in the **bottom-left** corner (smaller font, only if > 0).
- **Bottom Sheet**:
    - Add the text line: "YOU HAVE DONE [X] OF SESSIONS" below the action buttons.
    - Style: Small font, all capitalized, gray color.

### "New Session" Crash Fix

#### [MODIFY] [MainActivity.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/MainActivity.kt)
- Pass the `sessionId` from `Route.NewSession` into `NewSessionScreen`.
- Ensure `loadSessionForEditing` is handled properly (moving it into the screen or ensuring it's called reliably).

#### [MODIFY] [NewSessionScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/NewSessionScreen.kt)
- Update `NewSessionScreen` to accept `sessionId: Long?`.
- Add a `LaunchedEffect` to call `viewModel.loadSessionForEditing(sessionId)` if `sessionId` is not null and not already being edited. This ensures that session data is loaded even after process death/restoration.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors: `./gradlew :app:assembleDebug`

### Manual Verification
- Verify the Calendar grid layout matches the new design (Date in TR, Count in BL).
- Verify the Bottom Sheet displays the correct session count message.
- Verify that tapping "New Session" and entering a name correctly navigates to `NewSessionScreen` without crashing.
- Verify that editing an existing session also works correctly.
