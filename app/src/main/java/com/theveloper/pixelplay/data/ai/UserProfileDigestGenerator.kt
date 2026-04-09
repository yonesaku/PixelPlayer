package com.theveloper.pixelplay.data.ai


import com.theveloper.pixelplay.data.database.EngagementDao
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.stats.PlaybackStatsRepository
import com.theveloper.pixelplay.data.stats.StatsTimeRange
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileDigestGenerator @Inject constructor(
    private val engagementDao: EngagementDao,
    private val statsRepository: PlaybackStatsRepository
) {
    /**
     * Computes a highly condensed representation of the user's listening profile.
     * Uses a compact key-value format to minimize token consumption while maximizing signal.
     */
    suspend fun generateDigest(allSongs: List<Song>): String {
        // AI Optimization: Load summaries for both all-time and recent activity to detect trends
        val summary = statsRepository.loadSummary(StatsTimeRange.ALL_TIME, allSongs)
        val recentSummary = statsRepository.loadSummary(StatsTimeRange.WEEK, allSongs)
        
        val sb = StringBuilder()
        // Simplified name for identification
        sb.append("USER_PROFILE\n")
        sb.append("TOP_GENRES: ${summary.topGenres.take(5).joinToString(",") { it.genre }}\n")
        sb.append("TOP_ARTISTS: ${summary.topArtists.take(5).joinToString(",") { it.artist }}\n")
        
        // AI Optimization: Analyze time-of-day listening patterns for context-aware recommendations
        summary.dayListeningDistribution?.let { dist ->
            val peakBuckets = dist.buckets.sortedByDescending { it.totalDurationMs }.take(3)
            val peaks = peakBuckets.joinToString(",") { "${it.startMinute/60}h" }
            sb.append("PEAK_HOURS: $peaks\n")
            
            // Phase Affinity (Morning, Afternoon, Evening, Night) - helps AI choose appropriate energy levels
            val phases = dist.buckets.groupBy { bucket ->
                val hour = bucket.startMinute / 60
                when (hour) {
                    in 5..10 -> "Morning"
                    in 11..16 -> "Afternoon"
                    in 17..22 -> "Evening"
                    else -> "Night"
                }
            }.mapValues { it.value.sumOf { b -> b.totalDurationMs } }
            val dominantPhase = phases.maxByOrNull { it.value }?.key ?: "Unknown"
            sb.append("PHASE_AFFINITY: $dominantPhase\n")
        }
        
        // AI Integration: Discovery Velocity indicates how much new music the user is consuming
        val totalPlays = summary.totalPlayCount
        val discoveryPlays = recentSummary.totalPlayCount
        val discoveryVelocity = if (totalPlays > 0) (discoveryPlays.toDouble() / totalPlays) else 0.0
        sb.append("DISCOVERY_VELOCITY: ${"%.2f".format(discoveryVelocity)} (High=Exploring, Low=Stagnant)\n")
        
        // Behavior stats for personalized tone
        val avgSessionMin = summary.averageSessionDurationMs / (1000 * 60)
        sb.append("BEHAVIOR: AvgSession=${avgSessionMin}m, TotalSessions=${summary.totalSessions}, Streak=${summary.longestStreakDays}d\n")
        
        // AI Integration: Variety Score helps AI decide between safe hits and deep cuts
        val varietyRatio = if (summary.totalPlayCount > 0) (summary.uniqueSongs.toDouble() / summary.totalPlayCount) else 0.0
        sb.append("VARIETY_SCORE: ${"%.2f".format(varietyRatio)} (1.0=pure variety, 0.1=repeater)\n")
        
        // Temporal Focus (Weekday vs Weekend)
        val peakDay = summary.peakDayLabel ?: "Unknown"
        sb.append("TEMPORAL_FOCUS: PeakDay=$peakDay\n")
        
        // Recent "Vibe"
        val recentTracks = summary.topSongs.take(5).joinToString(" | ") { "${it.title}-${it.artist}" }
        sb.append("CURRENT_FAVORITES: $recentTracks\n")
        
        return sb.toString()
    }
}
