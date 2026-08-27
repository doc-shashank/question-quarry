# Implementation Plan - Refine Dedicated Text Editor Screen

This plan details the overhaul of the `TextEditorScreen` in `QuestionQuarry` to improve the editing experience and navigation between text blocks.

## User Review Required

> [!IMPORTANT]
> The navigation between blocks will happen within the `TextEditorScreen` state. The "Back" button will return to the `NewSessionScreen`.

## Proposed Changes

### UI & Navigation Components

#### [MODIFY] [TextEditorScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/TextEditorScreen.kt)
- **State Management**:
    - Introduce `currentBlockIndex` as a `mutableIntStateOf(blockIndex)`.
    - Introduce `text` as `mutableStateOf("")`, updated whenever `currentBlockIndex` changes.
    - Track the list of text-only blocks for the current stage (`isQuestion` for the given `pairIndex`).
- **Header Overhaul**:
    - Implement a `CenterAlignedTopAppBar`.
    - **Top-Left**: "Previous" button (icon + text "Previous") that decrements `currentBlockIndex` among text blocks.
    - **Top-Right**: "Next" button (text "Next" + icon) that increments `currentBlockIndex` among text blocks.
    - **Center**: "TEXT BLOCK [N]" label.
    - **Switcher**: A `Surface` with `DropdownMenu` below the center label to pick specific text blocks.
- **Editing Area**:
    - Replace `OutlinedTextField` with a full-screen `BasicTextField` or a highly customized `TextField` without a visible border, focused by default.
    - Use `WindowInsets` to handle the keyboard properly.
- **Save Logic**:
    - Auto-save current text to ViewModel when switching blocks? Or just save everything on "Finish". The requirement says "update the editor's content to that block's text" when switching. I'll save the current content to the ViewModel before switching to the new block to ensure no data is lost.
    - **Finish Action**: An action button in the top bar to save the final changes and return to the canvas.

### Logic & Data

#### [MODIFY] [SessionViewModel.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/SessionViewModel.kt)
- Ensure `updateTextBlock` correctly handles updates using the stable `id` if possible, though `blockIndex` is currently used and seems sufficient if the list order is stable.
- *Note*: The requirement mentions "stable `id` system". `CanvasBlock` has an `id`. I might update `updateTextBlock` to take `blockId` instead of `blockIndex` to be safer.

## Verification Plan

### Automated Tests
- N/A (Unit tests for `SessionViewModel` could be added but focus is on UI/Navigation).

### Manual Verification
1. Open a new session.
2. Add multiple text blocks to a question.
3. Tap on a text block to enter the editor.
4. Verify the header shows "Previous", "Next", and the switcher.
5. Verify "Previous"/"Next" navigates only between text blocks.
6. Verify the switcher lists only text blocks.
7. Verify changes are saved when switching blocks.
8. Verify "Finish" returns to the `NewSessionScreen` with all changes persisted.
9. Verify the `TextField` is focused and full-screen.
