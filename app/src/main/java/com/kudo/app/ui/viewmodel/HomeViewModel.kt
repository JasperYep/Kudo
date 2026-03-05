package com.kudo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kudo.app.data.entity.*
import com.kudo.app.data.repository.*
import com.kudo.app.domain.GameMechanics
import com.kudo.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray

class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val storeRepository: StoreRepository,
    private val logRepository: LogRepository,
    private val userStatsRepository: UserStatsRepository,
    private val gameMechanics: GameMechanics
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadUserStats()
        loadTasks()
        loadStoreItems()
    }
    
    private fun loadUserStats() {
        viewModelScope.launch {
            userStatsRepository.userStats.collect { stats ->
                stats?.let {
                    _uiState.update { state ->
                        state.copy(
                            coins = it.coins,
                            life = it.life,
                            multiplier = it.multiplier,
                            maxCoins = it.maxCoins,
                            level = gameMechanics.calculateLevel(it.life),
                            xpProgress = gameMechanics.calculateXpProgress(it.life)
                        )
                    }
                }
            }
        }
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            taskRepository.getTasksByTypeAndList(0, "focus").collect { tasks ->
                _uiState.update { state ->
                    state.copy(focusTasks = tasks.map { it.toDomainTask() })
                }
            }
        }
        
        viewModelScope.launch {
            taskRepository.getTasksByTypeAndList(0, "inbox").collect { tasks ->
                _uiState.update { state ->
                    state.copy(inboxTasks = tasks.map { it.toDomainTask() })
                }
            }
        }
        
        viewModelScope.launch {
            taskRepository.getTasksByTypeAndList(1, "focus").collect { tasks ->
                _uiState.update { state ->
                    state.copy(habits = tasks.map { it.toDomainTask() })
                }
            }
        }
    }
    
    private fun loadStoreItems() {
        viewModelScope.launch {
            storeRepository.allItems.collect { items ->
                _uiState.update { state ->
                    state.copy(storeItems = items.map { it.toDomainStoreItem() })
                }
            }
        }
    }
    
    fun addTask(title: String, value: Int, list: String) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                value = value,
                type = 0, // Task
                list = list
            )
            taskRepository.insert(task)
        }
    }
    
    fun completeTask(task: Task) {
        viewModelScope.launch {
            val reward = gameMechanics.calculateReward(task.value, _uiState.value.multiplier)
            
            // Update coins
            val currentStats = userStatsRepository.getUserStats()
            currentStats?.let { stats ->
                val newCoins = stats.coins + reward
                val newLife = stats.life + task.value
                val recentValues = try {
                    JSONArray(stats.recentValues).let { json ->
                        List(json.length()) { json.getInt(it) }
                    }
                } catch (e: Exception) {
                    emptyList()
                }
                
                val newMultiplier = gameMechanics.updateMultiplier(
                    stats.multiplier,
                    recentValues,
                    task.value
                )
                val newMax = maxOf(stats.maxCoins, newCoins)
                val newRecentValues = (recentValues + task.value).takeLast(5)
                
                userStatsRepository.updateStats(
                    coins = newCoins,
                    life = newLife,
                    multiplier = newMultiplier,
                    maxCoins = newMax,
                    recentValues = JSONArray(newRecentValues).toString()
                )
            }
            
            // Add log
            logRepository.insert(
                LogEntity(
                    description = task.title,
                    value = reward,
                    taskId = task.id,
                    beforeCoins = _uiState.value.coins,
                    afterCoins = _uiState.value.coins + reward
                )
            )
            
            // Mark task as completed
            taskRepository.updateCompleted(task.id, true)
        }
    }
    
    fun addHabit(title: String, value: Int) {
        viewModelScope.launch {
            val habit = TaskEntity(
                title = title,
                value = value,
                type = 1 // Habit
            )
            taskRepository.insert(habit)
        }
    }
    
    fun completeHabit(habit: Task) {
        viewModelScope.launch {
            val reward = gameMechanics.calculateReward(habit.value, _uiState.value.multiplier)
            
            // Update stats (same as task)
            val currentStats = userStatsRepository.getUserStats()
            currentStats?.let { stats ->
                val newCoins = stats.coins + reward
                val newLife = stats.life + habit.value
                val newMax = maxOf(stats.maxCoins, newCoins)
                val recentValues = try {
                    JSONArray(stats.recentValues).let { json ->
                        List(json.length()) { json.getInt(it) }
                    }
                } catch (e: Exception) {
                    emptyList()
                }
                
                userStatsRepository.updateStats(
                    coins = newCoins,
                    life = newLife,
                    multiplier = stats.multiplier,
                    maxCoins = newMax,
                    recentValues = JSONArray(recentValues).toString()
                )
            }
            
            // Update habit count
            val updatedHabit = habit.copy(
                habitCount = habit.habitCount + 1,
                lastCompletedTime = System.currentTimeMillis()
            )
            taskRepository.update(updatedHabit.toEntityTask())
            
            // Add log
            logRepository.insert(
                LogEntity(
                    description = habit.title,
                    value = reward,
                    taskId = habit.id,
                    isHabit = true,
                    beforeCoins = _uiState.value.coins,
                    afterCoins = _uiState.value.coins + reward
                )
            )
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteById(task.id)
        }
    }
    
    fun deleteHabit(habit: Task) {
        viewModelScope.launch {
            taskRepository.deleteById(habit.id)
        }
    }
    
    fun purchaseItem(item: StoreItem): Boolean {
        return if (_uiState.value.coins >= item.cost) {
            viewModelScope.launch {
                val currentStats = userStatsRepository.getUserStats()
                currentStats?.let { stats ->
                    val recentValues = try {
                        JSONArray(stats.recentValues).let { json ->
                            List(json.length()) { json.getInt(it) }
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }
                    
                    userStatsRepository.updateStats(
                        coins = stats.coins - item.cost,
                        life = stats.life,
                        multiplier = stats.multiplier,
                        maxCoins = stats.maxCoins,
                        recentValues = JSONArray(recentValues).toString()
                    )
                }
                
                storeRepository.purchaseItem(item.id)
                
                // Add log
                logRepository.insert(
                    LogEntity(
                        description = "Purchase: ${item.name}",
                        value = -item.cost,
                        beforeCoins = _uiState.value.coins,
                        afterCoins = _uiState.value.coins - item.cost
                    )
                )
            }
            true
        } else {
            false
        }
    }
    
    fun toggleListMode() {
        _uiState.update { state ->
            state.copy(
                listMode = if (state.listMode == "focus") "inbox" else "focus"
            )
        }
    }
    
    fun switchView(view: String) {
        _uiState.update { state ->
            state.copy(currentView = view)
        }
    }
    
    private fun calculateLevel(life: Int): Int {
        return gameMechanics.calculateLevel(life)
    }
    
    private fun calculateXpProgress(life: Int): Float {
        return gameMechanics.calculateXpProgress(life)
    }
}

