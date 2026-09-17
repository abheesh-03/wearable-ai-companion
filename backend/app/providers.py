"""AI provider abstraction for the /chat endpoint.

MockProvider is deterministic and offline — used by tests and as a safe
default so pytest never makes a real, paid Anthropic API call.

AnthropicProvider talks to the real Claude API. It reads ANTHROPIC_API_KEY
from the environment only (never hardcoded, never logged, never returned to
the client) via the SDK's own credential resolution.
"""

import logging
from typing import Protocol

import anthropic

logger = logging.getLogger("wearable_ai_companion")

# Claude Haiku 4.5 — the current Haiku-class model: 200K context, $1/$5 per
# 1M input/output tokens. Chosen for fast, low-cost responses appropriate
# for a wearable client. This is a stable model ID, not a dated snapshot —
# do not append a date suffix.
CLAUDE_MODEL = "claude-haiku-4-5"

# Modest ceiling appropriate for a small round watch display; a Haiku
# response for this system prompt should land well under this.
MAX_RESPONSE_TOKENS = 180

WATCH_SYSTEM_PROMPT = (
    "You are a voice assistant for a smartwatch with a small round display "
    "that requires excessive scrolling for long text. Answer the user's "
    "question immediately, with no greeting or sign-off. Respond in plain "
    "text only: no Markdown, no headings, no code fences, and no bullet or "
    "numbered lists unless the user explicitly asks for a list. Default to "
    "1-2 short sentences, roughly 25-45 words, for normal questions — skip "
    "background explanation unless it's necessary to answer, and use at "
    "most one compact analogy only when it materially helps. If the user "
    "explicitly asks for detail, steps, a list, or code, you may give a "
    "longer answer. Never truncate your answer mid-sentence."
)


class ProviderUnavailableError(Exception):
    """Raised when the AI provider cannot fulfill a request.

    `category` is for server-side logging only — it is never sent to the
    client. The FastAPI layer maps every instance of this to a generic
    HTTP 503 with a fixed detail string.
    """

    def __init__(self, category: str) -> None:
        super().__init__(category)
        self.category = category


class AIProvider(Protocol):
    async def generate(self, message: str) -> str: ...


class MockProvider:
    """Deterministic, offline provider. Never touches the real Anthropic API."""

    async def generate(self, message: str) -> str:
        return f"Cloud backend received: {message}"


class AnthropicProvider:
    """Production provider backed by the official Anthropic Python SDK."""

    def __init__(self) -> None:
        # Resolves ANTHROPIC_API_KEY from the environment. Does not validate
        # the key eagerly — an invalid/missing key surfaces as an
        # AuthenticationError (an APIStatusError subclass) on first call,
        # which generate() below already maps to ProviderUnavailableError.
        self._client = anthropic.AsyncAnthropic()

    async def generate(self, message: str) -> str:
        try:
            response = await self._client.messages.create(
                model=CLAUDE_MODEL,
                max_tokens=MAX_RESPONSE_TOKENS,
                system=WATCH_SYSTEM_PROMPT,
                messages=[{"role": "user", "content": message}],
            )
        except anthropic.APITimeoutError as e:
            raise ProviderUnavailableError("timeout") from e
        except anthropic.APIConnectionError as e:
            raise ProviderUnavailableError("connection") from e
        except anthropic.APIStatusError as e:
            raise ProviderUnavailableError(f"api_status_{e.status_code}") from e
        except Exception as e:
            # Anything else from the SDK (unexpected shape, etc.) — never let
            # a raw exception/traceback reach the client.
            raise ProviderUnavailableError("unknown") from e

        text = next((block.text for block in response.content if block.type == "text"), "")
        if not text.strip():
            raise ProviderUnavailableError("empty_response")

        return text
