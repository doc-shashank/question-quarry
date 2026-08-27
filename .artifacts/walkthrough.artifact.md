# Walkthrough - Flashcard 'Test' Mode Implementation

I have implemented the core Flashcard-style 'Test' mode for QuestionQuarry, allowing users to review their learning material through an interactive testing interface.

## Changes

### Navigation
- **[Route.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/navigation/Route.kt)**: Added `Route.Test(sessionIds: List<Long>)` to support passing multiple sessions for a combined test.
- **[MainActivity.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/MainActivity.kt)**: Registered the `Test` route in the navigation backstack and provided the `TestViewModel`.

### Logic & State
- **[TestViewModel.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/test/TestViewModel.kt)**:
    - Implemented session data fetching and grouping into Q&A pairs.
    - Added a shuffling mechanism for the flashcard queue.
    - Built a coroutine-based stopwatch that tracks elapsed time in `MM:SS` format.
    - Managed flip state and navigation index within the test queue.

### User Interface
- **[TestScreen.kt](file:///C:/Users/BRIN/ControllableAppData/AndroidStudioProjects/QuestionQuarry/app/src/main/java/opensource/qwx/questionquarry/ui/test/TestScreen.kt)**:
    - **Flashcard Component**: A Material 3 Card with a smooth 180-degree Y-axis flip animation using `graphicsLayer`.
    - **Markdown Support**: Integrated the Markdown renderer for both front (Question) and back (Answer) of the card.
    - **Transitions**: Added a gentle fade-in effect for the answer content using `AnimatedVisibility`.
    - **Controls**: Large interaction buttons for toggling the card and advancing to the next question.
    - **Stopwatch**: Real-time timer displayed in the top bar.
    - **Summary View**: A "Test Complete" screen showing the total questions reviewed and the total time taken.

## Verification Results

### Automated Tests
- The project was built successfully using `./gradlew :app:assembleDebug`.

### Manual Verification Path
1. Launch the app and navigate to the `Test` route (e.g., by selecting sessions).
2. The `TestViewModel` fetches blocks, groups them by `pairIndex`, and shuffles them.
3. The `TestScreen` displays the first card with a stopwatch running.
4. Toggling "Show Answer" flips the card with a 3D rotation and fades in the answer.
5. "Next Question" advances the queue.
6. Reaching the end shows the "Test Complete" summary with the total time.
