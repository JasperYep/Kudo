package com.kudo.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kudo.app.KudoApplication
import com.kudo.app.core.model.KudoReducer
import com.kudo.app.core.model.KudoState
import com.kudo.app.core.repository.KudoStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class KudoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KudoStateRepository =
        (application as KudoApplication).kudoStateRepository

    val themeMode: StateFlow<String> = repository.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = KudoStateRepository.THEME_SYSTEM
        )

    private val currentView = MutableStateFlow(VIEW_TASKS)
    private val taskCreationTarget = MutableStateFlow(TaskCreationTarget.INBOX)
    private val storeMode = MutableStateFlow(KudoState.STORE_ONCE)
    private val habitsCollapsed = MutableStateFlow(false)
    private val habitJiggleMode = MutableStateFlow(false)
    private val settingsVisible = MutableStateFlow(false)
    private val helpVisible = MutableStateFlow(false)
    private val editingTarget = MutableStateFlow<EditingTarget?>(null)
    private val pendingMoveTaskId = MutableStateFlow<Long?>(null)
    private val recentTaskInsertId = MutableStateFlow<Long?>(null)
    private val recentStoreInsertId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<KudoUiState> = combine(
        repository.state,
        repository.theme,
        currentView,
        taskCreationTarget,
        storeMode,
        habitsCollapsed,
        habitJiggleMode,
        settingsVisible,
        helpVisible,
        editingTarget,
        pendingMoveTaskId,
        recentTaskInsertId,
        recentStoreInsertId
    ) { values ->
        val state = values[0] as KudoState
        val theme = values[1] as String
        KudoUiState(
            data = state,
            theme = theme,
            currentView = values[2] as String,
            taskCreationTarget = values[3] as TaskCreationTarget,
            storeMode = values[4] as Int,
            habitsCollapsed = values[5] as Boolean,
            isHabitJiggleMode = values[6] as Boolean,
            isSettingsVisible = values[7] as Boolean,
            isHelpVisible = values[8] as Boolean,
            editingTarget = values[9] as EditingTarget?,
            pendingMoveTaskId = values[10] as Long?,
            recentTaskInsertId = values[11] as Long?,
            recentStoreInsertId = values[12] as Long?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KudoUiState()
    )

    fun switchView(view: String) {
        if (view != VIEW_TASKS) {
            habitJiggleMode.value = false
        }
        currentView.value = view
    }

    fun toggleListMode() {
        viewModelScope.launch {
            repository.updateState { state ->
                state.copy(
                    listMode = if (state.listMode == KudoState.LIST_FOCUS) {
                        KudoState.LIST_INBOX
                    } else {
                        KudoState.LIST_FOCUS
                    }
                )
            }
        }
    }

    fun setListMode(mode: String) {
        viewModelScope.launch {
            repository.updateState { it.copy(listMode = mode) }
        }
    }

    fun cycleTaskCreationTarget() {
        taskCreationTarget.value = when (taskCreationTarget.value) {
            TaskCreationTarget.INBOX,
            TaskCreationTarget.FOCUS -> TaskCreationTarget.HABIT
            TaskCreationTarget.HABIT -> TaskCreationTarget.INBOX
        }
    }

    fun toggleStoreMode() {
        storeMode.value = if (storeMode.value == KudoState.STORE_ONCE) {
            KudoState.STORE_INFINITE
        } else {
            KudoState.STORE_ONCE
        }
    }

    fun addDashboardItem(title: String, valueInput: String): Boolean {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) {
            return false
        }

        val current = uiState.value
        val parsedValue = parseDashboardValue(
            raw = valueInput,
            view = current.currentView
        )
        val createdId = System.currentTimeMillis()
        val isStoreInsert = current.currentView == VIEW_STORE
        val isTaskInsertAnimation = !isStoreInsert && current.taskCreationTarget != TaskCreationTarget.HABIT

        if (isStoreInsert) {
            recentStoreInsertId.value = createdId
        } else if (isTaskInsertAnimation) {
            recentTaskInsertId.value = createdId
        }

        viewModelScope.launch {
            repository.updateState { state ->
                if (isStoreInsert) {
                    KudoReducer.addStoreItem(
                        state = state,
                        title = trimmedTitle,
                        cost = parsedValue,
                        type = current.storeMode,
                        now = createdId
                    )
                } else {
                    KudoReducer.addTask(
                        state = state,
                        title = trimmedTitle,
                        value = parsedValue,
                        type = when (current.taskCreationTarget) {
                            TaskCreationTarget.HABIT -> KudoState.TYPE_HABIT
                            else -> KudoState.TYPE_TASK
                        },
                        list = when (current.taskCreationTarget) {
                            TaskCreationTarget.HABIT -> KudoState.LIST_FOCUS
                            TaskCreationTarget.FOCUS -> KudoState.LIST_FOCUS
                            TaskCreationTarget.INBOX -> KudoState.LIST_INBOX
                        },
                        now = createdId
                    )
                }
            }
        }
        if (isStoreInsert) {
            clearRecentStoreInsert(createdId)
        } else if (isTaskInsertAnimation) {
            clearRecentTaskInsert(createdId)
        }
        return true
    }

    private fun clearRecentTaskInsert(id: Long) {
        viewModelScope.launch {
            delay(380)
            if (recentTaskInsertId.value == id) {
                recentTaskInsertId.value = null
            }
        }
    }

    private fun clearRecentStoreInsert(id: Long) {
        viewModelScope.launch {
            delay(380)
            if (recentStoreInsertId.value == id) {
                recentStoreInsertId.value = null
            }
        }
    }

    fun completeTask(id: Long) {
        viewModelScope.launch {
            repository.updateState { state -> KudoReducer.completeTask(state, id) }
        }
    }

    fun completeHabit(id: Long) {
        viewModelScope.launch {
            repository.updateState { state -> KudoReducer.completeHabit(state, id) }
        }
    }

    fun moveTask(id: Long) {
        val current = uiState.value.data
        val result = KudoReducer.moveTask(current, id)
        if (result.requiresValue) {
            pendingMoveTaskId.value = id
            editingTarget.value = EditingTarget(kind = KIND_TASK, id = id)
            return
        }

        viewModelScope.launch {
            repository.saveState(result.state)
        }
    }

    fun moveTaskFromGesture(id: Long): Boolean {
        val current = uiState.value.data
        val result = KudoReducer.moveTask(current, id)
        if (result.requiresValue) {
            pendingMoveTaskId.value = id
            editingTarget.value = EditingTarget(kind = KIND_TASK, id = id)
            return false
        }

        viewModelScope.launch {
            repository.saveState(result.state)
        }
        return true
    }

    fun purchaseItem(id: Long) {
        viewModelScope.launch {
            repository.updateState { state -> KudoReducer.buyItem(state, id) }
        }
    }

    fun purchaseItemFromGesture(id: Long): Boolean {
        val current = uiState.value.data
        val item = current.store.firstOrNull { it.id == id } ?: return false
        if (current.coins < item.cost) {
            return false
        }

        viewModelScope.launch {
            repository.updateState { state -> KudoReducer.buyItem(state, id) }
        }
        return true
    }

    fun undoLog(index: Int) {
        viewModelScope.launch {
            repository.updateState { state -> KudoReducer.undoLog(state, index) }
        }
    }

    fun openEditTask(id: Long) {
        editingTarget.value = EditingTarget(kind = KIND_TASK, id = id)
    }

    fun openEditStore(id: Long) {
        editingTarget.value = EditingTarget(kind = KIND_STORE, id = id)
    }

    fun closeEdit() {
        editingTarget.value = null
        pendingMoveTaskId.value = null
    }

    fun saveEditing(title: String, valueInput: String, dueEpochDay: Long?) {
        val target = editingTarget.value ?: return
        val pendingTaskId = pendingMoveTaskId.value
        val sanitizedTitle = title.trim()
        val parsedValue = parseEditValue(valueInput)

        viewModelScope.launch {
            repository.updateState { state ->
                if (pendingTaskId != null) {
                    val task = state.tasks.firstOrNull { it.id == pendingTaskId } ?: return@updateState state
                    val updatedTitle = sanitizedTitle.ifBlank { task.title }
                    val updatedValue = if (valueInput.isBlank()) 10 else parsedValue
                    KudoReducer.moveTask(
                        state = KudoReducer.updateTask(
                        state = state,
                        id = pendingTaskId,
                        title = updatedTitle,
                        value = updatedValue,
                        dueEpochDay = dueEpochDay
                    ),
                    id = pendingTaskId,
                    assignedValueForInboxToFocus = updatedValue
                ).state
            } else {
                    when (target.kind) {
                        KIND_TASK -> KudoReducer.updateTask(
                            state = state,
                            id = target.id,
                            title = sanitizedTitle,
                            value = parsedValue,
                            dueEpochDay = dueEpochDay
                        )

                        KIND_STORE -> KudoReducer.updateStoreItem(
                            state = state,
                            id = target.id,
                            title = sanitizedTitle,
                            cost = parsedValue
                        )

                        else -> state
                    }
                }
            }
            closeEdit()
        }
    }

    fun deleteEditing() {
        val target = editingTarget.value ?: return
        viewModelScope.launch {
            repository.updateState { state ->
                when (target.kind) {
                    KIND_TASK -> KudoReducer.deleteTask(state, target.id)
                    KIND_STORE -> KudoReducer.deleteStoreItem(state, target.id)
                    else -> state
                }
            }
            closeEdit()
        }
    }

    fun openSettings() {
        habitJiggleMode.value = false
        settingsVisible.value = true
        helpVisible.value = false
    }

    fun closeSettings() {
        settingsVisible.value = false
        helpVisible.value = false
    }

    fun toggleHelp() {
        helpVisible.value = !helpVisible.value
    }

    fun toggleHabitsCollapsed() {
        habitsCollapsed.value = !habitsCollapsed.value
    }

    fun enterHabitJiggleMode() {
        habitJiggleMode.value = true
    }

    fun exitHabitJiggleMode() {
        habitJiggleMode.value = false
    }

    fun reorderCurrentTaskList(orderedIds: List<Long>) {
        viewModelScope.launch {
            repository.updateState { state ->
                KudoReducer.setTaskSortMode(
                    state = KudoReducer.reorderTasks(state, state.listMode, orderedIds),
                    listMode = state.listMode,
                    sortMode = KudoState.TASK_SORT_MANUAL
                )
            }
        }
    }

    fun resetTaskSortMode(listMode: String) {
        viewModelScope.launch {
            repository.updateState { state ->
                KudoReducer.setTaskSortMode(
                    state = state,
                    listMode = listMode,
                    sortMode = KudoState.TASK_SORT_AUTO_DUE
                )
            }
        }
    }

    fun reorderHabits(orderedIds: List<Long>) {
        viewModelScope.launch {
            repository.updateState { state ->
                KudoReducer.reorderHabits(state, orderedIds)
            }
        }
    }

    fun reorderStore(orderedIds: List<Long>) {
        viewModelScope.launch {
            repository.updateState { state ->
                KudoReducer.reorderStore(state, orderedIds)
            }
        }
    }

    fun deleteTaskItem(id: Long) {
        viewModelScope.launch {
            repository.updateState { state ->
                KudoReducer.deleteTask(state, id)
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            repository.setTheme(theme)
        }
    }

    fun exportJson(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            onComplete(repository.exportJson())
        }
    }

    fun exportToUri(uri: Uri, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            onComplete(repository.exportToUri(uri))
        }
    }

    fun importJson(raw: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            onComplete(repository.importJson(raw))
        }
    }

    fun importFromUri(uri: Uri, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            onComplete(repository.importFromUri(uri))
        }
    }

    private fun parseDashboardValue(
        raw: String,
        view: String
    ): Int {
        if (raw.isBlank()) {
            return when (view) {
                VIEW_STORE -> 0
                else -> 0 // Zero-friction default
            }
        }
        return parseEditValue(raw)
    }

    private fun parseEditValue(raw: String): Int {
        return raw.toIntOrNull()?.let(::abs) ?: 0
    }

    companion object {
        const val VIEW_TASKS = "tasks"
        const val VIEW_STORE = "store"
        const val VIEW_LOG = "log"

        const val KIND_TASK = "task"
        const val KIND_STORE = "store"
    }
}

@Immutable
data class KudoUiState(
    val data: KudoState = KudoState(),
    val theme: String = KudoStateRepository.THEME_SYSTEM,
    val currentView: String = KudoViewModel.VIEW_TASKS,
    val taskCreationTarget: TaskCreationTarget = TaskCreationTarget.INBOX,
    val storeMode: Int = KudoState.STORE_ONCE,
    val habitsCollapsed: Boolean = false,
    val isHabitJiggleMode: Boolean = false,
    val isSettingsVisible: Boolean = false,
    val isHelpVisible: Boolean = false,
    val editingTarget: EditingTarget? = null,
    val pendingMoveTaskId: Long? = null,
    val recentTaskInsertId: Long? = null,
    val recentStoreInsertId: Long? = null
)

@Immutable
data class EditingTarget(
    val kind: String,
    val id: Long
)

enum class TaskCreationTarget {
    INBOX,
    FOCUS,
    HABIT
}
