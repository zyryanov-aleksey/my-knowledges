"""Invoke the pinned skills-ref executable once for every generated skill."""

import argparse
import json
from pathlib import Path
import subprocess


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("bundle", type=Path)
    parser.add_argument("--validator", required=True, type=Path)
    args = parser.parse_args()
    manifest = json.loads((args.bundle / "manifest.json").read_text(encoding="utf-8"))
    expected = {entry["name"] for entry in manifest}
    actual = {path.name for path in args.bundle.iterdir() if path.is_dir()}
    if len(expected) != len(manifest) or expected != actual:
        raise SystemExit("Skill directories differ from manifest.json")
    for name in sorted(expected):
        subprocess.run([str(args.validator.resolve()), "validate", str(args.bundle / name)],
                       check=True)
    print(f"skills-ref validated {len(expected)} skills")


if __name__ == "__main__":
    main()
