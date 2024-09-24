package audio.omgsoundboard.presentation.utils

sealed class UiEvent {
    data class Navigate (val route: String) : UiEvent()
    data class PopAndNavigate(val route: String) : UiEvent()
    object NavigateUp: UiEvent()
    object PopBackStack: UiEvent()
    data class ShowInfoDialog(val message: UiText, val action: InfoDialogActions = InfoDialogActions.NONE): UiEvent()
}

enum class InfoDialogActions{
    NONE,
    NAVIGATE_UP,
    REFRESH
}