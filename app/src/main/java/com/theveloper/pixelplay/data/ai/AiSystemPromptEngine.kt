package com.theveloper.pixelplay.data.ai


import javax.inject.Inject
import javax.inject.Singleton

enum class AiSystemPromptType {
    PLAYLIST,
    METADATA,
    TAGGING,
    MOOD_ANALYSIS,
    PERSONA,
    GENERAL
}

@Singleton
class AiSystemPromptEngine @Inject constructor() {

    fun buildPrompt(basePersona: String, type: AiSystemPromptType, context: String = ""): String {
        // AI Optimization: Layered prompts ensure strict format adherence while preserving personality
        val requirementLayer = when (type) {
            AiSystemPromptType.PLAYLIST -> """
                ---
                MUSICAL OBJECTIVE:
                Create a cohesive sonic journey. Prioritize track flow, harmonic compatibility, and genre-appropriate energy progression.
                
                STRICT OUTPUT RULES:
                1. Your response MUST be ONLY a raw JSON array of song IDs.
                2. NO markdown code blocks (no ```json).
                3. NO conversational text, NO explanations.
                4. Example: ["id1", "id2", "id3"]
                5. If no suitable match is found, return an empty array [].
            """.trimIndent()

            AiSystemPromptType.METADATA -> """
                ---
                OBJECTIVE:
                Provide highly accurate metadata based on the provided song info. Use standard naming conventions. 
                For genres, be specific (e.g., 'Synthwave' instead of just 'Electronic').
                
                STRICT OUTPUT RULES:
                1. Your response MUST be ONLY a raw JSON object matching this schema: 
                   {"title": "...", "artist": "...", "album": "...", "genre": "..."}
                2. Fill in ONLY the requested fields, use null or empty string for others.
                3. NO markdown, NO conversational text.
            """.trimIndent()

            AiSystemPromptType.TAGGING -> """
                ---
                STRICT OUTPUT RULES:
                1. Provide 5-8 descriptive, evocative tags. 
                2. Mix technical (e.g. '808-heavy', 'reverb-drenched') with atmospheric (e.g. 'liminal', 'neon-lit', 'melancholic').
                3. Return as a CSV string. No JSON, no markdown.
                4. Example: lo-fi, chill, nocturnal, rainy, study, vinyl-crackle
            """.trimIndent()

            AiSystemPromptType.MOOD_ANALYSIS -> """
                ---
                STRICT OUTPUT RULES:
                1. Return a single word for the primary mood.
                2. Provide 0-1 scores for Energy, Valence, Danceability, and Acousticness.
                3. Format: Mood | Energy:0.X | Valence:0.X | Danceability:0.X | Acousticness:0.X
            """.trimIndent()

            AiSystemPromptType.PERSONA -> """
                ---
                INSTRUCTIONS:
                1. Adopt the persona of a sophisticated musical curator and sonic expert.
                2. Use descriptive, slightly poetic language when describing music.
                3. Keep responses concise but impactful.
                4. Reference the user's listening profile metrics to make responses feel personal.
            """.trimIndent()

            AiSystemPromptType.GENERAL -> """
                ---
                STRICT OUTPUT RULES:
                1. Respond clearly and concisely.
                2. If the user asks about music, provide expert insights.
            """.trimIndent()
        }

        // AI Integration: Inject real-time user metrics and playback history for deep personalization
        val contextLayer = if (context.isNotBlank()) {
            "--- USER_CONTEXT_START ---\n$context\n--- USER_CONTEXT_END ---"
        } else ""

        // AI Optimization: The core prompt is assembled from persona, context, and specialized requirements
        return """
            $basePersona
            
            $contextLayer
            
            $requirementLayer
        """.trimIndent()
    }
}
