#!/usr/bin/env python3
"""Keep page motion in the shared host and animation implementation in Liquid Monet."""
from pathlib import Path
import re
import sys


def validate(root=None):
    root = Path(root) if root else Path(__file__).resolve().parents[1]
    errors = []
    for directory in (root / 'composeApp/src', root / 'androidApp/src', root / 'iosApp'):
        for path in directory.rglob('*') if directory.exists() else []:
            if path.suffix not in ('.kt', '.swift') or any(part.endswith('Test') for part in path.parts):
                continue
            code = re.sub(r'/\*.*?\*/|//[^\n]*', '', path.read_text(), flags=re.S)
            relative = path.relative_to(root).as_posix()
            if re.search(r'^\s*import androidx\.compose\.animation', code, re.M):
                errors.append(f'{relative}: Compose animation imports belong to the SDK')
            if re.search(r'\b(?:tween|spring|keyframes|infiniteRepeatable|rememberInfiniteTransition|'
                         r'Animatable|MutableTransitionState|updateTransition|rememberTransition|'
                         r'AnimatedContent|AnimatedVisibility|Crossfade|animate\w*AsState|'
                         r'fadeIn|fadeOut|scaleIn|scaleOut|slideIn\w*|slideOut\w*|'
                         r'expandVertically|shrinkVertically|withAnimation)\s*\(', code):
                errors.append(f'{relative}: replace local animation construction with SDK motion')
            if ('LiquidAnimatedSwitcher(' in code and path.name != 'UniUpdateBanner.kt') or (
                    'rememberLiquidContentTransition<' in code and path.name != 'AppNavigationHost.kt'):
                errors.append(f'{relative}: changing content must use a shared content host')
            if re.search(r'\bLiquid(?:Section|Screen)Entrance\s*\(', code):
                errors.append(f'{relative}: page entrances belong to the general navigation host')
            if re.search(r'LiquidSwitcherTransition\.(?!None\b)\w+', code) and path.name != 'UniMotion.kt':
                errors.append(f'{relative}: choose the SDK preset in UniMotion')
    return errors


if __name__ == '__main__':
    errors = validate(sys.argv[1] if len(sys.argv) > 1 else None)
    for error in errors:
        print(error, file=sys.stderr)
    print(f'Motion ownership: {len(errors)} violations.')
    raise SystemExit(bool(errors))
