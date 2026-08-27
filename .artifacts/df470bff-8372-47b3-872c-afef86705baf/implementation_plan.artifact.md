# Fix Capture from Camera Crash

The "Capture from Camera" feature is crashing, likely due to memory issues (OOM) during image processing or potential `FileProvider` misconfiguration/misuse. This plan addresses both by centralizing URI creation in `StorageManager`, improving memory efficiency during image saving, and adding defensive checks and logging.

## User Review Required

> [!IMPORTANT]
> The image saving logic will now prefer copying the file directly from the URI rather than decoding it into a `Bitmap` and re-encoding it. This saves memory and prevents OOM crashes for high-resolution photos. If image processing (like resizing) is needed in the future, it should be done with proper downsampling.

## Proposed Changes

### [Component] Storage & Utilities

#### [MODIFY] [StorageManager.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/util/StorageManager.kt)
- Add `getAuthority()` to centralize the `FileProvider` authority.
- Add `createTempImageUri()` to safely create a URI for the camera.
- Refactor `saveImage(Uri)` to use `inputStream.copyTo(outputStream)` to avoid OOM.
- Catch `Throwable` instead of `Exception` to handle `OutOfMemoryError`.
- Add logging to track the saving process.

### [Component] UI Components

#### [MODIFY] [CanvasBlock.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/components/CanvasBlock.kt)
- Use `storageManager.createTempImageUri()` instead of manual URI creation.
- Add logging around camera launch and result handling.
- Ensure the `tempUriString` is handled safely.

### [Component] Resources & Configuration

#### [MODIFY] [file_paths.xml](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/res/xml/file_paths.xml)
- Change `path="."` to `path=""` for broader compatibility and clarity.

## Verification Plan

### Automated Tests
- N/A (UI and Camera interaction are hard to test in unit tests without extensive mocking).

### Manual Verification
1. Open the app and navigate to "New Session".
2. Add an Image block.
3. Tap "Capture from Camera".
4. Take a photo and confirm.
5. Verify the photo appears in the canvas without crashing.
6. Verify "Add from Gallery" still works.
