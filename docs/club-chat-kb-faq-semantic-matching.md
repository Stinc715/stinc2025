# Club Chat FAQ Semantic Matching

## Overview

This project now supports a club-level FAQ semantic matching layer for club chat.

The flow is:

1. Club admins create or edit FAQ entries in the club FAQ manager.
2. When an FAQ question is saved, the backend normalizes the question text and generates an embedding.
3. The embedding is stored on the FAQ row as `question_embedding`, together with `embedding_model` and `embedding_dim`.
4. When a user sends a message in club chat, the backend normalizes the user question, generates a user-question embedding, and compares it against enabled FAQ embeddings for the current club only.
5. The matcher computes cosine similarity and returns the best candidate.
6. A third-layer guard decides whether a static FAQ reply is safe to return.
7. If the FAQ hit is allowed, the configured FAQ reply is returned directly.
8. If the FAQ hit is rejected or no FAQ matches, the system falls back to the existing safe path or ordinary club chat logic.

## Save-Time Embedding

FAQ save-time embedding generation happens automatically in the backend.

For each saved FAQ question:

- trim leading and trailing whitespace
- normalize full-width / half-width spaces and common punctuation
- convert English text to lowercase
- compress repeated whitespace
- remove repeated trailing punctuation such as `??`, `!!`, `...`

The normalized question is then sent to the configured embedding provider.

Stored fields:

- `question_embedding`
- `embedding_model`
- `embedding_dim`
- `enabled`

## Match-Time Embedding

During club chat:

- the current user question is normalized with the same normalization logic
- the backend generates an embedding for the user question
- only enabled FAQ rows for the current `clubId` are considered
- cosine similarity is computed in Java memory
- the best FAQ candidate is selected using:
  - `bestScore >= app.chat.kb.matcher.best-score-threshold`
  - `bestScore - secondBestScore >= app.chat.kb.matcher.min-score-gap`

## Third-Layer Guard

Even if a semantic FAQ match is found, the system does not always return the static FAQ reply.

The third-layer guard blocks direct FAQ replies when:

- the user message contains high-risk keywords such as refund, complaint, dispute, legal, lawyer, human agent, special case, or their Chinese equivalents
- the user message contains realtime keywords such as today, now, currently, remaining, available now, available today, or their Chinese equivalents
- the top FAQ match is too ambiguous relative to the second-best score

If any of the above apply, the request falls back to the existing safe path or ordinary club chat logic.

## Environment Variables

Required for embedding generation at save time, backfill time, and match time:

- `APP_LLM_API_KEY`
- or `OPENAI_API_KEY`

Optional:

- `APP_LLM_API_BASE_URL`
- `APP_LLM_EMBEDDING_MODEL`

If no embedding API key is configured, embedding generation fails clearly and FAQ semantic matching falls back to the existing non-FAQ logic.

## Matcher Configuration

Configured in `application.yml`:

- `app.chat.kb.matcher.best-score-threshold`
- `app.chat.kb.matcher.min-score-gap`

Environment overrides:

- `APP_CHAT_KB_MATCHER_BEST_SCORE_THRESHOLD`
- `APP_CHAT_KB_MATCHER_MIN_SCORE_GAP`

## Guard Configuration

Configured in `application.yml`:

- `app.chat.kb.guard.high-risk-keywords`
- `app.chat.kb.guard.realtime-keywords`

Environment overrides:

- `APP_CHAT_KB_GUARD_HIGH_RISK_KEYWORDS`
- `APP_CHAT_KB_GUARD_REALTIME_KEYWORDS`

## Backfill

Historical FAQ rows may not have embeddings yet. Those rows must be backfilled before semantic FAQ matching can work reliably online.

Disabled FAQ rows are also eligible for backfill. This is intentional: backfill repairs data quality, while the matcher still only uses enabled FAQ rows.

### Club-Level Backfill

Endpoint:

`POST /api/my/clubs/{clubId}/chat-kb/backfill?forceRebuild=false&dryRun=false`

Permissions:

- system admin
- or admin of the specified club

Parameters:

- `forceRebuild=false`
  - only rebuild rows with missing or clearly invalid embedding data
- `forceRebuild=true`
  - rebuild every FAQ row in that club
- `dryRun=true`
  - do not write data, only return summary counts

### Global Backfill

Endpoint:

`POST /api/admin/chat-kb/backfill?forceRebuild=false&dryRun=false`

Permissions:

- system admin only

### Backfill Summary

Backfill returns:

- `scope`
- `clubId`
- `forceRebuild`
- `dryRun`
- `totalFound`
- `eligibleCount`
- `rebuiltCount`
- `skippedCount`
- `failedCount`
- `failureEntryIds`

## Manual Verification

Recommended verification order:

1. Configure `APP_LLM_API_KEY` or `OPENAI_API_KEY`.
2. Restart the backend service.
3. Trigger club-level dry-run backfill.
4. Trigger club-level real backfill.
5. Check the database:

```sql
SELECT id, club_id, question_title, embedding_model, embedding_dim, LENGTH(question_embedding)
FROM club_chat_kb_entry
WHERE club_id = 123
ORDER BY id DESC;
```

6. In user-side club chat, ask a semantically similar low-risk question and confirm the configured FAQ reply is returned directly.
7. Ask a realtime question such as `today` / `now` and confirm the static FAQ is not returned.
8. Ask a high-risk question such as `refund` and confirm the existing safe path is used.

## Common Failure Reasons

- `APP_LLM_API_KEY` / `OPENAI_API_KEY` is not configured
- historical FAQ rows have no embeddings yet
- FAQ is disabled
- FAQ matcher found a candidate but the third-layer guard rejected it
- FAQ embedding API call failed
- top FAQ match is too weak or too ambiguous
