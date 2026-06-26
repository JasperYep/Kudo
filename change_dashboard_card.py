import re

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

old_func_sig = """private fun DashboardCard(
    uiState: KudoUiState,
    palette: KudoPalette,
    revealProgress: Float,
    title: String,
    value: String,
    onTitleChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onModeToggle: () -> Unit,
    onAdd: () -> Boolean
) {"""

new_func_sig = """private fun DashboardCard(
    currentView: String,
    taskCreationTarget: TaskCreationTarget,
    storeMode: Int,
    palette: KudoPalette,
    revealProgress: Float,
    title: String,
    value: String,
    onTitleChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onModeToggle: () -> Unit,
    onAdd: () -> Boolean
) {"""

content = content.replace(old_func_sig, new_func_sig)

# Now replace usages of uiState
# In DashboardCard:
# uiState.currentView -> currentView
# uiState.taskCreationTarget -> taskCreationTarget
# uiState.storeMode -> storeMode

import textwrap

# We only want to replace uiState. in DashboardCard. So we will find the body of DashboardCard
start_idx = content.find(new_func_sig) + len(new_func_sig)
end_idx = content.find("private fun ", start_idx)
if end_idx == -1: end_idx = len(content)

dashboard_body = content[start_idx:end_idx]
dashboard_body = dashboard_body.replace("uiState.currentView", "currentView")
dashboard_body = dashboard_body.replace("uiState.taskCreationTarget", "taskCreationTarget")
dashboard_body = dashboard_body.replace("uiState.storeMode", "storeMode")

content = content[:start_idx] + dashboard_body + content[end_idx:]

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

