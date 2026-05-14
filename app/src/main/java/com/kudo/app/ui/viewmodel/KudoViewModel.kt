package com.kudo.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kudo.app.KudoApplication
import com.kudo.app.core.model.KudoIds
import com.kudo.app.core.model.KudoReducer
import com.kudo.app.core.model.KudoState
import com.kudo.app.core.model.KudoStoreKind
import com.kudo.app.core.model.KudoSubtaskDraft
import com.kudo.app.core.model.KudoTaskKind
import com.kudo.app.core.model.KudoTaskSortMode
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

    private val viewState = MutableStateFlow(KudoViewState())

    val uiState: StateFlow<KudoUiState> = combine(
        repository.state,
        repository.theme,
        viewState
    ) { state, theme, view ->
        KudoUiState(data = state, theme = theme, view = view)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KudoUiState()
    )

    fun switchView(view: KudoView) {
        viewState.update {
            it.copy(
                currentView = view,
                habitJiggleMode = if (view != KudoView.Tasks) false else it.habitJiggleMode
            )
        }
    }

    fun cycleTaskCreationTarget() {
        viewState.update {
            it.copy(
                taskCreationTarget = when (it.taskCreationTarget) {
                    KudoTaskKind.Task -> KudoTaskKind.Habit
                    KudoTaskKind.Habit -> KudoTaskKind.Task
                }
            )
        }
    }

    fun toggleStoreMode() {
        viewState.update {
            it.copy(
                storeMode = when (it.storeMode) {
                    KudoStoreKind.Once -> KudoStoreKind.Repeatable
                    KudoStoreKind.Repeatable -> KudoStoreKind.Once
                }
            )
        }
    }

    fun addDashboardItem(title: String, valueInput: String): Boolean {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) {
            return false
        }

        val view = viewState.value
        val parsedValue = parseDashboardValue(valueInput)
        val createdId = KudoIds.next()
        val isStoreInsert = view.currentView == KudoView.Store
        val isTaskInsertAnimation = !isStoreInsert && view.taskCreationTarget == KudoTaskKind.Task

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
                        kind = view.storeMode,
                        now = createdId
                    )
                } else {
                    KudoReducer.addTask(
                        state = state,
                        title = trimmedTitle,
                        coins = parsedValue,
                        kind = view.taskCreationTarget,
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

    fun completeTask(id: Long) {
        launchStateUpdate { state -> KudoReducer.completeTask(state, id) }
        triggerUndoBanner()
    }

    fun completeHabit(id: Long) {
        launchStateUpdate { state -> KudoReducer.completeHabit(state, id) }
        triggerUndoBanner()
    }

    fun completeSubtask(taskId: Long, subtaskId: Long) {
        launchStateUpdate { state -> KudoReducer.completeSubtask(state, taskId, subtaskId) }
    }

    fun purchaseItemFromGesture(id: Long): Boolean {
        val current = uiState.value.data
        val item = current.store.firstOrNull { it.id == id } ?: return false
        if (current.coins < item.cost) {
            return false
        }

        launchStateUpdate { state -> KudoReducer.buyItem(state, id) }
        triggerUndoBanner()
        return true
    }

    fun undoLog(index: Int) {
        dismissUndoBanner()
        launchStateUpdate { state -> KudoReducer.undoLog(state, index) }
    }

    fun openEditTask(id: Long) {
        viewState.update { it.copy(editingTarget = EditingTarget(kind = KudoEditKind.Task, id = id)) }
    }

    fun openEditStore(id: Long) {
        viewState.update { it.copy(editingTarget = EditingTarget(kind = KudoEditKind.Store, id = id)) }
    }

    fun closeEdit() {
        viewState.update { it.copy(editingTarget = null) }
    }

    fun saveEditing(
        title: String,
        valueInput: String,
        dueAtEpochMillis: Long?,
        subtaskDrafts: List<KudoSubtaskDraft>?
    ) {
        val target = viewState.value.editingTarget ?: return
        val sanitizedTitle = title.trim()
        val parsedValue = parseEditValue(valueInput)
        val sanitizedSubtasks = subtaskDrafts?.mapNotNull { draft ->
            draft.title.trim().ifBlank { null }?.let { KudoSubtaskDraft(title = it) }
        }
        val editTimestamp = KudoIds.next()

        launchAsync {
            repository.updateState { state ->
                when (target.kind) {
                    KudoEditKind.Task -> KudoReducer.updateTask(
                        state = state,
                        id = target.id,
                        title = sanitizedTitle,
                        coins = parsedValue,
                        dueAtEpochMillis = dueAtEpochMillis,
                        subtaskDrafts = sanitizedSubtasks,
                        now = editTimestamp
                    )

                    KudoEditKind.Store -> KudoReducer.updateStoreItem(
                        state = state,
                        id = target.id,
                        title = sanitizedTitle,
                        cost = parsedValue
                    )
                }
            }
            closeEdit()
        }
    }

    fun deleteEditing() {
        val target = viewState.value.editingTarget ?: return
        launchAsync {
            repository.updateState { state ->
                when (target.kind) {
                    KudoEditKind.Task -> KudoReducer.deleteTask(state, target.id)
                    KudoEditKind.Store -> KudoReducer.deleteStoreItem(state, target.id)
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
        launchStateUpdate { state ->
            KudoReducer.setTaskSortMode(
                state = KudoReducer.reorderTasks(state, orderedIds),
                sortMode = KudoTaskSortMode.Manual
            )
        }
    }

    fun resetTaskSortMode() {
        launchStateUpdate { state -> KudoReducer.resetTaskOrder(state) }
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

    fun importTasksFromText(raw: String, onComplete: (Int) -> Unit = {}) {
        val drafts = KudoTaskTextImport.parse(raw)
        if (drafts.isEmpty()) {
            onComplete(0)
            return
        }

        val createdAt = KudoIds.next()
        val lastCreatedId = createdAt + drafts.lastIndex
        viewState.update {
            it.copy(
                currentView = KudoView.Tasks,
                taskCreationTarget = KudoTaskKind.Task,
                recentTaskInsertId = lastCreatedId
            )
        }
        launchAsync {
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
            return 0
        }
        return parseEditValue(raw)
    }

    fun openNotebook() {
        val notes = uiState.value.data.notes
        if (notes.isEmpty()) {
            val id = KudoIds.next()
            viewState.update { it.copy(isNotebookVisible = true, selectedNotebookNoteId = id) }
            launchStateUpdate { state -> KudoReducer.addNote(state, now = id).first }
            return
        }

        val currentSelected = viewState.value.selectedNotebookNoteId
        val selectedId = notes.firstOrNull { it.id == currentSelected }?.id ?: notes.first().id
        viewState.update { it.copy(isNotebookVisible = true, selectedNotebookNoteId = selectedId) }
    }

    fun closeNotebook() {
        viewState.update { it.copy(isNotebookVisible = false) }
    }

    fun addNotebookNote() {
        val id = KudoIds.next()
        viewState.update { it.copy(selectedNotebookNoteId = id, isNotebookVisible = true) }
        launchStateUpdate { state -> KudoReducer.addNote(state, now = id).first }
    }

    fun selectNotebookNote(id: Long) {
        viewState.update { it.copy(selectedNotebookNoteId = id) }
    }

    fun updateNotebookNoteTitle(id: Long, title: String) {
        launchStateUpdate { state -> KudoReducer.updateNoteTitle(state, id, title) }
    }

    fun updateNotebookNoteContent(id: Long, content: String) {
        launchStateUpdate { state -> KudoReducer.updateNoteContent(state, id, content) }
    }

    fun deleteNotebookNote(id: Long) {
        val current = uiState.value
        val remaining = current.data.notes.filterNot { it.id == id }
        viewState.update {
            it.copy(
                selectedNotebookNoteId = if (it.selectedNotebookNoteId == id) {
                    remaining.firstOrNull()?.id
                } else {
                    it.selectedNotebookNoteId
                }
            )
        }
        launchStateUpdate { state -> KudoReducer.deleteNote(state, id) }
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
}

enum class KudoView { Tasks, Store, Log }

enum class KudoEditKind { Task, Store }

@Immutable
data class KudoViewState(
    val currentView: KudoView = KudoView.Tasks,
    val taskCreationTarget: KudoTaskKind = KudoTaskKind.Task,
    val storeMode: KudoStoreKind = KudoStoreKind.Once,
    val habitsCollapsed: Boolean = false,
    val habitJiggleMode: Boolean = false,
    val settingsVisible: Boolean = false,
    val helpVisible: Boolean = false,
    val editingTarget: EditingTarget? = null,
    val recentTaskInsertId: Long? = null,
    val recentStoreInsertId: Long? = null,
    val showUndoBanner: Boolean = false,
    val isNotebookVisible: Boolean = false,
    val selectedNotebookNoteId: Long? = null
)

@Immutable
data class KudoUiState(
    val data: KudoState = KudoState(),
    val theme: String = KudoStateRepository.THEME_SYSTEM,
    val view: KudoViewState = KudoViewState()
)

@Immutable
data class EditingTarget(
    val kind: KudoEditKind,
    val id: Long
)
