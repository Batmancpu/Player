package com.batmancpu.player.ui.library

import androidx.activity.ComponentActivity

/**
 * Empty debug-only host for synthetic Compose instrumentation screens.
 * Keeping tests out of MainActivity avoids real startup and persisted-library state racing
 * test-owned setContent calls. This class is excluded from release variants.
 */
class PreviewTestActivity : ComponentActivity()
