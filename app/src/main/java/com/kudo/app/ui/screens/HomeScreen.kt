package com.kudo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kudo.app.domain.model.Task
import com.kudo.app.domain.model.TaskType
import com.kudo.app.domain.model.TaskList
import com.kudo.app.ui.components.Header
import com.kudo.app.ui.components.Dashboard
import com.kudo.app.ui.components.TaskItem
import com.kudo.app.ui.components.HabitItem
import com.kudo.app.ui.components.BottomNavigation
import com.kudo.app.ui.theme.LightBackground
import com.kudo.app.ui.theme.DarkBackground

@Composable
fun HomeScreen() {
    var currentView by remember { mutableStateOf("tasks") }
    var tasks by remember { mutableStateOf(emptyList<Task>()) }
    var habits by remember { mutableStateOf(emptyList<Task>()) }
    var coins by remember { mutableStateOf(0) }
    var life by remember { mutableStateOf(0) }
    var multiplier by remember { mutableStateOf(1.0f) }
    var level by remember { mutableStateOf(1) }
    var xpProgress by remember { mutableStateOf(0f) }
    var listMode by remember { mutableStateOf("focus") } // focus | inbox
    
    // Mock data - 后续会从数据库加载
    LaunchedEffect(Unit) {
        // TODO: Load from database
        tasks = listOf(
            Task(id = 1, title = "Complete project proposal", value = 50, type = TaskType.TASK, list = TaskList.FOCUS),
            Task(id = 2, title = "Review code changes", value = 30, type = TaskType.TASK, list = TaskList.INBOX),
            Task(id = 3, title = "Write documentation", value = 40, type = TaskType.TASK, list = TaskList.FOCUS)
        )
        
        habits = listOf(
            Task(id = 4, title = "Morning exercise", value = 20, type = TaskType.HABIT, list = TaskList.FOCUS),
            Task(id = 5, title = "Read 30 minutes", value = 15, type = TaskType.HABIT, list = TaskList.FOCUS)
        )
        
        coins = 150
        life = 350
        multiplier = 1.05f
        level = 2
        xpProgress = 0.5f
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                LightBackground // TODO: Get from theme
            )
    ) {
        // Header
        Header(
            coins = coins,
            level = level,
            xpProgress = xpProgress,
            multiplier = multiplier
        )
        
        // Content
        when (currentView) {
            "tasks" -> TaskView(
                tasks = tasks,
                habits = habits,
                listMode = listMode,
                onListModeChange = { listMode = it },
                onTaskComplete = { /* TODO */ },
                onHabitCharge = { /* TODO */ }
            )
            "store" -> StoreView()
            "log" -> LogView()
        }
        
        // Bottom Navigation
        BottomNavigation(
            currentView = currentView,
            onNavigate = { currentView = it }
        )
    }
}

@Composable
fun TaskView(
    tasks: List<Task>,
    habits: List<Task>,
    listMode: String,
    onListModeChange: (String) -> Unit,
    onTaskComplete: (Task) -> Unit,
    onHabitCharge: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for bottom nav
    ) {
        // Dashboard
        item {
            Dashboard(
                onAddTask = { title, value, mode -> 
                    // TODO: Add task
                },
                mode = listMode,
                onModeToggle = { 
                    onListModeChange(if (listMode == "focus") "inbox" else "focus")
                }
            )
        }
        
        // Habits
        if (habits.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Text(
                    text = "Habits",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(habits) { habit ->
                HabitItem(
                    habit = habit,
                    onCharge = { onHabitCharge(habit) }
                )
            }
        }
        
        // Tasks
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Text(
                text = if (listMode == "focus") "Focus" else "Inbox",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        items(tasks.filter { it.list.toString().lowercase() == listMode }) { task ->
            TaskItem(
                task = task,
                onComplete = { /* TODO */ }
            )
        }
    }
}

@Composable
fun StoreView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Store - Coming Soon")
    }
}

@Composable
fun LogView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Log - Coming Soon")
    }
}
