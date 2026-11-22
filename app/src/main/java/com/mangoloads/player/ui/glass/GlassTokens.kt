/*
 * Spring/inset values adapted from Batmancpu/brrMusic (GPL-3.0).
 * Copyright and source attribution are recorded in THIRD_PARTY_NOTICES.md.
 */
package com.mangoloads.player.ui.glass

import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.dp

internal val GlassSpring = spring<Float>(dampingRatio = .72f, stiffness = 320f)
internal val PILL_INSET = 4.dp
