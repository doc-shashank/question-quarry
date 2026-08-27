# Implementation Plan - Fix Camera and UI Restructuring

This plan addresses the non-functional camera issue, restricts the "New Session" button to the appropriate screens, and ensures stability for the session editing flow.

## Proposed Changes

### UI Components

#### [MODIFY] [CanvasBlock.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/components/CanvasBlock.kt)
- Refactor `ImageBlockContent` to handle `ActivityResultLauncher` more robustly.
- Access `tempUriString` directly within the `onResult` callback to ensure the latest URI is used after process recreation.
- Clear `tempUriString` after a successful capture to avoid stale data.
- Add additional logging for debugging camera results.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/home/HomeScreen.kt)
- Move the "New Session" Floating Action Button (FAB) from the top-level `Scaffold` into the `listPane` of the `NavigableListDetailPaneScaffold`.
- This ensures the FAB is only visible when the list of sessions is active and not when viewing session details on compact screens.
- On expanded screens (tablets), the FAB will be anchored to the list pane, following adaptive design best practices.

### Storage & Manifest

#### [MODIFY] [StorageManager.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/util/StorageManager.kt)
- Ensure directory creation in `createTempImageUri` is robust.
- Add more logging for URI creation.

## Verification Plan

### Automated Tests
- N/A (UI and hardware integration focused)

### Manual Verification
- **Camera Fix**:
    1. Open a new session.
    2. Add an Image block.
    3. Click "Capture from Camera".
    4. Grant permission if prompted.
    5. Take a photo and confirm.
    6. Verify the photo appears in the canvas.
- **Button Restriction**:
    1. On a phone, verify the "New Session" FAB is visible on the Home screen.
    2. Click a session to view details.
    3. Verify the "New Session" FAB is NO LONGER visible.
    4. Verify the "Edit" button in the top bar works correctly.
- **Edit Stability**:
    1. Click "Edit" on a session.
    2. Verify it opens the canvas with existing content and NOT a naming prompt.
