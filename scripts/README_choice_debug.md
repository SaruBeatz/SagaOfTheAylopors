# Choice analytics debug export

## On device

After each choice and after chapter sync, the app writes:

- `Android/data/com.example.sagaoftheaylopors/files/choice_analytics/debug_log.ndjson`
- `Android/data/com.example.sagaoftheaylopors/files/choice_analytics/snapshot_latest.json`

## Pull files (USB debugging)

```bash
adb pull /sdcard/Android/data/com.example.sagaoftheaylopors/files/choice_analytics ./choice_analytics
```

## Parse locally

```bash
python scripts/parse_choice_debug.py choice_analytics/debug_log.ndjson choice_analytics/snapshot_latest.json
```

Send `debug_log.ndjson` and `snapshot_latest.json` for analysis.

## Logcat

Filter: `ChoiceAnalytics` or `PlaythroughRepository`

After each chapter sync, temporary verification lines appear:

Chapter sync is write-only (batch at chapter end); no post-sync read verification.
- `MISMATCH` — see `local=` vs `remote=` per field

**Note:** `progress/current.stats` is the live total after the chapter ends; `choices/7_3.statsAfter` is frozen at the moment of that choice. They can differ if more dialogue ran after the last choice.
