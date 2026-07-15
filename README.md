# BookScout

> **Discover your next great read.**

BookScout is a real-world Android book-discovery app built with Kotlin and Jetpack
Compose. It searches the live [Open Library](https://openlibrary.org) API, lets you browse
results as cover cards, open a rich details screen, and stays calm when the network, the
data, or the user misbehaves.

---

## Features

- **Search** by title, author, or free-text keyword (dropdown selector).
- **Results grid** of responsive cover cards (`LazyVerticalGrid`, `GridCells.Adaptive`).
- **Details screen** with cover, authors, publication date, subjects, description, ISBN,
  language, edition count, and an e-book availability badge.
- **Load more / pagination** — manual "Load more" button plus automatic loading as you
  scroll near the end.
- **Loading, empty, success and error states**, each with a friendly message.
- **Retry** on failure.
- **Recent searches** (persisted) with one-tap reuse and clear-all.
- **Settings** — theme (System / Light / Dark), default sort order
  (Relevance / Newest / Oldest / Title), and a "covers only" display filter.
- **Resilient data handling**:
  - Missing cover / author / year / subject / ISBN / description never crashes.
  - No internet, timeout, HTTP error, and malformed JSON are all caught and surfaced.
  - Repeated searches and rapid tapping are guarded.
- **Open in Open Library** and **Share** intents on the details screen.
- **Animated shimmer placeholders** while loading.
- **Splash screen** and an **adaptive app icon** with a custom brand identity.

---

## Screenshots

> Add emulator/device screenshots to `docs/screenshots/` and reference them here.
> Suggested captures: Search (idle), Search results grid, Book details, Settings,
> Error state, Empty state.

| Search | Results | Details | Settings |
|--------|---------|---------|----------|
| `docs/screenshots/search.png` | `docs/screenshots/results.png` | `docs/screenshots/details.png` | `docs/screenshots/settings.png` |

---

## Architecture

BookScout follows a clean, unidirectional MVVM architecture.

```
UI (Compose)                 ViewModel                   Repository                Remote / Local
-------------                ----------                   ----------                ----------------
SearchScreen  ──events──▶   SearchViewModel  ──▶         BookRepository  ──▶        OpenLibraryApi (Retrofit + OkHttp)
DetailsScreen ──events──▶   DetailsViewModel ──▶        (maps DTO → domain)        Open Library REST API
SettingsScreen ──events──▶  SettingsViewModel ──▶       UserSettingsRepository ──▶ DataStore (Preferences)
```

- **Data layer**
  - `OpenLibraryApi` — Retrofit service for `search.json` and `works/{id}.json`.
  - `BookRepository` — wraps the API, maps DTOs → domain models, and converts every
    failure into a typed `ApiResult` (`Success` / `Failure` with an `ErrorType`).
  - `UserSettingsRepository` — Jetpack DataStore for theme, sort, "covers only" and
    recent searches.
  - `NetworkModule` — builds OkHttp (with connect/read/write timeouts) and Retrofit
    with **Kotlin Serialization** configured to be lenient and ignore unknown keys.
- **Domain layer** — framework-agnostic models (`BookSummary`, `BookDetails`) and enums
  (`SearchField`, `SortOrder`, `ThemeMode`).
- **UI layer**
  - `MainActivity` + `AppNavHost` (Jetpack Navigation Compose).
  - One ViewModel per screen, exposing an immutable `StateFlow` UI state.
  - Reusable components: `BookCoverCard`, `ShimmerBox`, `EmptyState`, `ErrorState`.
- **Dependency injection** — a small manual DI container (`AppContainer`) created in
  `BookScoutApplication` (no Hilt, to keep the build simple).

### Error handling matrix

| Scenario | Behaviour |
|----------|-----------|
| Empty search field | Search is ignored; screen stays in the idle state. |
| No books found | `EMPTY` state with a helpful message. |
| No internet | `ErrorType.NETWORK` → retry screen. |
| Request timeout | `ErrorType.TIMEOUT` → retry screen. |
| API error (non-2xx) | `ErrorType.HTTP` → retry screen. |
| Unparseable response | `ErrorType.SERIALIZATION` → retry screen. |
| Incomplete data | Nullable fields degrade gracefully (placeholder cover, "Unknown title", etc.). |
| Book without a cover | A stylised book placeholder is shown instead of the image. |
| Repeated searches | Loading state / in-flight guard prevents duplicate requests. |
| Loading more | A footer spinner; the existing list stays visible. |

---

## Open Library acknowledgement

This product uses the **Open Library** API and cover images
(<https://openlibrary.org>), an open, editable library catalog. Book metadata and cover
art are © their respective rights holders and are used here under Open Library's terms.

---

## How to run

### Requirements
- Android Studio (latest stable, e.g. Hedgehog / Iguana or newer)
- JDK 17
- An Android device or emulator running API 24+ (Android 7.0+)

### Steps
1. Clone the repository.
2. Open the project in Android Studio. On first import, Android Studio generates the
   Gradle wrapper (`gradlew`, `gradle-wrapper.jar`) automatically.
3. Let Gradle sync and download dependencies.
4. Select an emulator/device and click **Run** (▶) or build an APK:
   ```
   ./gradlew assembleDebug
   ```

### Configuration
- `minSdk = 24`, `targetSdk = 34`, `compileSdk = 34`.
- Network requests go to `https://openlibrary.org/` (see `util/Constants.kt`).

---

## Known limitations

- **Cover images** are served by Open Library's Covers API; some books have no cover and
  show a placeholder, and a few cover URLs may 404 (handled with the placeholder fallback).
- **Descriptions** are only available on the work endpoint and are sometimes missing or
  very sparse in the source data.
- **Pagination** relies on the `page` parameter of the search API; combined with the
  optional "covers only" client-side filter, a few extra pages may be fetched before the
  list is exhausted.
- **Screenshots** are not bundled in this repository; capture them from a device/emulator
  and drop them into `docs/screenshots/` (see above).
- The Gradle wrapper jar is generated by Android Studio on import; the repo ships the
  wrapper configuration (`gradle-wrapper.properties`) rather than the binary.

---

## Build pipeline (CI)

A GitHub Actions workflow (`.github/workflows/ci.yml`) builds the debug APK
(`assembleDebug`) and runs the unit tests (`testDebugUnitTest`) on every push/PR to
`main`. It provisions JDK 17, accepts Android SDK licenses, installs the required SDK
packages, and uploads the built APK as a workflow artifact.

---

## Tech stack

- **Language:** Kotlin 1.9.22
- **UI:** Jetpack Compose (Material 3), Navigation Compose, Window Size Class
- **Networking:** Retrofit 2.11, OkHttp 4.12, Kotlin Serialization 1.6
- **Images:** Coil 2.6
- **Persistence:** Jetpack DataStore (Preferences)
- **Async:** Kotlin Coroutines + Flow
- **Build:** Android Gradle Plugin 8.3, Gradle 8.6

---

## Testing

Unit tests cover the mapper and repository/ViewModel logic under
`app/src/test/`:

```
./gradlew testDebugUnitTest
```

- `MappersTest` — DTO → domain mapping, missing-field fallbacks, cover-URL building,
  polymorphic `description` handling.
- `BookRepositoryTest` — success mapping, "covers only" filtering, and every error type
  (network / timeout / HTTP / serialization).
- `SearchViewModelTest` — input validation, first-page search, error state, and
  "load more" merging.
