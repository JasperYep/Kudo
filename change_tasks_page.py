import re

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

old_func_sig = """private fun TasksPage(
    uiState: KudoUiState,
    palette: KudoPalette,"""

new_func_sig = """private fun TasksPage(
    tasks: List<KudoTask>,
    multiplier: Float,
    taskSortMode: Int,
    habitsCollapsed: Boolean,
    isHabitJiggleMode: Boolean,
    recentTaskInsertId: Long?,
    palette: KudoPalette,"""

content = content.replace(old_func_sig, new_func_sig)

start_idx = content.find(new_func_sig) + len(new_func_sig)
end_idx = content.find("private fun StorePage", start_idx)
if end_idx == -1: end_idx = len(content)

tasks_body = content[start_idx:end_idx]

tasks_body = tasks_body.replace("uiState.data.tasks", "tasks")
tasks_body = tasks_body.replace("uiState.data.taskSortMode", "taskSortMode")
tasks_body = tasks_body.replace("uiState.data.multiplier", "multiplier")
tasks_body = tasks_body.replace("uiState.habitsCollapsed", "habitsCollapsed")
tasks_body = tasks_body.replace("uiState.isHabitJiggleMode", "isHabitJiggleMode")
tasks_body = tasks_body.replace("uiState.recentTaskInsertId", "recentTaskInsertId")

content = content[:start_idx] + tasks_body + content[end_idx:]

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

