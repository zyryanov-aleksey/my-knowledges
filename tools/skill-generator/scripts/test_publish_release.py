import hashlib
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import publish_release


class PublishReleaseTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.archive = Path(self.directory.name) / "skills-v1.2.3.zip"
        self.archive.write_bytes(b"archive")
        self.checksum = self.archive.with_name(self.archive.name + ".sha256")
        self.checksum.write_text(hashlib.sha256(b"archive").hexdigest() + f"  {self.archive.name}\n",
                                 encoding="utf-8")

    def run_existing(self, remote):
        def fake_gh(*args):
            if args[1] == "download":
                name = args[args.index("--pattern") + 1]
                directory = Path(args[args.index("--dir") + 1])
                (directory / name).write_bytes(remote[name])

        metadata = {"assets": [{"name": name} for name in remote]}
        return patch.multiple(publish_release, release_metadata=lambda *_: metadata), fake_gh

    def test_creates_release_with_both_assets(self):
        with patch.object(publish_release, "release_metadata", return_value=None), \
                patch.object(publish_release, "gh") as gh:
            publish_release.publish("owner/repo", "skills-v1", self.archive)
        self.assertEqual("create", gh.call_args.args[1])
        self.assertIn(self.archive, gh.call_args.args)
        self.assertIn(self.checksum, gh.call_args.args)

    def test_identical_assets_are_not_uploaded(self):
        remote = {self.archive.name: b"archive", self.checksum.name: self.checksum.read_bytes()}
        metadata_patch, download = self.run_existing(remote)
        with metadata_patch, patch.object(publish_release, "gh", side_effect=download) as gh:
            publish_release.publish("owner/repo", "skills-v1", self.archive)
        self.assertEqual(["download", "download"], [call.args[1] for call in gh.call_args_list])

    def test_different_archive_fails_before_upload(self):
        metadata_patch, download = self.run_existing({self.archive.name: b"different"})
        with metadata_patch, patch.object(publish_release, "gh", side_effect=download) as gh:
            with self.assertRaisesRegex(ValueError, "different bytes"):
                publish_release.publish("owner/repo", "skills-v1", self.archive)
        self.assertEqual(["download"], [call.args[1] for call in gh.call_args_list])

    def test_different_remote_checksum_fails_before_upload(self):
        remote = {self.archive.name: b"archive", self.checksum.name: b"different checksum"}
        metadata_patch, download = self.run_existing(remote)
        with metadata_patch, patch.object(publish_release, "gh", side_effect=download) as gh:
            with self.assertRaisesRegex(ValueError, "different bytes"):
                publish_release.publish("owner/repo", "skills-v1", self.archive)
        self.assertTrue(all(call.args[1] == "download" for call in gh.call_args_list))

    def test_recovers_missing_checksum_without_overwriting_archive(self):
        metadata_patch, download = self.run_existing({self.archive.name: b"archive"})
        with metadata_patch, patch.object(publish_release, "gh", side_effect=download) as gh:
            publish_release.publish("owner/repo", "skills-v1", self.archive)
        self.assertEqual("upload", gh.call_args.args[1])
        self.assertIn(self.checksum, gh.call_args.args)
        self.assertNotIn(self.archive, gh.call_args.args)

    def test_rejects_incorrect_local_checksum_before_network(self):
        self.checksum.write_bytes(b"incorrect")
        with patch.object(publish_release, "release_metadata") as metadata:
            with self.assertRaisesRegex(ValueError, "checksum"):
                publish_release.publish("owner/repo", "skills-v1", self.archive)
        metadata.assert_not_called()


if __name__ == "__main__":
    unittest.main()
