# Refine TextEditorScreen and Fix Stage Persistence

This plan outlines the changes to `TextEditorScreen` to simplify the header, implement individual block saving, add a back-gesture save prompt, and fix a bug where the session stage is lost when returning from the editor.

## User Review Required

> [!IMPORTANT]
> The "Switch Block" functionality and "Previous/Next" navigation in the `TextEditorScreen` will be removed to align with the requirement that each block be saved individually. The screen will focus solely on the block selected from the canvas.

## Proposed Changes

### Session UI Component

#### [MODIFY] [TextEditorScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/TextEditorScreen.kt)
- Remove "Previous", "Next" buttons and "Switch Block" dropdown from the header.
- Add a "Finish" button as the primary action in the header.
- Implement `BackHandler` to intercept back gestures.
- Add a "Save Changes?" confirmation dialog (Save, Discard, Cancel).
- Compare current text with initial text to detect changes.

#### [MODIFY] [NewSessionScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/NewSessionScreen.kt)
- Use `rememberSaveable` for `currentStep` and `currentPairIndex` to ensure the stage and pair index are maintained when returning from the `TextEditorScreen` or during configuration changes.

#### [MODIFY] [SessionViewModel.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/SessionViewModel.kt)
- (Optional) Verify `updateTextBlock` logic, though it seems correct for individual saving.

## Verification Plan

### Manual Verification
1. Open a new session.
2. Add a text block to the Question stage.
3. Edit the text block.
4. Verify the header only has a "Finish" button and a back button.
5. Make changes and click "Finish". Verify the block is updated on the canvas.
6. Switch to the Answer stage.
7. Edit a text block in the Answer stage.
8. Make changes and press the system back button.
9. Verify the "Save Changes?" dialog appears.
10. Select "Save" and verify the change is saved and you return to the Answer stage (not reset to Question).
11. Repeat with "Discard" and verify changes are not saved.
12. Verify the stage (Question/Answer) is correctly maintained after returning from the editor.
