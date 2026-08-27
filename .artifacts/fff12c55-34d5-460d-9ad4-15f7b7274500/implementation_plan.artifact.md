# Camera, Editor, and UX Refinement Plan

This plan details the refinements for Camera capture, Drag-and-Drop cues, Rich Text Toolbar, and Navigation restrictions in QuestionQuarry.

## User Review Required

> [!IMPORTANT]
> The Rich Text Editor Toolbar will use HTML-like tags for formatting not natively supported by Markdown (like font size/color), as the current renderer supports them.

## Proposed Changes

### UI Components

#### [MODIFY] [CanvasEditor.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/components/CanvasEditor.kt)
- Add a green border to the block being dragged.
- Use `Modifier.border(2.dp, Color.Green, RoundedCornerShape(24.dp))` when `isDragging` is true.

#### [MODIFY] [CanvasBlock.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/components/CanvasBlock.kt)
- Ensure `ActivityResultLauncher` and related states are robustly handled at the top of `ImageBlockContent`.
- Double-check URI creation and permission handling.

### Session Screens

#### [MODIFY] [TextEditorScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/TextEditorScreen.kt)
- Add a horizontal scrollable toolbar below the header.
- Implement buttons for Bold, Italics, Underline, Font Size, Font Type, Foreground Color, and Highlight Color.
- Add a Delete button to remove the current text block.
- Update `text` state to `TextFieldValue` to support selection-based formatting.

#### [MODIFY] [SessionViewModel.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/SessionViewModel.kt)
- Add `deleteTextBlock` function to support the delete button in the toolbar.

### Home Screen

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/home/HomeScreen.kt)
- Ensure "New Session" is only initiated from here (already seems to be the case, but will verify).

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors: `./gradlew :app:assembleDebug`

### Manual Verification
- Verify Camera capture works.
- Verify green border appears during drag-and-drop in Canvas Editor.
- Verify Markdown formatting buttons in Text Editor work as expected.
- Verify Delete button in Text Editor works.
- Verify no other screens initiate a new session.
