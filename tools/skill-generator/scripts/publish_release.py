"""Publish immutable release assets, comparing existing bytes before any upload."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile
from urllib.error import HTTPError
from urllib.parse import quote
from urllib.request import Request, urlopen


def release_metadata(repository, tag):
    url = f"https://api.github.com/repos/{repository}/releases/tags/{quote(tag, safe='')}"
    request = Request(url, headers={
        "Authorization": f"Bearer {os.environ['GH_TOKEN']}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
    })
    try:
        with urlopen(request, timeout=60) as response:
            return json.load(response)
    except HTTPError as error:
        if error.code == 404:
            return None
        raise


def gh(*args):
    subprocess.run(["gh", *map(str, args)], check=True)


def publish(repository, tag, archive):
    checksum = archive.with_name(archive.name + ".sha256")
    expected = hashlib.sha256(archive.read_bytes()).hexdigest() + f"  {archive.name}\n"
    if checksum.read_text(encoding="utf-8") != expected:
        raise ValueError("Local archive checksum is incorrect")
    assets = [archive, checksum]
    release = release_metadata(repository, tag)
    if release is None:
        gh("release", "create", tag, *assets, "--repo", repository,
           "--verify-tag", "--title", tag, "--notes", "Agent Skills bundle and SHA-256 checksum.")
        return

    existing = {asset["name"] for asset in release["assets"]}
    missing = []
    with tempfile.TemporaryDirectory(prefix="skills-release-") as directory:
        for asset in assets:
            if asset.name not in existing:
                missing.append(asset)
                continue
            gh("release", "download", tag, "--repo", repository,
               "--pattern", asset.name, "--dir", directory)
            if (Path(directory) / asset.name).read_bytes() != asset.read_bytes():
                raise ValueError(f"Release {tag} already contains different bytes: {asset.name}")
    if missing:
        gh("release", "upload", tag, *missing, "--repo", repository)
    else:
        print(f"Release {tag} already contains identical assets; publication skipped")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive", type=Path)
    args = parser.parse_args()
    tag = os.environ["GITHUB_REF_NAME"]
    if not tag.startswith("skills-v"):
        raise SystemExit("Release tag must start with skills-v")
    publish(os.environ["GITHUB_REPOSITORY"], tag, args.archive)


if __name__ == "__main__":
    main()
