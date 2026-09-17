"""FastAPI backend for the Wearable AI Companion.

POST /chat is backed by a pluggable AIProvider (see app/providers.py):
production uses AnthropicProvider (real Claude), tests override the
dependency with MockProvider so no real, paid API call is ever made
automatically.
"""

import logging
import time
import uuid

from fastapi import Depends, FastAPI, HTTPException
from pydantic import BaseModel, field_validator

from app.providers import AIProvider, AnthropicProvider, ProviderUnavailableError

logger = logging.getLogger("wearable_ai_companion")

app = FastAPI(title="Wearable AI Companion Backend")

_provider: AIProvider | None = None


def get_provider() -> AIProvider:
    """Lazy singleton: the Anthropic client is created once, on first use,
    not per request. Tests override this dependency with MockProvider.
    """
    global _provider
    if _provider is None:
        _provider = AnthropicProvider()
    return _provider


class HealthResponse(BaseModel):
    status: str


class ChatRequest(BaseModel):
    message: str

    @field_validator("message")
    @classmethod
    def message_must_not_be_blank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("message must not be blank")
        return value


class ChatResponse(BaseModel):
    response: str
    request_id: str
    latency_ms: float


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(status="ok")


@app.post("/chat", response_model=ChatResponse)
async def chat(
    payload: ChatRequest,
    provider: AIProvider = Depends(get_provider),
) -> ChatResponse:
    request_id = str(uuid.uuid4())
    start = time.perf_counter()

    # Do not log payload.message anywhere in this handler: keep prompt
    # contents out of the logs, only structural request metadata.
    try:
        response_text = await provider.generate(payload.message)
    except ProviderUnavailableError as e:
        latency_ms = (time.perf_counter() - start) * 1000
        logger.info(
            "request_id=%s status=provider_error category=%s latency_ms=%.2f",
            request_id,
            e.category,
            latency_ms,
        )
        raise HTTPException(status_code=503, detail="ai_provider_unavailable") from e

    latency_ms = (time.perf_counter() - start) * 1000

    logger.info("request_id=%s status=ok latency_ms=%.2f", request_id, latency_ms)

    return ChatResponse(response=response_text, request_id=request_id, latency_ms=latency_ms)
