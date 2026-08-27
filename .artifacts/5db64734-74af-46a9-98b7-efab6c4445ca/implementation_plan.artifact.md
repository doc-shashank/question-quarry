# Fix Navigation Crash and UI Truncation

This plan addresses a crash in Navigation 3 due to an empty backstack and a UI issue where session titles are truncated in `HomeScreen.kt`.

## User Review Required

> [!IMPORTANT]
> The navigation fix involves adding safety checks to backstack manipulations. The UI fix involves adding `Modifier.weight(1f)` to ensure text components occupy available horizontal space.

## Proposed Changes

### [Navigation]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/MainActivity.kt)
- Add safety checks to `backStack.removeAt` calls in `NewSessionScreen` entry.
- Add `entryDecorators` to `NavDisplay` to ensure state is properly saved and ViewModels are correctly scoped.

### [UI]

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/home/HomeScreen.kt)
- Add `Modifier.weight(1f)` to the `Column` in `QuestionItem` and `SessionItem`.
- Set `maxLines = 1` and `overflow = TextOverflow.Ellipsis` for session titles to handle long text gracefully.

## Verification Plan

### Automated Tests
- Run `gradlew :app:assembleDebug` to ensure the project builds correctly.

### Manual Verification
- Verify that navigating back from `NewSessionScreen` doesn't crash the app.
- Verify that session titles in `HomeScreen.kt` are fully visible or properly ellipsized if they are long, and not truncated to a single letter.
