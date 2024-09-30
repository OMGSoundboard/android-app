package audio.omgsoundboard.presentation.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import audio.omgsoundboard.core.R
import audio.omgsoundboard.presentation.composables.PermissionDialog
import kotlinx.coroutines.launch


@Composable
fun OnboardingScreen(
    setOnboardingAsShown: () -> Unit,
) {

    var showPermissionDialog by remember { mutableStateOf(false) }


    val pagerState = rememberPagerState(pageCount = { OnboardingElements.elements.size })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
        ) { index ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier.scale(0.9f).border(border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)),
                    painter = painterResource(id = OnboardingElements.elements[index].drawableId),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(id = OnboardingElements.elements[index].description),
                    textAlign = TextAlign.Center
                )
                if (OnboardingElements.elements[index].showPermissionButton){
                    Button(onClick = {
                        showPermissionDialog = true
                    }) {
                        Text(text = stringResource(id = R.string.permission_allow))
                    }
                }
            }
        }

        if (pagerState.canScrollForward) {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .align(Alignment.TopEnd)
                    .clickable {
                        setOnboardingAsShown()
                    },
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.onboarding_skip),
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .align(Alignment.BottomCenter)
        ) {
            if (pagerState.canScrollBackward) {
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterStart)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                    text = stringResource(id = R.string.onboarding_back),
                    textAlign = TextAlign.Center
                )
            }

            Indicator(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.Center),
                currentPage = pagerState.currentPage
            )

            Text(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .align(Alignment.CenterEnd)
                    .clickable {
                        if (pagerState.canScrollForward) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            setOnboardingAsShown()
                        }
                    },
                text = stringResource(
                    id = if (pagerState.canScrollForward) {
                        R.string.onboarding_next
                    } else {
                        R.string.onboarding_finish
                    }
                ),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showPermissionDialog) {
        PermissionDialog(
            result = {
                showPermissionDialog = false
            },
            onDismiss = {
                showPermissionDialog = false
            }
        )
    }
}


@Composable
fun Indicator(modifier: Modifier, currentPage: Int) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        OnboardingElements.elements.forEachIndexed { index, _ ->
            Circle(
                color = if (currentPage >= index) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.White
                }
            )
        }
    }
}

@Composable
fun Circle(color: Color) {
    Canvas(
        modifier = Modifier
            .size(size = 15.dp)
            .padding(horizontal = 4.dp)
    ) {
        drawCircle(
            color = color
        )
    }
}

data class OnboardingModel(
    @StringRes val description: Int,
    @DrawableRes val drawableId: Int,
    val showPermissionButton: Boolean = false,
)

object OnboardingElements {
    val elements = arrayListOf(
        OnboardingModel(
            description = R.string.onboarding_text_1,
            drawableId = audio.omgsoundboard.R.drawable.onboarding_img_1,
        ),
        OnboardingModel(
            description = R.string.onboarding_text_2,
            drawableId = audio.omgsoundboard.R.drawable.onboarding_img_2
        ),
        OnboardingModel(
            description = R.string.onboarding_text_3,
            drawableId = audio.omgsoundboard.R.drawable.onboarding_img_3,
            showPermissionButton = true
        ),
        OnboardingModel(
            description = R.string.onboarding_text_4,
            drawableId = audio.omgsoundboard.R.drawable.onboarding_img_4
        ),
        OnboardingModel(
            description = R.string.onboarding_text_5,
            drawableId = audio.omgsoundboard.R.drawable.onboarding_img_5
        ),
        OnboardingModel(
            description = R.string.onboarding_text_6,
            drawableId = audio.omgsoundboard.R.drawable.onboarding_img_6
        ),
        OnboardingModel(
            description = R.string.onboarding_text_7,
            drawableId = audio.omgsoundboard.R.drawable.onboarding_img_7
        )
    )
}