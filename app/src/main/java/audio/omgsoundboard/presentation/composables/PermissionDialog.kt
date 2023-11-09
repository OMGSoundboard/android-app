package audio.omgsoundboard.presentation.composables

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                Row {
                    Button(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(12.dp),
                        onClick = onDismiss
                    ) {
                        Text(text = stringResource(id = R.string.permission_cancel), fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        modifier = Modifier.weight(1f),
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
}