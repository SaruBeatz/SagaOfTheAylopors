# Firestore backfill: `predictions`

Adds / updates the `predictions` field on:

- `users/{uid}/progress/current`
- `users/{uid}/playthroughs/{playthroughId}`

using the same logic as the Android app (`app/src/main/assets/ml/accentuation_export.json`).

## Setup

```bash
pip install firebase-admin
```

Download a Firebase **service account** JSON (Project settings → Service accounts → Generate key).

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccountKey.json"
```

## Run

```bash
# All users with subcollections
python scripts/firestore_backfill_predictions.py

# One user (example)
python scripts/firestore_backfill_predictions.py --uid l8LhR6LZ4UcwVIUW2ZIoqUD0zJm1

# Preview without writes
python scripts/firestore_backfill_predictions.py --dry-run
```

## `predictions` shape

```json
{
  "modelVersion": "v1_lr",
  "chapterId": 7,
  "predictedAt": "2026-05-21T12:00:00+00:00",
  "primaryLabel": "Тревожный",
  "primaryProbability": 0.9854,
  "items": [
    { "label": "Тревожный", "probability": 0.9854 },
    { "label": "Дистим", "probability": 0.0143 }
  ]
}
```

Only items with `probability >= 0.1` are included (max 3).