data class HomeUiState(
    val coins: Int = 0,
    val life: Int = 0,
    val multiplier: Float = 1.0f,
    val maxCoins: Int = 0,
    val level: Int = 1,
    val xpProgress: Float = 0f,
    val currentView: String = "tasks",
    val listMode: String = "focus",
    val focusTasks: List<Task> = emptyList(),
    val inboxTasks: List<Task> = emptyList(),
    val habits: List<Task> = emptyList(),
    val storeItems: List<StoreItem> = emptyList(),
    val logs: List<Log> = emptyList()
)

// Extension functions
fun TaskEntity.toDomainTask(): Task {
    return Task(
        id = id,
        title = title,
        value = value,
        type = if (type == 0) TaskType.TASK else TaskType.HABIT,
        list = if (list == "focus") TaskList.FOCUS else TaskList.INBOX,
        isCompleted = isCompleted,
        createdAt = createdAt,
        habitChargeTime = habitChargeTime,
        habitCount = habitCount,
        lastCompletedTime = lastCompletedTime,
        order = order
    )
}

fun Task.toEntityTask(): com.kudo.app.data.entity.TaskEntity {
    return com.kudo.app.data.entity.TaskEntity(
        id = id,
        title = title,
        value = value,
        type = if (type == TaskType.TASK) 0 else 1,
        list = if (list == TaskList.FOCUS) "focus" else "inbox",
        isCompleted = isCompleted,
        createdAt = createdAt,
        habitChargeTime = habitChargeTime,
        habitCount = habitCount,
        lastCompletedTime = lastCompletedTime,
        order = order
    )
}

fun StoreItemEntity.toDomainStoreItem(): StoreItem {
    return StoreItem(
        id = id,
        name = name,
        cost = cost,
        description = description,
        icon = icon,
        isPurchased = isPurchased,
        createdAt = createdAt
    )
}

fun StoreItem.toEntityStoreItem(): com.kudo.app.data.entity.StoreItemEntity {
    return com.kudo.app.data.entity.StoreItemEntity(
        id = id,
        name = name,
        cost = cost,
        description = description,
        icon = icon,
        isPurchased = isPurchased,
        createdAt = createdAt
    )
}
