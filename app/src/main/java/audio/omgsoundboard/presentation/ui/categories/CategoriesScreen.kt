package audio.omgsoundboard.presentation.ui.categories

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.Category
import audio.omgsoundboard.presentation.composables.AddRenameDialog
import audio.omgsoundboard.presentation.composables.CategoryItem
import audio.omgsoundboard.presentation.composables.Fab
import audio.omgsoundboard.presentation.composables.InfoDialog
import audio.omgsoundboard.presentation.composables.SimpleDropMenu
import audio.omgsoundboard.presentation.utils.UiEvent

@Composable
fun CategoriesScreen(
    onNavigateUp: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
){

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateUp -> onNavigateUp()
                else -> Unit
            }
        }
    }


    val state by viewModel.state.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        CategoriesScreenContent(state, viewModel::onEvent)
        Fab(
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
        ) {
           viewModel.onEvent(CategoriesEvents.OnShowHideAddRenameCategoryDialog("", false))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreenContent(
    state: CategoriesState,
    onEvents: (CategoriesEvents) -> Unit,
){
    var touchPoint: Offset by remember { mutableStateOf(Offset.Zero) }
    var picketCategory by remember { mutableStateOf<Category?>(null) }

    Column {
        TopAppBar(
            title = {
                Text(text = stringResource(R.string.categories_header))
            },
            navigationIcon = {
                IconButton(onClick = {
                    onEvents(CategoriesEvents.OnNavigateUp)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null,
                    )
                }
            }
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ){
            itemsIndexed(state.categories, key = { _, category -> category.id }) { index, category ->
                CategoryItem(
                    item = category,
                    index = index,
                    onDropMenu = {
                        touchPoint = it
                        picketCategory = category
                        onEvents(CategoriesEvents.OnToggleDropMenu)
                    }
                )
            }
        }
    }

    if (state.showDropMenu) {
        SimpleDropMenu (
            touchPoint = touchPoint,
            onRename = {
                onEvents(CategoriesEvents.OnShowHideAddRenameCategoryDialog(picketCategory!!.name, true))
            },
            onDelete = {
                onEvents(CategoriesEvents.OnShowHideDeleteCategoryDialog)
            },
            onDismiss = {
                onEvents(CategoriesEvents.OnToggleDropMenu)
            },
        )
    }

    if (state.showAddRenameCategoryDialog){
        AddRenameDialog(
            title = stringResource(id = if (state.isRenaming)  R.string.update_category else R.string.category_add_title),
            placeholderText = stringResource(id = R.string.category_add_placeholder),
            buttonText = stringResource(id = if (state.isRenaming) R.string.save else  R.string.add),
            text = state.textFieldValue,
            onChange = {
                onEvents(CategoriesEvents.OnTextFieldChange(it))
            },
            error = state.textFieldError,
            onFinish = {
                if (state.isRenaming){
                    onEvents(CategoriesEvents.OnConfirmRename(picketCategory!!))
                } else {
                    onEvents(CategoriesEvents.OnConfirmAdd)
                }
            },
            onDismiss = {
                onEvents(CategoriesEvents.OnShowHideAddRenameCategoryDialog("", false))
            }
        )
    }

    if (state.showConfirmDeleteDialog){
        InfoDialog(
            text = stringResource(R.string.delete_sound_confirm),
            onConfirmation = {
                onEvents(CategoriesEvents.OnConfirmDelete(picketCategory!!.id))
                onEvents(CategoriesEvents.OnShowHideDeleteCategoryDialog)
            },
            onDismissRequest = {
                onEvents(CategoriesEvents.OnShowHideDeleteCategoryDialog)
            }
        )
    }
}