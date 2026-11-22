import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]


class LocalArtworkContractTest(unittest.TestCase):
    def test_local_artwork_only_queries_granted_sibling_images(self):
        source = (ROOT / "app/src/main/java/com/mangoloads/player/ui/library/LocalArtworkResolver.kt").read_text()
        screen = (ROOT / "app/src/main/java/com/mangoloads/player/ui/library/LocalLibraryScreen.kt").read_text()
        self.assertIn("persistedUriPermissions", source)
        self.assertIn("buildChildDocumentsUriUsingTree", source)
        self.assertIn("isChildDocument", source)
        self.assertIn("readSafCursor", source)
        self.assertIn("LocalArtworkMatcher.choose", source)
        self.assertIn("LocalArtworkResolver(context, Uri.parse(folder.tree))", screen)
        self.assertNotRegex(source, r"(?i)https?://")
        self.assertNotIn("openOutputStream", source)
        self.assertNotIn("deleteDocument", source)

    def test_filename_matching_is_exact_and_does_not_share_movie_generic_art(self):
        source = (ROOT / "app/src/main/java/com/mangoloads/player/ui/library/LocalArtworkResolver.kt").read_text()
        self.assertIn("imageStem == videoStem", source)
        self.assertIn("videoCount == 1 && imageStem in genericNames", source)
        test = (ROOT / "app/src/test/java/com/mangoloads/player/ui/library/LocalArtworkMatcherTest.kt").read_text()
        self.assertIn("doesNotApplyAnAmbiguousGenericPosterToMultipleMovies", test)
        self.assertIn("prefersExactAdjacentArtworkOverGenericCover", test)


if __name__ == "__main__":
    unittest.main()
