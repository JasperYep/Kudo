package com.kudo.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kudo.app.KudoApplication
import com.kudo.app.core.model.KudoReducer
import com.kudo.app.core.model.KudoState
import com.kudo.app.core.model.KudoSubtaskDraft
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
        repository.isSubtaskModeEnabled,
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
            isSubtaskModeEnabled = values[2] as Boolean,
            currentView = values[3] as String,
            taskCreationTarget = values[4] as TaskCreationTarget,
            storeMode = values[5] as Int,
            habitsCollapsed = values[6] as Boolean,
            isHabitJiggleMode = values[7] as Boolean,
            isSettingsVisible = values[8] as Boolean,
            isHelpVisible = values[9] as Boolean,
            editingTarget = values[10] as EditingTarget?,
            pendingMoveTaskId = values[11] as Long?,
            recentTaskInsertId = values[12] as Long?,
            recentStoreInsertId = values[13] as Long?
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

    fun setListMode(mode: String) {
        launchStateUpdate { it.copy(listMode = mode) }
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
        val parsedValue = parseDashboardValue(valueInput)
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
            clearRecentInsert(
                target = recentStoreInsertId,
                id = createdId
            )
        } else if (isTaskInsertAnimation) {
            clearRecentInsert(
                target = recentTaskInsertId,
                id = createdId
            )
        }
        return true
    }

    private fun clearRecentInsert(
        target: MutableStateFlow<Long?>,
        id: Long
    ) {
        viewModelScope.launch {
            delay(380)
            if (target.value == id) {
                target.value = null
            }
        }
    }

    fun completeTask(id: Long) {
        launchStateUpdate { state -> KudoReducer.completeTask(state, id) }
    }

    fun completeHabit(id: Long) {
        launchStateUpdate { state -> KudoReducer.completeHabit(state, id) }
    }

    fun completeSubtask(taskId: Long, subtaskId: Long) {
        launchStateUpdate { state -> KudoReducer.completeSubtask(state, taskId, subtaskId) }
    }

    fun moveTaskFromGesture(id: Long): Boolean {
        val current = uiState.value.data
        val result = KudoReducer.moveTask(current, id)
        if (result.requiresValue) {
            pendingMoveTaskId.value = id
            editingTarget.value = EditingTarget(kind = KIND_TASK, id = id)
            return false
        }

        launchSaveState(result.state)
        return true
    }

    fun purchaseItemFromGesture(id: Long): Boolean {
        val current = uiState.value.data
        val item = current.store.firstOrNull { it.id == id } ?: return false
        if (current.coins < item.cost) {
            return false
        }

        launchStateUpdate { state -> KudoReducer.buyItem(state, id) }
        return true
    }

    fun undoLog(index: Int) {
        launchStateUpdate { state -> KudoReducer.undoLog(state, index) }
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

    fun saveEditing(
        title: String,
        valueInput: String,
        dueEpochDay: Long?,
        subtaskDrafts: List<KudoSubtaskDraft>?
    ) {
        val target = editingTarget.value ?: return
        val pendingTaskId = pendingMoveTaskId.value
        val sanitizedTitle = title.trim()
        val parsedValue = parseEditValue(valueInput)
        val sanitizedSubtasks = subtaskDrafts?.mapNotNull { draft ->
            val trimmedTitle = draft.title.trim()
            if (trimmedTitle.isBlank()) {
                null
            } else {
                KudoSubtaskDraft(
                    title = trimmedTitle,
                    difficulty = draft.difficulty
                )
            }
        }
        val editTimestamp = System.currentTimeMillis()

        launchAsync {
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
                            dueEpochDay = dueEpochDay,
                            subtaskDrafts = sanitizedSubtasks,
                            now = editTimestamp
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
                            dueEpochDay = dueEpochDay,
                            subtaskDrafts = sanitizedSubtasks,
                            now = editTimestamp
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
        launchAsync {
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
        val listMode = uiState.value.data.listMode
        launchStateUpdate { state ->
            KudoReducer.setTaskSortMode(
                state = KudoReducer.reorderTasks(state, listMode, orderedIds),
                listMode = listMode,
                sortMode = KudoState.TASK_SORT_MANUAL
            )
        }
    }

    fun resetTaskSortMode(listMode: String) {
        launchStateUpdate { state ->
            KudoReducer.setTaskSortMode(
                state = state,
                listMode = listMode,
                sortMode = KudoState.TASK_SORT_AUTO_DUE
            )
        }
    }

    fun reorderHabits(orderedIds: List<Long>) {
        launchStateUpdate { state -> KudoReducer.reorderHabits(state, orderedIds) }
    }

    fun reorderStore(orderedIds: List<Long>) {
        launchStateUpdate { state -> KudoReducer.reorderStore(state, orderedIds) }
    }

    fun deleteTaskItem(id: Long) {
        launchStateUpdate { state -> KudoReducer.deleteTask(state, id) }
    }

    fun setTheme(theme: String) {
        launchAsync { repository.setTheme(theme) }
    }

    fun setSubtaskModeEnabled(enabled: Boolean) {
        launchAsync { repository.setSubtaskModeEnabled(enabled) }
    }

    fun exportToUri(uri: Uri, onComplete: (Boolean) -> Unit = {}) {
        launchAsync {
            onComplete(repository.exportToUri(uri))
        }
    }

    fun importFromUri(uri: Uri, onComplete: (Boolean) -> Unit = {}) {
        launchAsync {
            onComplete(repository.importFromUri(uri))
        }
    }

    private fun parseDashboardValue(raw: String): Int {
        if (raw.isBlank()) {
            return 0
        }
        return parseEditValue(raw)
    }

    private fun parseEditValue(raw: String): Int {
        return raw.toIntOrNull()?.let(::abs) ?: 0
    }

    private fun launchAsync(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private fun launchStateUpdate(transform: (KudoState) -> KudoState) {
        launchAsync {
            repository.updateState(transform)
        }
    }

    private fun launchSaveState(state: KudoState) {
        launchAsync {
            repository.saveState(state)
        }
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
    val isSubtaskModeEnabled: Boolean = false,
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
