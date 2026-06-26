with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

old_call = """                    KudoViewModel.VIEW_TASKS -> PullComposerPage(
                        uiState = uiState,
                        palette = palette,"""
new_call = """                    KudoViewModel.VIEW_TASKS -> PullComposerPage(
                        currentView = uiState.currentView,
                        taskCreationTarget = uiState.taskCreationTarget,
                        storeMode = uiState.storeMode,
                        palette = palette,"""
content = content.replace(old_call, new_call)

old_call2 = """                    KudoViewModel.VIEW_STORE -> PullComposerPage(
                        uiState = uiState,
                        palette = palette,"""
new_call2 = """                    KudoViewModel.VIEW_STORE -> PullComposerPage(
                        currentView = uiState.currentView,
                        taskCreationTarget = uiState.taskCreationTarget,
                        storeMode = uiState.storeMode,
                        palette = palette,"""
content = content.replace(old_call2, new_call2)

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

