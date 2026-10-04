#!/usr/bin/env python3
"""
Parse choice analytics exported from the Android app.

Files (pull from device):
  adb pull /sdcard/Android/data/com.example.sagaoftheaylopors/files/choice_analytics .

Usage:
  python parse_choice_debug.py debug_log.ndjson
  python parse_choice_debug.py debug_log.ndjson snapshot_latest.json

Output: human-readable report + issues (mismatched dialogue/choice, duplicate seq, stats stuck).
"""

from __future__ import annotations

import json
import sys
from collections import defaultdict
from pathlib import Path
from typing import Any


STAT_KEYS = [
    "sociality",
    "activity",
    "emotionalSensitivity",
    "anxiety",
    "selfControl",
    "impulsivity",
    "egoFocus",
    "rigidity",
    "negativeAffect",
    "adaptability",
]


def load_ndjson(path: Path) -> list[dict[str, Any]]:
    events = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line:
            continue
        events.append(json.loads(line))
    return events


def load_snapshot(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def fmt_stats(stats: dict[str, Any] | None) -> str:
    if not stats:
        return "(none)"
    parts = []
    for key in STAT_KEYS:
        if key in stats:
            parts.append(f"{key}={stats[key]}")
    return " ".join(parts)


def analyze_events(events: list[dict[str, Any]]) -> None:
    print("=== EVENT LOG ===")
    print(f"Total events: {len(events)}\n")

    by_chapter: dict[int, list[dict]] = defaultdict(list)

    for ev in events:
        ts = ev.get("ts", "?")
        name = ev.get("event", "?")
        data = ev.get("data") or {}
        print(f"[{ts}] {name}")
        if name == "choice_selected":
            ch = data.get("chapterNumber")
            doc = data.get("choicePointId")
            dlg = data.get("dialogueJsonId")
            picked = data.get("selectedChoiceId")
            scene = data.get("sceneJsonId")
            print(f"  chapter={ch} docId={doc} scene={scene} dialogue={dlg} picked={picked}")
            before = data.get("statsBefore")
            after = data.get("statsAfter")
            print(f"  statsBefore:  {fmt_stats(before)}")
            print(f"  statsAfter:   {fmt_stats(after)}")
            if before and after:
                deltas = []
                for key in STAT_KEYS:
                    if key in before and key in after and before[key] != after[key]:
                        deltas.append(f"{key}: {before[key]} -> {after[key]}")
                if deltas:
                    print(f"  deltas: {', '.join(deltas)}")
                else:
                    print("  WARNING: no stat deltas (effects may be zero or duplicate click)")
            if dlg and picked and not str(picked).startswith(str(dlg).split("_c")[0] if "_c" in str(picked) else dlg):
                # crude: picked should be dialogueJsonId + _cN
                if not str(picked).startswith(str(dlg)):
                    print(f"  ISSUE: picked choice id does not belong to dialogue {dlg}")
            if ch is not None:
                by_chapter[int(ch)].append(data)
        print()

    print("=== PER-CHAPTER SUMMARY ===")
    for ch in sorted(by_chapter):
        items = by_chapter[ch]
        ids = [i.get("choicePointId") for i in items]
        print(f"Chapter {ch}: {len(items)} choice event(s), docIds={ids}")
        if len(items) > 5:
            print(f"  WARNING: unusually many choices for one chapter (autoclicker?)")
    print()


def analyze_snapshot(snap: dict[str, Any]) -> None:
    print("=== SNAPSHOT ===")
    print(f"exportedAt: {snap.get('exportedAt')}")
    print(f"playthroughId: {snap.get('playthroughId')}")
    print(f"note: {snap.get('note')}")
    progress = snap.get("progress")
    if progress:
        print(f"progress stats: {fmt_stats(progress)}")
    pending = snap.get("pendingChoices") or []
    print(f"pendingChoices rows: {len(pending)}\n")
    for row in pending:
        synced = row.get("synced")
        print(
            f"  ch={row.get('chapterId')} doc={row.get('choicePointId')} "
            f"synced={synced} dlg={row.get('dialogueId')} picked={row.get('selectedChoiceId')}"
        )
    print()


def main() -> None:
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)

    log_path = Path(sys.argv[1])
    if not log_path.exists():
        print(f"File not found: {log_path}")
        sys.exit(1)

    events = load_ndjson(log_path)
    analyze_events(events)

    if len(sys.argv) >= 3:
        snap_path = Path(sys.argv[2])
        if snap_path.exists():
            analyze_snapshot(load_snapshot(snap_path))
        else:
            print(f"Snapshot not found: {snap_path}")


if __name__ == "__main__":
    main()
