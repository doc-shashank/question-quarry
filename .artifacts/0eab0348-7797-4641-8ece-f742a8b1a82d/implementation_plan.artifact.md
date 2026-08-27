# Implementation Plan - Calendar View and Session Caching

Enhance QuestionQuarry with a Calendar view for browsing sessions, implement an LRU cache for sessions, and add a custom text selection menu in the editor.

## Proposed Changes

### Data Model & Persistence

#### [MODIFY] [Session.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/data/local/entity/Session.kt)
- Add `completionTime: Long = 0` field to `Session` data class.

#### [MODIFY] [BlockDao.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/data/local/dao/BlockDao.kt)
- Add `getSessionsByDateRange(start: Long, end: Long): Flow<List<Session>>` to query sessions within a specific timeframe.

#### [NEW] [SessionCache.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/data/cache/SessionCache.kt)
- Implement `SessionCache` using `LinkedHashMap` to store the 10 most recently accessed `Session` objects (LRU policy).

---

### Calendar Browser

#### [MODIFY] [Route.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/navigation/Route.kt)
- Add `Calendar` and `DailySessionList(date: Long)` routes.

#### [NEW] [CalendarScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/calendar/CalendarScreen.kt)
- Implement a calendar UI where dates are selectable.
- Use `ModalBottomSheet` for date actions ("View", "Test").
- "Test" will show a `Toast` placeholder.

#### [NEW] [DailySessionListScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/calendar/DailySessionListScreen.kt)
- Show a list of sessions for a specific day.
- Tapping a session navigates back to Home with the session selected (or opens a detail view).

#### [MODIFY] [MainActivity.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/MainActivity.kt)
- Add a `NavigationBar` to switch between `Home` and `Calendar`.
- Register new routes in `NavDisplay`.

---

### Loading Experience & Caching Integration

#### [MODIFY] [SessionViewModel.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/SessionViewModel.kt)
- Integrate `SessionCache`.
- Update `getSessionDetail` to check the cache first.
- Add a loading state to track data retrieval.

---

### Custom Editor Text Selection

#### [MODIFY] [TextEditorScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/session/TextEditorScreen.kt)
- Add a custom selection menu at the bottom of the screen when text is selected.
- Include Cut, Copy, Paste, Bold, Italics, and Underline actions.

## Verification Plan

### Automated Tests
- Build the project using `./gradlew assembleDebug`.
- Run unit tests (if applicable) for `SessionCache`.

### Manual Verification
- Navigate to the Calendar screen and verify date selection.
- Verify that selecting a date opens the bottom sheet.
- Verify session list for a day.
- Check that the custom selection menu appears in `TextEditorScreen` when text is selected.
- Verify caching by observing (via logs if needed) that session details are loaded faster/without Room hits for cached items.
