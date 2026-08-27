# Implementation Plan - Adaptive Canvas Editor

Develop a flexible `CanvasEditor` for QuestionQuarry that supports text and image blocks with adaptive UI.

## Proposed Changes

### Configuration

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/AndroidManifest.xml)
- Add `androidx.core.content.FileProvider` to support high-resolution camera capture.
- Add `CAMERA` permission.

#### [NEW] [file_paths.xml](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/res/xml/file_paths.xml)
- Define paths for `FileProvider` to access cache and internal files.

### UI Components

#### [NEW] [CanvasBlock.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/components/CanvasBlock.kt)
- Define `CanvasBlock` sealed class for local state management.

#### [NEW] [CanvasEditor.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/components/CanvasEditor.kt)
- Implement `CanvasEditor` composable.
- Implement `TextBlock` with curved boundaries.
- Implement `ImageBlock` with Gallery and Camera integration.
- Implement `AddBlockSection` for adding new elements.

### Screens

#### [MODIFY] [NewSessionScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/NewSessionScreen.kt)
- Integrate `CanvasEditor` into the session creation flow.
- Add state to manage Question and Answer blocks.
- Implement a two-step process: Question editing then Answer editing.

## Verification Plan

### Automated Tests
- Unit tests for `CanvasBlock` state transitions if applicable.
- Build the app to ensure no compilation errors.

### Manual Verification
- Verify that "Add Text" adds a text field.
- Verify that "Add Image" shows Gallery/Camera buttons.
- Verify that selecting an image from the gallery displays it and saves it via `StorageManager`.
- Verify that capturing an image from the camera displays it.
- Verify that blocks can be removed.
- Verify the UI aesthetics (curved boundaries, adaptive image sizing).
