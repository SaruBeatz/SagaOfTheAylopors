#!/usr/bin/env python3
"""
Backfill Firestore `predictions` for existing users (progress/current + playthroughs).

Requires:
  pip install firebase-admin
  set GOOGLE_APPLICATION_CREDENTIALS=path/to/serviceAccountKey.json

Usage:
  python scripts/firestore_backfill_predictions.py
  python scripts/firestore_backfill_predictions.py --uid l8LhR6LZ4UcwVIUW2ZIoqUD0zJm1
  python scripts/firestore_backfill_predictions.py --dry-run
"""
from __future__ import annotations

import argparse
import json
import math
import sys
from datetime import datetime, timezone
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
MODEL_JSON = PROJECT_ROOT / "app" / "src" / "main" / "assets" / "ml" / "accentuation_export.json"
MIN_PROB = 0.1
MAX_ITEMS = 3
MODEL_VERSION = "v1_lr"


def load_model(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def vector_from_stats(stats: dict, features: list[str]) -> list[float]:
    key_map = {
        "Soc": "sociality",
        "Act": "activity",
        "Emp": "emotionalSensitivity",
        "Anx": "anxiety",
        "Ctrl": "selfControl",
        "Imp": "impulsivity",
        "Ego": "egoFocus",
        "Rig": "rigidity",
        "Neg": "negativeAffect",
        "Adp": "adaptability",
    }
    out = []
    for f in features:
        fire_key = key_map.get(f, f)
        val = stats.get(fire_key, stats.get(f, 0.5))
        out.append(max(0.0, min(1.0, float(val))))
    return out


def softmax(scores: list[float]) -> list[float]:
    m = max(scores)
    exps = [math.exp(s - m) for s in scores]
    s = sum(exps)
    return [e / s for e in exps]


def predict(cfg: dict, stats: dict, chapter_id: int) -> dict | None:
    if not stats:
        return None
    features = cfg["features"]
    classes = cfg["classes"]
    mean = cfg["scaler_mean"]
    scale = cfg["scaler_scale"]
    coef = cfg["coefficients"]
    intercept = cfg["intercept"]

    raw = vector_from_stats(stats, features)
    scaled = [(raw[i] - mean[i]) / scale[i] for i in range(len(raw))]
    scores = []
    for k in range(len(classes)):
        s = intercept[k]
        for j in range(len(scaled)):
            s += coef[k][j] * scaled[j]
        scores.append(s)
    probs = softmax(scores)
    order = sorted(range(len(probs)), key=lambda i: -probs[i])

    items = []
    for idx in order:
        if probs[idx] < MIN_PROB:
            break
        items.append({"label": classes[idx], "probability": round(probs[idx], 4)})
        if len(items) >= MAX_ITEMS:
            break
    if not items:
        best = order[0]
        items.append({"label": classes[best], "probability": round(probs[best], 4)})

    primary = items[0]
    return {
        "modelVersion": MODEL_VERSION,
        "chapterId": chapter_id,
        "predictedAt": datetime.now(timezone.utc).isoformat(),
        "primaryLabel": primary["label"],
        "primaryProbability": primary["probability"],
        "items": items,
    }


def chapter_id_from_doc(data: dict, default: int = 7) -> int:
    if "lastCompletedChapterId" in data:
        return int(data["lastCompletedChapterId"])
    if "currentChapterId" in data:
        return max(1, int(data["currentChapterId"]) - 1)
    return default


def main() -> int:
    parser = argparse.ArgumentParser(description="Backfill predictions in Firestore")
    parser.add_argument("--dry-run", action="store_true", help="Print only, no writes")
    parser.add_argument("--uid", type=str, help="Single user id to process")
    args = parser.parse_args()

    if not MODEL_JSON.exists():
        print(f"Model not found: {MODEL_JSON}", file=sys.stderr)
        return 1

    try:
        import firebase_admin
        from firebase_admin import credentials, firestore
    except ImportError:
        print("Install: pip install firebase-admin", file=sys.stderr)
        return 1

    if not firebase_admin._apps:
        firebase_admin.initialize_app(credentials.ApplicationDefault())

    cfg = load_model(MODEL_JSON)
    db = firestore.client()
    updated = 0
    skipped = 0

    if args.uid:
        user_ids = [args.uid]
    else:
        user_ids = [u.id for u in db.collection("users").stream()]

    for uid in user_ids:
        print(f"\n=== User {uid} ===")
        user_ref = db.collection("users").document(uid)

        # progress/current
        current_ref = user_ref.collection("progress").document("current")
        current_snap = current_ref.get()
        if current_snap.exists:
            data = current_snap.to_dict() or {}
            stats = data.get("stats")
            ch = chapter_id_from_doc(data, default=7)
            pred = predict(cfg, stats, ch)
            if pred:
                print(f"  progress/current ch={ch} -> {pred['primaryLabel']} ({pred['primaryProbability']})")
                if not args.dry_run:
                    current_ref.set({"predictions": pred}, merge=True)
                updated += 1
            else:
                print("  progress/current — no stats, skip")
                skipped += 1
        else:
            print("  progress/current — missing")
            skipped += 1

        # playthroughs
        for pt_ref in user_ref.collection("playthroughs").stream():
            pt_data = pt_ref.to_dict() or {}
            stats = pt_data.get("stats")
            ch = chapter_id_from_doc(pt_data, default=7)
            pred = predict(cfg, stats, ch)
            if pred:
                print(f"  playthrough {pt_ref.id} ch={ch} -> {pred['primaryLabel']}")
                if not args.dry_run:
                    pt_ref.reference.set({"predictions": pred}, merge=True)
                updated += 1
            else:
                print(f"  playthrough {pt_ref.id} — no stats, skip")
                skipped += 1

    print(f"\nDone. updated={updated} skipped={skipped} dry_run={args.dry_run}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
