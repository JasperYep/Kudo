with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

old_call = """                        TasksPage(
                            uiState = uiState,
                            palette = palette,"""
new_call = """                        TasksPage(
                            tasks = uiState.data.tasks,
                            multiplier = uiState.data.multiplier,
                            taskSortMode = uiState.data.taskSortMode,
                            habitsCollapsed = uiState.habitsCollapsed,
                            isHabitJiggleMode = uiState.isHabitJiggleMode,
                            recentTaskInsertId = uiState.recentTaskInsertId,
                            palette = palette,"""

content = content.replace(old_call, new_call)

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

