# Implementation Plan - Bug Fixes and Enhancements

This plan addresses the Calendar crash, preloading of calendar data, custom text selection toolbar in the editor, and the theme switching issue.

## Proposed Changes

### Theme & Styling
#### [MODIFY] [Theme.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/theme/Theme.kt)
- Update `QuestionQuarryTheme` to only use dynamic colors if `colorSchemeId` is `DEFAULT`. This allows the custom color schemes (Green, Red, Purple) to take effect when selected.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/MainActivity.kt)
- Ensure the theme is updated correctly when settings change.

### Calendar & Data Preloading
#### [MODIFY] [SessionViewModel.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/SessionViewModel.kt)
- Add `allSessionsByDate` StateFlow to store all sessions grouped by date (ignoring time).
- Populate this flow on initialization to "preload" calendar data.
- Add a helper function to check if a specific date has sessions.

#### [MODIFY] [CalendarScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/calendar/CalendarScreen.kt)
- Move `getDaysOfMonth` logic to be more efficient (memoized or in ViewModel).
- Observe `allSessionsByDate` from `SessionViewModel`.
- Add a small dot or indicator below the date number in the calendar grid if sessions exist for that date.
- Fix the crash by safely handling `selectedDate` and ensuring `ModalBottomSheet` usage follows best practices.
- Improve responsiveness by reducing recompositions in the grid.

### Custom Text Selection Toolbar
#### [NEW] [CustomTextToolbar.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/CustomTextToolbar.kt)
- Implement the `TextToolbar` interface.
- Manage state for toolbar visibility, position (Rect), and callbacks.

#### [MODIFY] [TextEditorScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/TextEditorScreen.kt)
- Remove the selection toolbar from the `bottomBar`.
- Implement a floating toolbar Composable that observes the `CustomTextToolbar` state.
- Use `LocalTextToolbar.provides` to inject the custom implementation.
- Wire up Bold, Italics, Underline, Cut, Copy, and Paste actions using `RichTextState` and `ClipboardManager`.
- Ensure the toolbar appears **BELOW** the selected text using the `Rect` provided by the system.

## Verification Plan

### Automated Tests
- None required (as per instructions).

### Manual Verification
- **Theme**: Open Settings, tap different color circles, and verify the app theme updates immediately.
- **Calendar**:
    - Verify that dates with sessions have indicators.
    - Tap multiple dates to ensure no crashes occur.
    - Navigate between months and verify responsiveness.
- **Text Editor**:
    - Select text in the editor.
    - Verify the custom toolbar appears below the selection.
    - Test Bold, Italics, Underline, Cut, Copy, and Paste functionality.
    - Verify the system toolbar is successfully replaced.
