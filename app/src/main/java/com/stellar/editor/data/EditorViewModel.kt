package com.stellar.editor.data

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class EditorState(
    val project: Project = Project(),
    val currentPageIndex: Int = 0,
    val selectedItemId: String? = null,
    val activeInspectorTab: Int = 0
) {
    val currentPage: Page
        get() = project.pages.getOrElse(currentPageIndex) { project.pages.first() }

    val selectedItem: TextItem?
        get() = selectedItemId?.let { id -> currentPage.textItems.find { it.id == id } }
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()
    private val maxUndo = 50

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    fun pushUndo() {
        undoStack.add(_state.value.project)
        if (undoStack.size > maxUndo) undoStack.removeFirst()
        redoStack.clear()
        _canUndo.value = true
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(_state.value.project)
        _state.update { it.copy(project = undoStack.removeLast(), selectedItemId = null) }
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = true
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.add(_state.value.project)
        _state.update { it.copy(project = redoStack.removeLast(), selectedItemId = null) }
        _canUndo.value = true
        _canRedo.value = redoStack.isNotEmpty()
    }

    // ── Helpers ──────────────────────────────────────────────

    private fun updateCurrentPage(transform: (Page) -> Page) {
        _state.update { state ->
            val pages = state.project.pages.toMutableList()
            pages[state.currentPageIndex] = transform(state.currentPage)
            state.copy(project = state.project.copy(pages = pages))
        }
    }

    // ── Selection ────────────────────────────────────────────

    fun selectItem(id: String?) {
        _state.update { it.copy(selectedItemId = id) }
    }

    fun setInspectorTab(tab: Int) {
        _state.update { it.copy(activeInspectorTab = tab) }
    }

    // ── Text Item CRUD ───────────────────────────────────────

    fun addTextItem() {
        pushUndo()
        val item = TextItem(x = 150f, y = 200f)
        updateCurrentPage { page -> page.copy(textItems = page.textItems + item) }
        _state.update { it.copy(selectedItemId = item.id) }
    }

    fun deleteSelectedItem() {
        val id = _state.value.selectedItemId ?: return
        pushUndo()
        updateCurrentPage { page -> page.copy(textItems = page.textItems.filter { it.id != id }) }
        _state.update { it.copy(selectedItemId = null) }
    }

    fun updateItem(itemId: String, transform: (TextItem) -> TextItem) {
        updateCurrentPage { page ->
            page.copy(textItems = page.textItems.map { if (it.id == itemId) transform(it) else it })
        }
    }

    fun updateSelectedItem(transform: (TextItem) -> TextItem) {
        val id = _state.value.selectedItemId ?: return
        updateItem(id, transform)
    }

    fun updateSelectedItemWithUndo(transform: (TextItem) -> TextItem) {
        pushUndo()
        updateSelectedItem(transform)
    }

    fun moveItemByDelta(itemId: String, dx: Float, dy: Float) {
        updateItem(itemId) { it.copy(x = it.x + dx, y = it.y + dy) }
    }

    fun resizeItemByDelta(itemId: String, dw: Float, dh: Float) {
        updateItem(itemId) {
            it.copy(
                width = (it.width + dw).coerceAtLeast(60f),
                height = (it.height + dh).coerceAtLeast(40f)
            )
        }
    }

    // ── Page Management ──────────────────────────────────────

    fun nextPage() {
        _state.update { s ->
            if (s.currentPageIndex < s.project.pages.size - 1)
                s.copy(currentPageIndex = s.currentPageIndex + 1, selectedItemId = null)
            else s
        }
    }

    fun previousPage() {
        _state.update { s ->
            if (s.currentPageIndex > 0)
                s.copy(currentPageIndex = s.currentPageIndex - 1, selectedItemId = null)
            else s
        }
    }

    fun addPage() {
        pushUndo()
        _state.update { state ->
            val newPages = state.project.pages + Page(pageNumber = state.project.pages.size + 1)
            state.copy(
                project = state.project.copy(pages = newPages),
                currentPageIndex = newPages.size - 1,
                selectedItemId = null
            )
        }
    }

    // ── Image ────────────────────────────────────────────────

    fun setPageImage(uri: Uri) {
        pushUndo()
        val context = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            val internalPath = copyImageToInternal(context, uri)
            _state.update { state ->
                val updatedPage = state.currentPage.copy(
                    imageUri = uri.toString(),
                    imagePath = internalPath
                )
                val pages = state.project.pages.toMutableList()
                pages[state.currentPageIndex] = updatedPage
                state.copy(project = state.project.copy(pages = pages))
            }
        }
    }

    private fun copyImageToInternal(context: Context, uri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "images")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "img_${System.currentTimeMillis()}.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    // ── Save / Load ──────────────────────────────────────────

    fun saveProject() {
        val context = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            val json = ProjectManager.projectToJson(_state.value.project)
            File(context.filesDir, "stellar_project.json").writeText(json)
        }
    }

    fun loadProject() {
        val context = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(context.filesDir, "stellar_project.json")
            if (file.exists()) {
                val project = ProjectManager.projectFromJson(file.readText())
                if (project != null) {
                    _state.update { EditorState(project = project) }
                    undoStack.clear()
                    redoStack.clear()
                    _canUndo.value = false
                    _canRedo.value = false
                }
            }
        }
    }
}
