# Implement Flashcard-style 'Test' Mode

Implement a core flashcard testing experience where users can review questions and answers from multiple sessions with a shuffle mechanism, flip animations, and a stopwatch.

## User Review Required

> [!NOTE]
> The 'Test' mode will be accessible via navigation, but for this task, we are only implementing the destination screen and logic. Triggering the navigation (e.g., from Home or Session Detail) might be part of a separate task or I can add a placeholder button if appropriate. I will assume the `sessionIds` are passed correctly.

## Proposed Changes

### Navigation

#### [MODIFY] [Route.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/navigation/Route.kt)
- Add `@Serializable data class Test(val sessionIds: List<Long>) : Route`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/MainActivity.kt)
- Add navigation entry for `Route.Test`.
- Instantiate `TestViewModel` using `db.blockDao()`.

### Test Feature

#### [NEW] [TestViewModel.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/test/TestViewModel.kt)
- Fetches blocks from `BlockDao` for the provided `sessionIds`.
- Groups blocks by `pairIndex` and `sessionId`.
- Shuffles the pairs.
- Manages `currentIndex` and `isFlipped`.
- Implements a stopwatch using `Flow` or `delay`.

#### [NEW] [TestScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/test/TestScreen.kt)
- UI for the Flashcard test.
- `Flashcard` component with Y-axis rotation animation.
- Fade-in transition for the back of the card.
- "Show Answer" and "Next Question" buttons.
- "Test Complete" state when the queue is finished.

## Verification Plan

### Automated Tests
- N/A (Manual verification is more suitable for animations).

### Manual Verification
- Navigate to Test mode (can be done via a temporary button or deep link if needed, but I'll focus on the screen implementation).
- Verify cards shuffle correctly.
- Verify flip animation is smooth.
- Verify stopwatch starts and updates.
- Verify "Next Question" advances or shows completion.
