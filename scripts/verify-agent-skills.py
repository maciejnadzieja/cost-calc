"""Verify vendored skill provenance and local guidance links without dependencies."""

import hashlib
import json
import re
import sys
from pathlib import Path
from urllib.parse import unquote, urlsplit


ROOT = Path(__file__).resolve().parents[1]


def verify(root):
    errors = []
    lock = json.loads((root / "docs/work/matt-pocock-skills.lock.json").read_text(encoding="utf-8"))
    entries = [lock["license"], *lock["files"]]
    destinations = set()
    for entry in entries:
        path = root / entry["destination"]
        destinations.add(path)
        if not path.is_file():
            errors.append(f"Missing upstream file: {entry['destination']}")
            continue
        # Git checkouts may use CRLF; upstream blobs use LF. Preserve all other bytes.
        content = path.read_bytes().replace(b"\r\n", b"\n")
        header = f"blob {len(content)}\0".encode("ascii")
        actual = hashlib.sha1(header + content).hexdigest()
        if actual != entry["git_blob_sha"]:
            errors.append(f"Upstream content changed: {entry['destination']}")
    skill_dirs = {p.parent for p in destinations if p.name == "SKILL.md"}
    for directory in skill_dirs:
        for path in directory.rglob("*"):
            if path.is_file() and path not in destinations:
                errors.append(f"Unrecorded upstream file: {path.relative_to(root)}")

    docs = [root / "AGENTS.md", *sorted((root / "docs/work").rglob("*.md")),
            *sorted((root / ".agents/skills").rglob("*.md"))]
    links = 0
    for path in docs:
        fence = None
        for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            marker = re.match(r"^\s*(`{3,}|~{3,})", line)
            if marker:
                kind = marker.group(1)[0]
                if fence is None:
                    fence = kind
                elif fence == kind:
                    fence = None
                continue
            if fence:
                continue
            for target in re.findall(r"\[[^\]]*\]\(([^)]+)\)", line):
                target = target.strip().strip("<>")
                parsed = urlsplit(target)
                if parsed.scheme or parsed.netloc or not parsed.path:
                    continue
                resolved = path.parent / unquote(parsed.path)
                links += 1
                if not resolved.exists():
                    errors.append(f"Broken link: {path.relative_to(root)}:{number}: {target}")
    return errors, len(entries), links


if __name__ == "__main__":
    failures, files, links = verify(ROOT)
    for failure in failures:
        print(failure)
    if failures:
        sys.exit(1)
    print(f"Verified {files} upstream files (including license) and {links} local links.")
