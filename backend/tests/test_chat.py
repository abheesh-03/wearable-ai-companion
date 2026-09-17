import pytest
from fastapi.testclient import TestClient

from app.main import app, get_provider
from app.providers import MockProvider, ProviderUnavailableError

client = TestClient(app)


class FailingProvider:
    """Fake provider that always fails, to exercise the 503 error path
    without ever touching the real Anthropic API.
    """

    async def generate(self, message: str) -> str:
        raise ProviderUnavailableError("connection")


@pytest.fixture(autouse=True)
def override_with_mock_provider():
    # Every test in this file uses MockProvider by default so pytest never
    # makes a real, paid Anthropic API call, regardless of whether
    # ANTHROPIC_API_KEY happens to be set in the environment running the
    # tests. Individual tests may override further (see below).
    app.dependency_overrides[get_provider] = lambda: MockProvider()
    yield
    app.dependency_overrides.clear()


def test_health_returns_ok():
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_chat_returns_deterministic_response_with_metadata():
    response = client.post("/chat", json={"message": "Explain transformers simply"})

    assert response.status_code == 200
    body = response.json()
    assert body["response"] == "Cloud backend received: Explain transformers simply"
    assert isinstance(body["request_id"], str) and len(body["request_id"]) > 0
    assert isinstance(body["latency_ms"], (int, float))
    assert body["latency_ms"] >= 0


def test_chat_rejects_blank_message():
    response = client.post("/chat", json={"message": "   "})

    assert response.status_code == 422


def test_chat_rejects_missing_message():
    response = client.post("/chat", json={})

    assert response.status_code == 422


def test_chat_provider_failure_returns_503_with_stable_detail():
    app.dependency_overrides[get_provider] = lambda: FailingProvider()

    response = client.post("/chat", json={"message": "Explain transformers simply"})

    assert response.status_code == 503
    assert response.json() == {"detail": "ai_provider_unavailable"}


def test_chat_provider_failure_does_not_expose_internal_details():
    app.dependency_overrides[get_provider] = lambda: FailingProvider()

    response = client.post("/chat", json={"message": "hello"})

    body_text = response.text
    assert "Traceback" not in body_text
    assert "ProviderUnavailableError" not in body_text
    assert "anthropic" not in body_text.lower()
