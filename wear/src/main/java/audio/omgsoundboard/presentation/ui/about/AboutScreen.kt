package audio.omgsoundboard.presentation.ui.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.utils.Data
import audio.omgsoundboard.presentation.composables.Chip


@Composable
fun AboutScreen(){

    val context = LocalContext.current
    val scalingLazyListState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        state = scalingLazyListState
    ){
        item {
            Row(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(color = MaterialTheme.colors.surface)) {
                Text(modifier = Modifier.padding(horizontal = 16.dp) ,text = stringResource(id = R.string.about_header), textAlign = TextAlign.Center)
            }
        }

        item {
            Text(text = stringResource(id = R.string.contribute))
        }
        items(Data.contribute){
            Chip(icon = it.icon, title = stringResource(it.title)) {
                launchUrl(context, it.url)
            }
        }
        item {
            Text(text = stringResource(id = R.string.contact))
        }
        items(Data.contact){
            Chip(icon = it.icon, title = stringResource(it.title)) {
                launchUrl(context, it.url)
            }
        }
        item {
            Text(text = stringResource(id = R.string.legal))
        }
        items(Data.legal){
            Chip(icon = it.icon, title = stringResource(it.title)) {
                launchUrl(context, it.url)
            }
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