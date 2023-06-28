package audio.omgsoundboard.presentation.ui.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import audio.omgsoundboard.R
import audio.omgsoundboard.presentation.composables.AboutItem
import audio.omgsoundboard.presentation.composables.Licenses
import audio.omgsoundboard.presentation.composables.Particles
import audio.omgsoundboard.presentation.navigation.Screens
import audio.omgsoundboard.presentation.ui.MainViewModel

@Composable
fun AboutScreen(mainViewModel: MainViewModel){

    LaunchedEffect(Unit){
        mainViewModel.setCurrentScreenValue(Screens.AboutScreen)
    }

    val context = LocalContext.current
    var showLicenses by remember{ mutableStateOf(false)}

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){

        if (mainViewModel.areParticlesEnabled){
            Particles()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp) ,text = stringResource(id = R.string.about_header))
            }

            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)) {

                Column(

                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                        text = stringResource(id = R.string.contribute)
                    )

                    AboutItem(icon = R.drawable.translate, title = R.string.translate) {
                        launchUrl(context, "https://hosted.weblate.org/engage/omgsoundboard/")
                    }
                    AboutItem(icon = R.drawable.report_a_problem, title = R.string.report_problem) {
                        launchUrl(context, "https://github.com/OMGSoundboard/android-app/issues/")
                    }
                    AboutItem(icon = R.drawable.view_source, title =  R.string.view_source) {
                        launchUrl(context, "https://github.com/OMGSoundboard/android-app/")
                    }


                    Text(
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                        text = stringResource(id = R.string.contact)
                    )

                    AboutItem(icon = R.drawable.website, title = R.string.website) {
                        launchUrl(context, "https://omgsoundboard.audio/")
                    }
                    AboutItem(icon = R.drawable.e_mail, title = R.string.email) {
                        launchUrl(context, "mailto:marvin@omgsoundboard.audio")
                    }
                    AboutItem(icon = R.drawable.telegram, title = R.string.telegram) {
                        launchUrl(context, "https://t.me/omgsoundboard/")
                    }
                    AboutItem(icon = R.drawable.helpdesk, title =  R.string.helpdesk) {
                        launchUrl(context, "https://help.omgsoundboard.audio/")
                    }

                    Text(
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                        text = stringResource(id = R.string.legal)
                    )

                    AboutItem(icon = R.drawable.terms_of_service, title = R.string.terms_of_service) {
                        launchUrl(context, "https://omgsoundboard.audio/assets/legal/OMGSoundboard_ToS.pdf")
                    }
                    AboutItem(icon = R.drawable.privacy_policy, title = R.string.privacy_policy) {
                        launchUrl(context, "https://omgsoundboard.audio/assets/legal/OMGSoundboard_PrivacyPolicy.pdf")
                    }
                    AboutItem(icon = R.drawable.disclaimer, title = R.string.disclaimer) {
                        launchUrl(context, "https://omgsoundboard.audio/assets/legal/OMGSoundboard_Disclaimer.pdf")
                    }
                    AboutItem(icon = R.drawable.disclaimer, title = R.string.dmca) {
                        launchUrl(context, "mailto:marvin@omgsoundboard.audio?subject=DMCA")
                    }
                    AboutItem(icon = R.drawable.license, title =  R.string.license) {
                        launchUrl(context, "https://github.com/OMGSoundboard/android-app/blob/trunk/LICENSE/")
                    }
                    AboutItem(icon = R.drawable.open_source_licenses, title =  R.string.open_source) {
                        showLicenses = true
                    }
                }
            }
        }
    }

    if (showLicenses){
        Licenses {
            showLicenses = false
        }
    }

}

private fun launchUrl(context: Context, url: String){
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException){
        Toast.makeText(context, context.getString(R.string.can_not_handle), Toast.LENGTH_SHORT).show()
    }
}