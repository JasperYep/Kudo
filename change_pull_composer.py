import re

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

old_func_sig = """private fun PullComposerPage(
    uiState: KudoUiState,
    palette: KudoPalette,
    title: String,
    value: String,
    listState: LazyListState,"""

new_func_sig = """private fun PullComposerPage(
    currentView: String,
    taskCreationTarget: TaskCreationTarget,
    storeMode: Int,
    palette: KudoPalette,
    title: String,
    value: String,
    listState: LazyListState,"""

content = content.replace(old_func_sig, new_func_sig)

# Inside PullComposerPage, DashboardCard is called.
old_dashboard_call = """                DashboardCard(
                    uiState = uiState,
                    palette = palette,"""
new_dashboard_call = """                DashboardCard(
                    currentView = currentView,
                    taskCreationTarget = taskCreationTarget,
                    storeMode = storeMode,
                    palette = palette,"""

content = content.replace(old_dashboard_call, new_dashboard_call)

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

