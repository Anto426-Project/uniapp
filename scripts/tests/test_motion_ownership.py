from pathlib import Path
import tempfile
import unittest
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from check_motion_ownership import validate


class MotionOwnershipTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)

    def source(self, name, code, folder='composeApp/src/commonMain/kotlin'):
        path = self.root / folder / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(code)

    def test_rejects_raw_motion_in_common_and_platform_sources(self):
        self.source('Screen.kt', 'import androidx.compose.animation.core.*\nval phase = rememberInfiniteTransition()')
        self.source('Activity.kt', 'val value = androidx.compose.animation.core.tween(200)', 'androidApp/src/main/kotlin')
        self.source('Content.swift', 'withAnimation() { visible = true }', 'iosApp')
        errors = validate(self.root)
        self.assertEqual(4, len(errors))
        self.assertTrue(any('Activity.kt' in error for error in errors))
        self.assertTrue(any('Content.swift' in error for error in errors))

    def test_allows_sdk_hosts_and_single_shared_preset(self):
        self.source('UniMotion.kt', 'val preset = LiquidSwitcherTransition.Crossfade')
        self.source('AppNavigationHost.kt', 'rememberLiquidContentTransition<Scene<NavKey>>(transition = UniMotion.contentTransition)')
        self.source('UniUpdateBanner.kt', 'LiquidAnimatedSwitcher(transition = UniMotion.contentTransition)')
        self.source('AppScreenTransitions.kt', 'val preset = LiquidSwitcherTransition.None')
        self.source('Section.kt', 'LiquidCard() // fadeIn()\n/* tween(300) */')
        self.assertEqual([], validate(self.root))

    def test_rejects_per_screen_sdk_transitions_and_entrances(self):
        self.source('Screen.kt', 'LiquidAnimatedSwitcher(transition = LiquidSwitcherTransition.FadeThrough)\nLiquidSectionEntrance()')
        self.assertEqual(3, len(validate(self.root)))

    def test_test_sources_may_exercise_animation_contracts(self):
        self.source('MotionTest.kt', 'import androidx.compose.animation.*\nAnimatedContent()', 'composeApp/src/commonTest/kotlin')
        self.assertEqual([], validate(self.root))

    def test_shared_banner_cannot_bypass_raw_motion_checks(self):
        self.source('UniUpdateBanner.kt', 'import androidx.compose.animation.core.*\nrememberInfiniteTransition()\nLiquidAnimatedSwitcher(transition = LiquidSwitcherTransition.LiquidMorph)')
        self.assertEqual(3, len(validate(self.root)))

    def test_sdk_scalar_bindings_are_allowed_but_raw_bindings_are_rejected(self):
        self.source('Card.kt', 'animateLiquidFloatAsState()\nanimateLiquidColorAsState()\nanimateLiquidDpAsState()')
        self.assertEqual([], validate(self.root))
        self.source('Card.kt', 'animateFloatAsState()\nanimateCustomAsState()')
        self.assertEqual(1, len(validate(self.root)))
