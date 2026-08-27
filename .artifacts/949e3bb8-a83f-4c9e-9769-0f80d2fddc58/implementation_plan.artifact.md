# Refine UI Header and Session Naming Flow

This plan outlines the changes to refine the `NewSessionScreen` header, ensure a mandatory naming step, and fix block deletion issues in `CanvasEditor`.

## Proposed Changes

### [Component] UI Session

#### [MODIFY] [NewSessionScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/NewSessionScreen.kt)
- Redesign the header using a custom `CenterAlignedTopAppBar` (or equivalent layout).
- Move the "Previous" button to the top-left (stylishly similar to "Next").
- Display "QUESTION" or "ANSWER" in the center (all caps, smaller font).
- Move the Pair Switcher from the bottom-left to directly under the center text in the header.
- Ensure the `SessionStep.NAME` screen is the mandatory first step, and canvas state is only interacted with after the "Start" button is clicked.
- Refine navigation logic for "Previous" to handle both step and pair transitions.

### [Component] UI Components

#### [MODIFY] [CanvasEditor.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/components/CanvasEditor.kt)
- Rename `onRemove` to `onDeleteBlock` for better alignment with requirements.
- Ensure the `onDeleteBlock` callback correctly filters the list and triggers recomposition.
- Verify `LazyColumn` keys are using `block.id` correctly.
- Optimize block removal to ensure UI updates immediately.

## Verification Plan

### Automated Tests
- Run existing unit tests (if any) to ensure no regressions in session logic.
- `./gradlew :app:assembleDebug` to verify compilation.

### Manual Verification
- Verify the mandatory naming step in the emulator.
- Verify the new header layout and Pair Switcher positioning.
- Test "Previous" and "Next" button navigation across multiple pairs and steps.
- Verify that deleting text and image blocks in the canvas works correctly and the items are removed immediately.
