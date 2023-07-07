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
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.utils.Data
import audio.omgsoundboard.presentation.composables.AboutItem
import audio.omgsoundboard.presentation.composables.Licenses
import audio.omgsoundboard.presentation.composables.Particles
import audio.omgsoundboard.presentation.navigation.Screens
import audio.omgsoundboard.presentation.ui.MainViewModel

@Composable
fun AboutScreen(mainViewModel: MainViewModel){

    val context = LocalContext.current

    LaunchedEffect(Unit){
        mainViewModel.setCurrentScreenValue(Screens.AboutScreen, context.getString(R.string.about_title))
    }

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

                    Data.contribute.forEach {
                        AboutItem(icon = it.icon, title = it.title) {
                            launchUrl(context, it.url)
                        }
                    }

                    Text(
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                        text = stringResource(id = R.string.contact)
                    )

                    Data.contact.forEach {
                        AboutItem(icon = it.icon, title = it.title) {
                            launchUrl(context, it.url)
                        }
                    }

                    Text(
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                        text = stringResource(id = R.string.legal)
                    )

                    Data.legal.forEach {
                        AboutItem(icon = it.icon, title = it.title) {
                            launchUrl(context, it.url)
                        }
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