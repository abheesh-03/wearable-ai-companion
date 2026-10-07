# Wearable AI Companion

A Wear OS AI assistant that keeps deterministic device requests on-device and
routes general questions to Claude through a FastAPI backend. Local intents include
time, date, help, and battery percentage; open-ended questions use cloud AI.
Built as a full-stack, offline-aware mobile/wearable + backend project:
Kotlin/Jetpack Compose on the watch, FastAPI + the Anthropic Python SDK on the server.

Initial development and local testing ran from June through September 2026, with later commits focused mainly on testing, documentation, and small refinements.

## Screenshots

**1. Native Wear OS Input** — the RemoteInput picker showing voice and keyboard options

![Native Wear OS Input](docs/screenshots/remote-input.png)

**2. On-device Time** — "What time is it now", routed locally, works without the backend

![On-device Time](docs/screenshots/on-device-time.png)

**3. On-device Date** — "What date is it today", routed locally, works without the backend

![On-device Date](docs/screenshots/on-device-date.png)

**4. Voice → Cloud AI** — a general question routed to Claude through FastAPI.
The Cloud AI response is shown with latency; this is a single observed
request, not a benchmark.

![Voice to Cloud AI](docs/screenshots/cloud-voice-response.png)

## Demo Flow

The app classifies every request into one of two paths before it does anything else.

**LOCAL** — deterministic, on-device, works with the backend fully offline:

```
Wear OS input (RemoteInput)
  -> AskAiViewModel
  -> IntentRouter
  -> LocalIntentHandler
  -> Clock / BatteryStatusProvider
  -> on-device response
```

For battery requests, the production provider reads the actual watch percentage
through Android `BatteryManager`. This path was manually verified with the
backend offline: asking **"What's my battery level?"** still returned an
**On-device** response with the emulator's current battery percentage.

**CLOUD** — general questions, requires the backend:

```
Wear OS input (RemoteInput)
  -> AskAiViewModel
  -> IntentRouter
  -> CloudAiRepository
  -> Retrofit / OkHttp
  -> FastAPI
  -> AnthropicProvider
  -> Claude (Haiku 4.5)
  -> compact watch-sized response
```

```mermaid
flowchart TD
    A[Wear OS RemoteInput] --> B[AskAiViewModel]
    B --> C{IntentRouter}
    C -->|LOCAL| D[LocalIntentHandler]
    D --> E[Clock / BatteryStatusProvider]
    E --> L[On-device response]
    C -->|CLOUD| F[CloudAiRepository]
    F --> G[Retrofit / OkHttp]
    G --> H[FastAPI backend]
    H --> I[AnthropicProvider]
    I --> J[Claude Haiku 4.5]
    J --> H
    H --> G
    G --> K[Compact watch response]
```

## Features

- Native Wear OS text entry: system keyboard and voice input via `RemoteInput`
- Local-vs-cloud intent routing, decided per request before any network call
- Time, date, help, and battery percentage requests can be answered entirely on-device, no backend required
- Battery percentage is read from Android `BatteryManager` through a testable `BatteryStatusProvider` abstraction
- "Explain" quick action and free-text questions route to Claude through the backend
- Home screen connectivity status: Checking / Connected / Offline, backed by a real `GET /health` check
- Short, watch-formatted Claude responses (plain text, no markdown, 1-2 sentences by default)
- Loading and error states on the Ask AI screen, with duplicate-send protection
- Latency shown for cloud responses
- Native Wear OS swipe-to-dismiss navigation

## Why Local vs Cloud Routing

Routing simple, deterministic requests on-device instead of always calling the cloud:

- Lower latency — "what time is it" or "what's my battery level" does not need a cloud round trip
- No unnecessary network dependency for facts the watch already knows
- Lower API cost — trivial deterministic requests never touch a paid model
- More resilient — local intents keep working even if the backend is down
- The router is intentionally conservative: it matches a fixed set of exact,
  normalized phrases rather than doing substring matching, so questions like
  *"What is time complexity?"* or *"Why is my battery draining so fast?"*
  correctly fall through to Claude instead of being misrouted because they
  contain the words "time" or "battery"

## Tech Stack

**Android**
- Kotlin
- Jetpack Compose for Wear OS
- Wear Compose Material3
- ViewModel / StateFlow
- Retrofit
- OkHttp

**Backend**
- Python
- FastAPI
- Anthropic Python SDK
- Pydantic
- pytest

**Testing**
- JUnit
- kotlinx-coroutines-test
- Fake repositories / providers (no mocking framework)
- FastAPI `TestClient` / pytest

## Project Structure

```
app/src/main/java/.../
  data/
    device/             # AndroidBatteryStatusProvider (BatteryManager-backed)
    remote/             # Retrofit API service + models
    ...                 # CloudAiRepository, HealthChecker
  domain/routing/       # IntentRoute, IntentRouter, LocalIntentClassifier, LocalIntentHandler,
                        # BatteryStatusProvider
  presentation/
    ask/                # Ask AI screen, ViewModel, UI state, AskAiViewModelFactory
    home/                # Home screen, ViewModel, quick actions, connectivity state
    navigation/          # Wear SwipeDismissableNavHost + route definitions
    components/          # Shared Wear Compose UI pieces
    theme/                # Colors and MaterialTheme

backend/
  app/
    main.py             # FastAPI app, /health and /chat endpoints
    providers.py         # AIProvider abstraction: MockProvider, AnthropicProvider
  tests/                 # pytest suite (dependency-overridden, no real API calls)
```

`domain/routing` has no Android or network dependencies — it's plain Kotlin,
which is what makes local intent handling testable and truly backend-independent.
`data/` isolates all networking behind interfaces (`CloudAiRepository`,
`HealthChecker`) so the ViewModels never depend on Retrofit directly.

