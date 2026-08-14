package audio.omgsoundboard.core.utils

object Constants {

    const val ONBOARDING_SHOWN = "OnboardingShown"
    const val PARTICLES_STATUS = "ParticlesStatus"
    const val THEME_TYPE = "ThemeType"

    const val STOP_ON_RETAP = "StopOnRetap"
    const val STOP_ON_NEW_SOUND = "StopOnNewSound"
    /** Shared preference key for the selected sound sort order. */
    const val SOUND_SORT_ORDER = "SoundSortOrder"
    const val OPTIONS_CATEGORY = "Categories"
    const val OPTIONS_PARTICLES = "Particles"
    const val OPTIONS_THEME_PICKER =  "Picker"
    const val OPTIONS_ABOUT =  "About"
    const val OPTIONS_SYNC =  "Sync"
    const val OPTIONS_PLAYBACK_BEHAVIOR = "PlaybackBehavior"
    /** Drawer option identifier for opening the sort picker. */
    const val OPTIONS_SORT = "Sort"
    const val CATEGORIES_TABLE = "categories"
    const val SOUNDS_TABLE = "sounds"

    const val WEAR_CAPABILITY = "omgsoundboard_wear"
    const val METADATA_PATH = "/metadata"
    const val METADATA_KEY = "metadata_key"
    /** Wear channel prefix for transferring audio files. */
    const val AUDIO_TRANSFER_PREFIX = "/audio_transfer"
    /** Legacy Wear channel prefix retained for older phone builds. */
    const val LEGACY_MP3_TRANSFER_PREFIX = "/mp3_transfer"
}