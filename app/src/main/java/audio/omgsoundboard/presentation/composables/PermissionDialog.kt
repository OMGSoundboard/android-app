package audio.omgsoundboard.presentation.composables

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import audio.omgsoundboard.core.R

@Composable
fun PermissionDialog(result: (Boolean) -> Unit, onDismiss: () -> Unit){

    val context = LocalContext.current
    var hasWriteSettingsPermission by remember { mutableStateOf(Settings.System.canWrite(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.System.canWrite(context)) {
            hasWriteSettingsPermission = true
            showPermissionDialog = false
            Toast.makeText(
                context,
                context.resources.getString(R.string.permission_granted),
                Toast.LENGTH_SHORT
            ).show()
            result(true)
        } else {
            Toast.makeText(
                context,
                context.resources.getString(R.string.permission_not_granted),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 15.dp, vertical = 10.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                Text(modifier = Modifier.padding(bottom = 16.dp), text = stringResource(id = R.string.permission), fontSize = 20.sp)
                Text(text = stringResource(id = R.string.permission_description), textAlign = TextAlign.Justify)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        Intent(
                            Settings.ACTION_MANAGE_WRITE_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        ).also {
                            writePermissionLauncher.launch(it)
                        }
                    }
                ) {
                    Text(text = stringResource(id = R.string.permission_allow), fontSize = 18.sp)
                }
            }
        }
    }
}