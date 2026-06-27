package com.kudo.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kudo.app.KudoApplication
import com.kudo.app.core.model.KudoReducer
import com.kudo.app.core.model.KudoState
import com.kudo.app.core.model.KudoTaskImportDraft
import com.kudo.app.core.model.KudoTaskTextImport
import com.kudo.app.core.repository.KudoStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
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

    private data class KudoViewState(
        val currentView: String = VIEW_TASKS,
        val taskCreationTarget: TaskCreationTarget = TaskCreationTarget.TASK,
        val storeMode: Int = KudoState.STORE_ONCE,
    val taskMultiplierMode: Int = 1,
        val taskMultiplierMode: Int = 1,
        val habitsCollapsed: Boolean = false,
        val habitJiggleMode: Boolean = false,
        val settingsVisible: Boolean = false,
        val helpVisible: Boolean = false,
        val editingTarget: EditingTarget? = null,
        val recentTaskInsertId: Long? = null,
        val recentStoreInsertId: Long? = null,
        val showUndoBanner: Boolean = false,
        val isImportPreviewVisible: Boolean = false,
        val importPreviewDrafts: List<KudoTaskImportDraft> = emptyList()
    )
    private val viewState = MutableStateFlow(KudoViewState())

    val uiState: StateFlow<KudoUiState> = combine(
        repository.state,
        repository.theme,
        viewState
    ) { state, theme, view ->
        KudoUiState(
            data = state,
            theme = theme,
            currentView = view.currentView,
            taskCreationTarget = view.taskCreationTarget,
            storeMode = view.storeMode,
            taskMultiplierMode = view.taskMultiplierMode,
            habitsCollapsed = view.habitsCollapsed,
            isHabitJiggleMode = view.habitJiggleMode,
            isSettingsVisible = view.settingsVisible,
            isHelpVisible = view.helpVisible,
            editingTarget = view.editingTarget,
            recentTaskInsertId = view.recentTaskInsertId,
            recentStoreInsertId = view.recentStoreInsertId,
            showUndoBanner = view.showUndoBanner,
            isImportPreviewVisible = view.isImportPreviewVisible,
            importPreviewDrafts = view.importPreviewDrafts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KudoUiState()
    )

    fun switchView(view: String) {
        viewState.update {
            it.copy(
                currentView = view,
                habitJiggleMode = if (view != VIEW_TASKS) false else it.habitJiggleMode
            )
        }
    }

    fun cycleTaskCreationTarget() {
        viewState.update {
            it.copy(
                taskCreationTarget = when (it.taskCreationTarget) {
                    TaskCreationTarget.TASK -> TaskCreationTarget.HABIT
                    TaskCreationTarget.HABIT -> TaskCreationTarget.TASK
                }
            )
        }
    }

    fun toggleTaskMultiplier() {
        viewState.update { 
            val next = when (it.taskMultiplierMode) {
                1 -> 2
                2 -> 3
                else -> 1
            }
            it.copy(taskMultiplierMode = next)
        }
    }

    fun toggleStoreMode() {
        viewState.update {
            it.copy(storeMode = if (it.storeMode == KudoState.STORE_ONCE) {
                KudoState.STORE_INFINITE
            } else {
                KudoState.STORE_ONCE
            })
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
            viewState.update { it.copy(recentStoreInsertId = createdId) }
        } else if (isTaskInsertAnimation) {
            viewState.update { it.copy(recentTaskInsertId = createdId) }
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
                        taskMultiplier = if (current.taskCreationTarget == TaskCreationTarget.TASK) current.taskMultiplierMode else 1,
                        state = state,
                        title = trimmedTitle,
                        value = parsedValue,
                        type = when (current.taskCreationTarget) {
                            TaskCreationTarget.HABIT -> KudoState.TYPE_HABIT
                            TaskCreationTarget.TASK -> KudoState.TYPE_TASK
                        },
                        now = createdId
                    )
                }
            }
        }
        if (isStoreInsert) {
            clearRecentInsert(isStore = true, id = createdId)
        } else if (isTaskInsertAnimation) {
            clearRecentInsert(isStore = false, id = createdId)
        }
        return true
    }

    private fun clearRecentInsert(isStore: Boolean, id: Long) {
        viewModelScope.launch {
            delay(380)
            viewState.update { current ->
                if (isStore) {
                    if (current.recentStoreInsertId == id) current.copy(recentStoreInsertId = null) else current
                } else {
                    if (current.recentTaskInsertId == id) current.copy(recentTaskInsertId = null) else current
                }
            }
        }
    }

    private var undoBannerJob: Job? = null

    private fun triggerUndoBanner() {
        undoBannerJob?.cancel()
        viewState.update { it.copy(showUndoBanner = true) }
        undoBannerJob = viewModelScope.launch {
            delay(3000)
            viewState.update { it.copy(showUndoBanner = false) }
        }
    }

    fun dismissUndoBanner() {
        undoBannerJob?.cancel()
        viewState.update { it.copy(showUndoBanner = false) }
    }

    fun toggleTimer(id: Long) {
        viewModelScope.launch { repository.updateState { state -> KudoReducer.toggleTimer(state, id) } }
    }

    fun completeTask(id: Long) {
        viewModelScope.launch { repository.updateState { state -> KudoReducer.completeTask(state, id) } }
        triggerUndoBanner()
    }

    fun completeHabit(id: Long) {
        viewModelScope.launch { repository.updateState { state -> KudoReducer.completeHabit(state, id) } }
        triggerUndoBanner()
    }

    fun purchaseItemFromGesture(id: Long): Boolean {
        val current = uiState.value.data
        val item = current.store.firstOrNull { it.id == id } ?: return false
        if (current.coins < item.cost) {
            return false
        }

        viewModelScope.launch { repository.updateState { state -> KudoReducer.buyItem(state, id) } }
        triggerUndoBanner()
        return true
    }

    fun undoLog(index: Int) {
        dismissUndoBanner()
        viewModelScope.launch { repository.updateState { state -> KudoReducer.undoLog(state, index) } }
    }

    fun openEditTask(id: Long) {
        viewState.update { it.copy(editingTarget = EditingTarget(kind = KIND_TASK, id = id)) }
    }

    fun openEditStore(id: Long) {
        viewState.update { it.copy(editingTarget = EditingTarget(kind = KIND_STORE, id = id)) }
    }

    fun closeEdit() {
        viewState.update { it.copy(editingTarget = null) }
    }

    fun saveEditing(
        title: String,
        valueInput: String,
        dueAtEpochMillis: Long?
    ) {
        val target = viewState.value.editingTarget ?: return
        val sanitizedTitle = title.trim()
        val parsedValue = parseEditValue(valueInput)

        viewModelScope.launch {
            repository.updateState { state ->
                when (target.kind) {
                    KIND_TASK -> KudoReducer.updateTask(
                        state = state,
                        id = target.id,
                        title = sanitizedTitle,
                        value = parsedValue,
                        dueAtEpochMillis = dueAtEpochMillis
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
            closeEdit()
        }
    }

    fun deleteEditing() {
        val target = viewState.value.editingTarget ?: return
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
        viewState.update { it.copy(habitJiggleMode = false, settingsVisible = true, helpVisible = false) }
    }

    fun closeSettings() {
        viewState.update { it.copy(settingsVisible = false, helpVisible = false) }
    }

    fun toggleHelp() {
        viewState.update { it.copy(helpVisible = !it.helpVisible) }
    }

    fun toggleHabitsCollapsed() {
        viewState.update { it.copy(habitsCollapsed = !it.habitsCollapsed) }
    }

    fun enterHabitJiggleMode() {
        viewState.update { it.copy(habitJiggleMode = true) }
    }

    fun exitHabitJiggleMode() {
        viewState.update { it.copy(habitJiggleMode = false) }
    }

    fun reorderTasks(orderedIds: List<Long>) {
        viewModelScope.launch {
            repository.updateState { state ->
                KudoReducer.setTaskSortMode(
                    state = KudoReducer.reorderTasks(state, orderedIds),
                    sortMode = KudoState.TASK_SORT_MANUAL
                )
            }
        }
    }

    fun resetTaskSortMode() {
        viewModelScope.launch { repository.updateState { state -> KudoReducer.resetTaskOrder(state) } }
    }

    fun reorderHabits(orderedIds: List<Long>) {
        viewModelScope.launch { repository.updateState { state -> KudoReducer.reorderHabits(state, orderedIds) } }
    }

    fun reorderStore(orderedIds: List<Long>) {
        viewModelScope.launch { repository.updateState { state -> KudoReducer.reorderStore(state, orderedIds) } }
    }

    fun deleteTaskItem(id: Long) {
        viewModelScope.launch { repository.updateState { state -> KudoReducer.deleteTask(state, id) } }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { repository.setTheme(theme) }
    }

    fun exportToUri(uri: Uri, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            onComplete(repository.exportToUri(uri))
        }
    }

    fun importFromUri(uri: Uri, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            onComplete(repository.importFromUri(uri))
        }
    }

    fun importTasksFromText(raw: String, onComplete: (Int) -> Unit = {}) {
        val drafts = KudoTaskTextImport.parse(raw)
        if (drafts.isEmpty()) {
            onComplete(0)
            return
        }

        val createdAt = System.currentTimeMillis()
        val lastCreatedId = createdAt + drafts.lastIndex
        viewState.update {
            it.copy(
                currentView = VIEW_TASKS,
                taskCreationTarget = TaskCreationTarget.TASK,
                recentTaskInsertId = lastCreatedId
            )
        }
        viewModelScope.launch {
            repository.updateState { state ->
                KudoReducer.addImportedTasks(
                    state = state,
                    drafts = drafts,
                    now = createdAt
                )
            }
            onComplete(drafts.size)
        }
        clearRecentInsert(isStore = false, id = lastCreatedId)
    }

    private fun parseDashboardValue(raw: String): Int {
        if (raw.isBlank()) {
            return 1
        }
        return parseEditValue(raw)
    }








    fun showImportPreview(text: String) {
        val drafts = KudoTaskTextImport.parse(text)
        if (drafts.isNotEmpty()) {
            viewState.update {
                it.copy(
                    isImportPreviewVisible = true,
                    importPreviewDrafts = drafts
                )
            }
        }
    }

    fun updateImportDraft(index: Int, title: String, value: Int) {
        viewState.update { state ->
            val updatedDrafts = state.importPreviewDrafts.toMutableList()
            if (index in updatedDrafts.indices) {
                updatedDrafts[index] = KudoTaskImportDraft(title, value)
            }
            state.copy(importPreviewDrafts = updatedDrafts)
        }
    }

    fun deleteImportDraft(index: Int) {
        viewState.update { state ->
            val updatedDrafts = state.importPreviewDrafts.toMutableList()
            if (index in updatedDrafts.indices) {
                updatedDrafts.removeAt(index)
            }
            state.copy(importPreviewDrafts = updatedDrafts)
        }
    }

    fun confirmImportPreview() {
        val drafts = viewState.value.importPreviewDrafts
        if (drafts.isEmpty()) {
            dismissImportPreview()
            return
        }

        val createdAt = System.currentTimeMillis()
        val lastCreatedId = createdAt + drafts.lastIndex
        viewState.update {
            it.copy(
                currentView = VIEW_TASKS,
                taskCreationTarget = TaskCreationTarget.TASK,
                recentTaskInsertId = lastCreatedId,
                isImportPreviewVisible = false
            )
        }
        viewModelScope.launch {
            repository.updateState { state ->
                KudoReducer.addImportedTasks(state, drafts, now = createdAt)
            }
        }
        viewState.update { it.copy(importPreviewDrafts = emptyList(), recentTaskInsertId = null) }
    }

    fun dismissImportPreview() {
        viewState.update {
            it.copy(
                isImportPreviewVisible = false,
                importPreviewDrafts = emptyList()
            )
        }
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
    val taskCreationTarget: TaskCreationTarget = TaskCreationTarget.TASK,
    val storeMode: Int = KudoState.STORE_ONCE,
    val taskMultiplierMode: Int = 1,
        val taskMultiplierMode: Int = 1,
    val habitsCollapsed: Boolean = false,
    val isHabitJiggleMode: Boolean = false,
    val isSettingsVisible: Boolean = false,
    val isHelpVisible: Boolean = false,
    val editingTarget: EditingTarget? = null,
    val recentTaskInsertId: Long? = null,
    val recentStoreInsertId: Long? = null,
    val showUndoBanner: Boolean = false,
    val isImportPreviewVisible: Boolean = false,
    val importPreviewDrafts: List<KudoTaskImportDraft> = emptyList()
)

@Immutable
data class EditingTarget(
    val kind: String,
    val id: Long
)

enum class TaskCreationTarget {
    TASK,
    HABIT
}