## API

### `GET /health`

```json
{
  "status": "ok"
}
```

### `POST /chat`

Request:

```json
{
  "message": "Explain black holes simply"
}
```

Response:

```json
{
  "response": "...",
  "request_id": "...",
  "latency_ms": 986.0
}
```

`latency_ms` is the backend's own request-processing time (server receive to
server respond, including the Claude API call) — it's diagnostic information
returned by the API, not a benchmark or a guarantee of end-to-end latency as
seen from the watch. `request_id` exists for backend-side tracing/log
correlation; it is not currently shown in the Wear OS UI.

## Running Locally

### Backend

From the repository root:

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Load your Anthropic API key into the shell without putting it in source code:

```bash
read -s "ANTHROPIC_API_KEY?Anthropic API key: "
export ANTHROPIC_API_KEY
echo
```

Then start the server:

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

`backend/.env` is gitignored, and `backend/.env.example` documents the one
expected variable name (`ANTHROPIC_API_KEY`). Never commit a real key.

### Android / Wear OS

1. Open the project root in Android Studio.
2. Create or select a Wear OS emulator (this project was built and verified against Wear OS 6 / API 36).
3. With the backend running locally, the app reaches it at `http://10.0.2.2:8000` — the special alias the Android emulator uses to reach the host machine's loopback interface. No configuration is needed for emulator development.
4. Run the app normally from Android Studio (Run ▶ on the Wear OS device/emulator target).

**Emulator voice input note:** microphone input on the emulator depends on
host audio being routed in, which sometimes needs to be enabled explicitly:

```bash
adb -s <emulator-id> emu avd hostmicon
adb -s <emulator-id> shell cmd sensor_privacy disable 0 microphone
```

These are emulator/host configuration steps, not app behavior — a physical
watch's own microphone does not need this. If speech recognition becomes
unreliable after changing these settings, a cold boot of the emulator
resolves it in most cases.

## Testing

**Android:**

```bash
./gradlew testDebugUnitTest
```

67 JVM unit tests currently pass.

**Backend:**

```bash
cd backend
source .venv/bin/activate
pytest
```

8 backend tests currently pass. Backend tests use `MockProvider` via FastAPI
dependency overrides and never call the real Anthropic API.

Coverage includes: local-vs-cloud routing decisions, false-positive routing
traps (for example "time" or "battery" appearing in unrelated/explanatory
questions), deterministic time/date responses via injected `Clock`, battery
responses via an injected `BatteryStatusProvider`, the unavailable-battery
fallback, and a ViewModel test proving a battery request is answered locally
without invoking `CloudAiRepository`. The suite also covers cloud repository
error mapping (timeout / connection / HTTP / malformed response), duplicate-send
protection, the health checker, Home connectivity states, quick-action behavior,
provider failures mapping to HTTP 503, and provider tests that never touch a real
API key or network.

## Error Handling / Reliability

- Network failures are classified at the repository boundary: connection,
  timeout, HTTP 5xx, and malformed responses each map to a distinct,
  user-readable message
- No raw exceptions, stack traces, or exception class names ever reach the
  Wear OS UI
- Backend-side provider failures (timeout, connection, Anthropic API errors)
  all collapse to a stable `HTTP 503` with a fixed `"ai_provider_unavailable"`
  detail — internal failure category is logged server-side only
- `POST /chat` is never automatically retried: an ambiguous failed request
  may have already reached the paid provider, so retrying could duplicate
  billed work
- Coroutine cancellation propagates normally and is never swallowed as a
  failure result

## Security / Development Notes

- The Anthropic API key lives only on the backend, read from
  `ANTHROPIC_API_KEY` in the environment — it is never embedded in the
  Android app or sent to the client
- Cleartext HTTP is allowed only for `10.0.2.2`, scoped via Android's
  network security config, specifically for local emulator development
- A real deployment would need to run the backend behind HTTPS; this project
  does not include that setup
- The backend currently runs locally on the developer's machine — it is not
  deployed anywhere public

## Known Limitations

- Verified on a Wear OS 6 / API 36 emulator; not yet validated on a physical Wear OS watch
- The backend runs locally and is not production-deployed
- No conversation history — each request is independent
- No database or persistence layer
- No authentication on the backend API
- Voice recognition quality depends on Wear OS's speech services and, on
  emulator, host audio/microphone configuration

## Design Decisions

- **Native `RemoteInput` instead of a custom speech-recording UI** — Wear OS
  has no on-screen keyboard by design; using the system's own
  keyboard/voice/handwriting picker is the idiomatic pattern and avoids
  reimplementing speech capture
- **Conservative, exact-match local router** — trades a bit of recall for
  correctness; a broader substring matcher would misroute genuine questions
  that happen to contain words like "time" or "date"
- **Battery access behind `BatteryStatusProvider`** — the routing/domain layer
  does not depend directly on Android framework APIs. Production injects
  `AndroidBatteryStatusProvider`, which reads
  `BatteryManager.BATTERY_PROPERTY_CAPACITY`; tests inject deterministic fake
  providers.
- **The Anthropic secret never leaves the backend** — the Android app only
  ever talks to the local FastAPI service, never to Anthropic directly
- **Repository and provider abstractions on both sides** (`CloudAiRepository`,
  `HealthChecker`, `AIProvider`) — every network dependency is an interface,
  which is what makes the ViewModel and endpoint tests possible without a
  real network or a real API key
- **No automatic retry on `POST /chat`** — a failed request may have already
  reached Claude; retrying blindly risks duplicating paid work
- **A short, format-constrained system prompt** — Claude is instructed to
  answer in 1-2 sentences, plain text, no markdown, so responses fit a small
  round display without heavy scrolling
