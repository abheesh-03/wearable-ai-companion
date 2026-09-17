import asyncio

from app.providers import MockProvider


def test_mock_provider_returns_deterministic_response():
    provider = MockProvider()

    result = asyncio.run(provider.generate("Explain transformers simply"))

    assert result == "Cloud backend received: Explain transformers simply"


def test_mock_provider_echoes_the_exact_message_each_time():
    provider = MockProvider()

    first = asyncio.run(provider.generate("What time is it"))
    second = asyncio.run(provider.generate("A different message"))

    assert first == "Cloud backend received: What time is it"
    assert second == "Cloud backend received: A different message"
