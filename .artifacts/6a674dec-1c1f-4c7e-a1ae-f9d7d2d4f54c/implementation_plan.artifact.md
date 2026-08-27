# Implementation Plan - Overhaul Canvas Block UI and Navigation

Overhaul the Canvas Block UI and implement a new navigation flow for text editing. This includes adding sequential labeling for blocks, a non-editable preview for text blocks with a fade-out effect, and a new `TextEditor` route.

## User Review Required

> [!IMPORTANT]
> The `TextEditorScreen` will be created to handle the actual editing of text blocks. This screen will need to update the `NewSessionScreen`'s state or the ViewModel.

## Proposed Changes

### Navigation

#### [MODIFY] [Route.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/navigation/Route.kt)
- Add `TextEditor(pairIndex: Int, blockIndex: Int, isQuestion: Boolean)` to the `Route` sealed interface.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/MainActivity.kt)
- Add navigation entry for `Route.TextEditor`.

---

### UI Components

#### [MODIFY] [CanvasBlock.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/components/CanvasBlock.kt)
- (Task says update this file for Block UI) I will move/implement `BlockItem` here as requested by the task, or update it if I move it from `CanvasEditor.kt`.
- Implement labeling UI (e.g., "Text 1").
- Implement Text Preview with fade-out effect using `Brush.verticalGradient`.
- Add "Edit Text" button.
- Make the text display read-only.

#### [MODIFY] [CanvasEditor.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/components/CanvasEditor.kt)
- Update `CanvasEditor` to calculate sequential labels for blocks.
- Pass navigation callback to `BlockItem`.
- Update `BlockItem` usage to reflect changes in `CanvasBlock.kt`.

#### [NEW] [TextEditorScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/TextEditorScreen.kt)
- Create a dedicated screen for editing text block content.

---

### Logic & Navigation Trigger

#### [MODIFY] [NewSessionScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/NewSessionScreen.kt)
- Implement navigation to `TextEditor` when the "Edit Text" button is clicked.

## Verification Plan

### Automated Tests
- N/A (UI focused change, manual verification preferred)

### Manual Verification
1. Open the app and start a new session.
2. Add multiple Text and Image blocks.
3. Verify labels are sequential (e.g., "Text 1", "Text 2", "Image 1").
4. Verify Text blocks show "Edit Text" button.
5. Verify Text blocks with content show a fade-out effect and the "Edit Text" button.
6. Click "Edit Text" and verify it navigates to a new screen where text can be edited.
7. Verify that the edited text is reflected back in the canvas.
