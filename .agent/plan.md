# Project Plan

QuestionQuarry: SQ3R study app.
New Feature: Flashcard-style 'Test' mode with randomization, stopwatch, and flip animations, accessible from the calendar.

## Project Brief

# Project Brief: QuestionQuarry

**QuestionQuarry** is a specialized study application built on the SQ3R (Survey, Question, Read, Recite, Review) methodology. The app helps users transform their study materials into active recall sessions, now featuring an interactive flashcard-based testing environment accessible directly from their study calendar.

## Features

*   **SQ3R Study Workflow**: Integrated support for guided study sessions following the SQ3R method, allowing users to organize their learning progress via a central calendar interface.
*   **Flashcard-Style 'Test' Mode**: A dedicated testing environment launched from calendar date pop-ups, enabling quick reviews of 'all sessions' or 'particular sessions' from that day.
*   **Animated Active Recall**: Interactive flashcards with randomized queuing and fluid flip animations (Question <-> Answer) featuring gentle fade-in transitions for the answer text.
*   **Timed Learning Sessions**: A built-in stopwatch within the testing window to help users track their recall speed and monitor mastery levels over time.
*   **Adaptive Testing UI**: A clean, distraction-free interface that scales perfectly across devices, featuring clear 'Show Answer' toggles and 'Next Question' navigation.

## High-Level Technical Stack

*   **Kotlin**: Primary language for robust and expressive application logic.
*   **Jetpack Compose**: The modern declarative UI toolkit used for the flashcard interface, interactive components, and custom flip animations.
*   **Jetpack Navigation 3**: A state-driven navigation architecture where the backstack is managed as a developer-owned list of keys, ensuring predictable screen transitions and easier testing.
*   **Compose Material 3 Adaptive**: Utilization of canonical layouts (such as `ListDetailPaneScaffold`) and `NavigationSuiteScaffold` to ensure the app adapts seamlessly to phones, foldables, and large-screen devices.
*   **Kotlin Coroutines**: Essential for managing asynchronous tasks such as the test session stopwatch and randomized question queue processing.

> [!NOTE]
> This MVP focuses on the core study loop and the new testing functionality without local persistence, prioritizing a state-driven architecture for rapid iteration.

## Implementation Steps

### Task_30_RefineTestUI: Refine the Test Type Selection UI and the Flashcard layout. Enlarge the flashcards, add 'QUESTION' and 'ANSWER' labels to the cards, and ensure a clean, modern design.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Test Type Selection UI is refined and modern
  - Flashcards are larger and take up appropriate screen space
  - 'QUESTION' and 'ANSWER' labels are visible on the respective sides of the flashcard
  - UI matches Material 3 Expressive guidelines
- **StartTime:** 2026-08-21 01:09:49 IST

