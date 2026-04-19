package com.theveloper.pixelplay.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<String>,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = false,
    val isQueueGenerated: Boolean = false,
    val coverImageUri: String? = null,
    val coverColorArgb: Int? = null,
    val coverIconName: String? = null,
    val coverShapeType: String? = null, // "Circle", "SmoothRect", etc. Storing as String to avoid Enum import issues if moved
    val coverShapeDetail1: Float? = null, // e.g., CornerRadius / StarCurve
    val coverShapeDetail2: Float? = null, // e.g., Smoothness / StarRotation
    val coverShapeDetail3: Float? = null, // e.g., StarScale
    val coverShapeDetail4: Float? = null, // e.g., Star Sides (Int)
    val source: String = "LOCAL" // Source: "LOCAL", "NETEASE", "TELEGRAM", "AI", etc.
    val Playlist.displayName: String
    get() = name.substringBefore('\n').trim()

    val Playlist.description: String?
    get() = name.indexOf('\n').takeIf { it >= 0 }?.let {
        name.substring(it + 1).trim().ifBlank { null }
    }
)

enum class PlaylistShapeType {
    Circle,
    SmoothRect,
    RotatedPill,
    Star
}
