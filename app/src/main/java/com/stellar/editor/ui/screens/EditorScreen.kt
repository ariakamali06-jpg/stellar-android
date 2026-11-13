package com.stellar.editor.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stellar.editor.data.*
import com.stellar.editor.ui.components.MangaCanvas
import com.stellar.editor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    // Auto load on start
    LaunchedEffect(Unit) {
        viewModel.loadProject()
    }

    // Auto save on project changes
    LaunchedEffect(state.project) {
        viewModel.saveProject()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setPageImage(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(IndigoAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✦", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Stellar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "صفحه ${state.currentPageIndex + 1}/${state.project.pages.size}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) TextPrimary else TextMuted
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) TextPrimary else TextMuted
                        )
                    }

                    // Add Text
                    IconButton(onClick = { viewModel.addTextItem() }) {
                        Icon(Icons.Default.AddComment, contentDescription = "کادر جدید", tint = IndigoLight)
                    }

                    // Pick Image
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = "انتخاب تصویر", tint = TextPrimary)
                    }

                    // Export PNG
                    Button(
                        onClick = {
                            val savedFiles = ProjectManager.exportAllPagesToFiles(context, state.project)
                            val path = savedFiles.firstOrNull()?.parentFile?.absolutePath ?: ""
                            Toast.makeText(
                                context,
                                "خروجی ${savedFiles.size} صفحه با نام‌های 01.png و... ذخیره شد در:\n$path",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = "Export", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 11.sp)
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                val selectedItem = state.selectedItem
                if (selectedItem != null) {
                    // Inspector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TabRow(
                            selectedTabIndex = state.activeInspectorTab,
                            containerColor = Color.Transparent,
                            contentColor = IndigoLight,
                            divider = {},
                            modifier = Modifier.weight(1f)
                        ) {
                            Tab(
                                selected = state.activeInspectorTab == 0,
                                onClick = { viewModel.setInspectorTab(0) }
                            ) {
                                Text("✏️ متن", modifier = Modifier.padding(vertical = 6.dp), fontSize = 12.sp)
                            }
                            Tab(
                                selected = state.activeInspectorTab == 1,
                                onClick = { viewModel.setInspectorTab(1) }
                            ) {
                                Text("💬 حباب", modifier = Modifier.padding(vertical = 6.dp), fontSize = 12.sp)
                            }
                            Tab(
                                selected = state.activeInspectorTab == 2,
                                onClick = { viewModel.setInspectorTab(2) }
                            ) {
                                Text("✨ افکت و زاویه", modifier = Modifier.padding(vertical = 6.dp), fontSize = 12.sp)
                            }
                        }

                        IconButton(
                            onClick = { viewModel.selectItem(null) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                        }
                    }

                    when (state.activeInspectorTab) {
                        0 -> {
                            // TAB 1: TEXT
                            Column {
                                OutlinedTextField(
                                    value = selectedItem.text,
                                    onValueChange = { newText ->
                                        viewModel.updateSelectedItem { it.copy(text = newText) }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 85.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = IndigoAccent,
                                        unfocusedBorderColor = BorderDark,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    placeholder = { Text("متن کادر...", color = TextMuted) }
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("اندازه: ${selectedItem.fontSize.toInt()}", color = TextSecondary, fontSize = 11.sp)
                                    Slider(
                                        value = selectedItem.fontSize,
                                        onValueChange = {
                                            viewModel.updateSelectedItem { item -> item.copy(fontSize = it) }
                                        },
                                        valueRange = 12f..60f,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Bold
                                    IconButton(
                                        onClick = {
                                            viewModel.updateSelectedItemWithUndo { it.copy(isBold = !it.isBold) }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("B", fontWeight = FontWeight.Bold, color = if (selectedItem.isBold) IndigoAccent else TextMuted)
                                    }

                                    // Italic
                                    IconButton(
                                        onClick = {
                                            viewModel.updateSelectedItemWithUndo { it.copy(isItalic = !it.isItalic) }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("I", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = if (selectedItem.isItalic) IndigoAccent else TextMuted)
                                    }

                                    // Alignment Left
                                    IconButton(
                                        onClick = {
                                            viewModel.updateSelectedItemWithUndo { it.copy(alignment = AlignmentMode.LEFT) }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.FormatAlignLeft,
                                            contentDescription = "چپ",
                                            tint = if (selectedItem.alignment == AlignmentMode.LEFT) IndigoAccent else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Alignment Center
                                    IconButton(
                                        onClick = {
                                            viewModel.updateSelectedItemWithUndo { it.copy(alignment = AlignmentMode.CENTER) }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.FormatAlignCenter,
                                            contentDescription = "وسط",
                                            tint = if (selectedItem.alignment == AlignmentMode.CENTER) IndigoAccent else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Alignment Right
                                    IconButton(
                                        onClick = {
                                            viewModel.updateSelectedItemWithUndo { it.copy(alignment = AlignmentMode.RIGHT) }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.FormatAlignRight,
                                            contentDescription = "راست",
                                            tint = if (selectedItem.alignment == AlignmentMode.RIGHT) IndigoAccent else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text("رنگ متن:", color = TextSecondary, fontSize = 11.sp)
                                    val textColors = listOf(
                                        0xFF000000 to Color.Black,
                                        0xFFFFFFFF to Color.White,
                                        0xFFEF4444 to DangerRed,
                                        0xFF6366F1 to IndigoAccent
                                    )
                                    textColors.forEach { (colorLong, c) ->
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .border(
                                                    width = if (selectedItem.fontColor == colorLong) 2.dp else 1.dp,
                                                    color = if (selectedItem.fontColor == colorLong) IndigoLight else BorderDark,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    viewModel.updateSelectedItemWithUndo { it.copy(fontColor = colorLong) }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            // TAB 2: BUBBLE
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BubbleShape.entries.forEach { shape ->
                                        FilterChip(
                                            selected = selectedItem.bubbleShape == shape,
                                            onClick = {
                                                viewModel.updateSelectedItemWithUndo { it.copy(bubbleShape = shape) }
                                            },
                                            label = { Text(shape.displayName, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = IndigoAccent,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("رنگ زمینه:", color = TextSecondary, fontSize = 11.sp)

                                    // White preset
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, BorderDark, CircleShape)
                                            .clickable {
                                                viewModel.updateSelectedItemWithUndo {
                                                    it.copy(bubbleColor = 0xFFFFFFFF, fontColor = 0xFF000000, hasBubble = true)
                                                }
                                            }
                                    )

                                    // Black preset
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                            .border(1.dp, BorderDark, CircleShape)
                                            .clickable {
                                                viewModel.updateSelectedItemWithUndo {
                                                    it.copy(bubbleColor = 0xFF000000, fontColor = 0xFFFFFFFF, hasBubble = true)
                                                }
                                            }
                                    )

                                    // Transparent
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BorderDark)
                                            .clickable {
                                                viewModel.updateSelectedItemWithUndo {
                                                    it.copy(bubbleColor = 0x00FFFFFF, strokeWidth = 0f, hasBubble = false)
                                                }
                                            }
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("بدون کادر", fontSize = 10.sp, color = TextPrimary)
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ضخامت خط: ${selectedItem.strokeWidth.toInt()}", color = TextSecondary, fontSize = 11.sp)
                                    Slider(
                                        value = selectedItem.strokeWidth,
                                        onValueChange = {
                                            viewModel.updateSelectedItem { item -> item.copy(strokeWidth = it) }
                                        },
                                        valueRange = 0f..10f,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        2 -> {
                            // TAB 3: EFFECTS & ROTATION
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedItem.hasShadow,
                                            onCheckedChange = {
                                                viewModel.updateSelectedItemWithUndo { item -> item.copy(hasShadow = it) }
                                            }
                                        )
                                        Text("سایه حباب", color = TextPrimary, fontSize = 11.sp)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedItem.hasExtrusion,
                                            onCheckedChange = {
                                                viewModel.updateSelectedItemWithUndo { item -> item.copy(hasExtrusion = it) }
                                            }
                                        )
                                        Text("افکت 3D", color = TextPrimary, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.deleteSelectedItem() },
                                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("حذف", fontSize = 11.sp)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("چرخش: ${selectedItem.rotation.toInt()}°", color = TextSecondary, fontSize = 11.sp)
                                    Slider(
                                        value = selectedItem.rotation,
                                        onValueChange = {
                                            viewModel.updateSelectedItem { item -> item.copy(rotation = it) }
                                        },
                                        valueRange = -180f..180f,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            viewModel.updateSelectedItemWithUndo { it.copy(rotation = 0f) }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "ریست زاویه", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Page Navigation Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { viewModel.previousPage() },
                                enabled = state.currentPageIndex > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("◀ قبلی")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                "صفحه ${state.currentPageIndex + 1} از ${state.project.pages.size}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { viewModel.nextPage() },
                                enabled = state.currentPageIndex < state.project.pages.size - 1,
                                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("بعدی ▶")
                            }
                        }

                        Button(
                            onClick = { viewModel.addPage() },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "صفحه جدید", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ صفحه جدید", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MangaCanvas(
                page = state.currentPage,
                selectedItem = state.selectedItem,
                onSelectItem = { itemId -> viewModel.selectItem(itemId) },
                onMoveItem = { id, dx, dy -> viewModel.moveItemByDelta(id, dx, dy) },
                onResizeItem = { id, dw, dh -> viewModel.resizeItemByDelta(id, dw, dh) },
                onDragStart = { viewModel.pushUndo() }
            )
        }
    }
}
