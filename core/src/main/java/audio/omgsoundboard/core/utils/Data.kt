package audio.omgsoundboard.core.utils

import audio.omgsoundboard.core.R


data class AboutModel(
    val icon: Int,
    val title: Int,
    val url: String
)

object Data {
    val contribute = arrayListOf(
        AboutModel(icon = R.drawable.translate, title = R.string.translate, url = "https://hosted.weblate.org/engage/omgsoundboard/"),
        AboutModel(icon = R.drawable.report_a_problem, title = R.string.report_problem, url = "https://github.com/OMGSoundboard/android-app/issues/"),
        AboutModel(icon = R.drawable.view_source, title = R.string.view_source, url = "https://github.com/OMGSoundboard/android-app/"),
    )
    val contact = arrayListOf(
        AboutModel(icon = R.drawable.website, title = R.string.website, url = "https://omgsoundboard.audio/"),
        AboutModel(icon = R.drawable.e_mail, title = R.string.email, url = "mailto:marvin@omgsoundboard.audio"),
        AboutModel(icon = R.drawable.telegram, title = R.string.telegram, url = "https://t.me/omgsoundboard/"),
        AboutModel(icon = R.drawable.helpdesk, title = R.string.helpdesk, url = "https://help.omgsoundboard.audio/"),
    )
    val legal = arrayListOf(
        AboutModel(icon = R.drawable.privacy_policy, title = R.string.privacy_policy, url = "https://omgsoundboard.audio/assets/legal/OMGSoundboard_PrivacyPolicy.pdf"),
        AboutModel(icon = R.drawable.disclaimer, title = R.string.disclaimer, url = "https://omgsoundboard.audio/assets/legal/OMGSoundboard_Disclaimer.pdf"),
        AboutModel(icon = R.drawable.disclaimer, title = R.string.dmca, url = "mailto:marvin@omgsoundboard.audio?subject=DMCA"),
        AboutModel(icon = R.drawable.license, title = R.string.license, url = "https://github.com/OMGSoundboard/android-app/blob/trunk/LICENSE/"),
    )
}