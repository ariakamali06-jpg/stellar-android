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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.stellar.editor.data.*
import com.stellar.editor.ui.components.MangaCanvas
import com.stellar.editor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen() {
    val context = LocalContext.current
    var project by remember { mutableStateOf(Project()) }
    var currentPageIndex by remember { mutableStateOf(0) }
    var selectedItem by remember { mutableStateOf<TextItem?>(null) }
    var activeInspectorTab by remember { mutableStateOf(0) } // 0: متن, 1: حباب, 2: افکت

    val currentPage = project.pages.getOrElse(currentPageIndex) {
        if (project.pages.isEmpty()) {
            val p = Page(pageNumber = 1)
            project.pages.add(p)
            p
        } else {
            project.pages[0]
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            currentPage.imageUri = uri.toString()
            project = project.copy(pages = ArrayList(project.pages))
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
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "صفحه ${currentPageIndex + 1}/${project.pages.size}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val newItem = TextItem(
                            text = "متن جدید",
                            x = 150f,
                            y = 200f
                        )
                        currentPage.textItems.add(newItem)
                        selectedItem = newItem
                        project = project.copy(pages = ArrayList(project.pages))
                    }) {
                        Icon(Icons.Default.AddComment, contentDescription = "کادر جدید", tint = IndigoLight)
                    }

                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = "انتخاب تصویر", tint = TextPrimary)
                    }

                    Button(
                        onClick = {
                            val savedFiles = ProjectManager.exportAllPagesToFiles(context, project)
                            val path = savedFiles.firstOrNull()?.parentFile?.absolutePath ?: ""
                            Toast.makeText(
                                context,
                                "خروجی ${savedFiles.size} صفحه با نام‌های 01.png و... در:\n$path",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = "Export", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PNG", fontSize = 12.sp)
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
                    .padding(12.dp)
            ) {
                if (selectedItem != null) {
                    val item = selectedItem!!

                    // Inspector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TabRow(
                            selectedTabIndex = activeInspectorTab,
                            containerColor = Color.Transparent,
                            contentColor = IndigoLight,
                            divider = {},
                            modifier = Modifier.weight(1f)
                        ) {
                            Tab(selected = activeInspectorTab == 0, onClick = { activeInspectorTab = 0 }) {
                                Text("✏️ متن", modifier = Modifier.padding(vertical = 8.dp), fontSize = 13.sp)
                            }
                            Tab(selected = activeInspectorTab == 1, onClick = { activeInspectorTab = 1 }) {
                                Text("💬 حباب", modifier = Modifier.padding(vertical = 8.dp), fontSize = 13.sp)
                            }
                            Tab(selected = activeInspectorTab == 2, onClick = { activeInspectorTab = 2 }) {
                                Text("✨ افکت", modifier = Modifier.padding(vertical = 8.dp), fontSize = 13.sp)
                            }
                        }

                        IconButton(onClick = { selectedItem = null }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                        }
                    }

                    when (activeInspectorTab) {
                        0 -> {
                            // TAB 1: TEXT
                            Column {
                                OutlinedTextField(
                                    value = item.text,
                                    onValueChange = {
                                        item.text = it
                                        project = project.copy(pages = ArrayList(project.pages))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 100.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = IndigoAccent,
                                        unfocusedBorderColor = BorderDark,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    placeholder = { Text("متن کادر...", color = TextMuted) }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("اندازه: ${item.fontSize.toInt()}", color = TextSecondary, fontSize = 12.sp)
                                    Slider(
                                        value = item.fontSize,
                                        onValueChange = {
                                            item.fontSize = it
                                            project = project.copy(pages = ArrayList(project.pages))
                                        },
                                        valueRange = 12f..60f,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(onClick = {
                                        item.isBold = !item.isBold
                                        project = project.copy(pages = ArrayList(project.pages))
                                    }) {
                                        Text("B", fontWeight = FontWeight.Bold, color = if (item.isBold) IndigoAccent else TextMuted)
                                    }

                                    IconButton(onClick = {
                                        item.isItalic = !item.isItalic
                                        project = project.copy(pages = ArrayList(project.pages))
                                    }) {
                                        Text("I", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = if (item.isItalic) IndigoAccent else TextMuted)
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BubbleShape.values().forEach { shape ->
                                        FilterChip(
                                            selected = item.bubbleShape == shape,
                                            onClick = {
                                                item.bubbleShape = shape
                                                project = project.copy(pages = ArrayList(project.pages))
                                            },
                                            label = { Text(shape.displayName) },
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
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("رنگ حباب:", color = TextSecondary, fontSize = 12.sp)

                                    // White preset
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .clickable {
                                                item.bubbleColor = 0xFFFFFFFF
                                                item.fontColor = 0xFF000000
                                                project = project.copy(pages = ArrayList(project.pages))
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
                                                item.bubbleColor = 0xFF000000
                                                item.fontColor = 0xFFFFFFFF
                                                project = project.copy(pages = ArrayList(project.pages))
                                            }
                                    )

                                    // Transparent
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BorderDark)
                                            .clickable {
                                                item.bubbleColor = 0x00FFFFFF
                                                item.strokeWidth = 0f
                                                project = project.copy(pages = ArrayList(project.pages))
                                            }
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("شفاف", fontSize = 10.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }
                        2 -> {
                            // TAB 3: EFFECTS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = item.hasShadow,
                                        onCheckedChange = {
                                            item.hasShadow = it
                                            project = project.copy(pages = ArrayList(project.pages))
                                        }
                                    )
                                    Text("سایه حباب", color = TextPrimary, fontSize = 12.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = item.hasExtrusion,
                                        onCheckedChange = {
                                            item.hasExtrusion = it
                                            project = project.copy(pages = ArrayList(project.pages))
                                        }
                                    )
                                    Text("افکت 3D", color = TextPrimary, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        currentPage.textItems.remove(item)
                                        selectedItem = null
                                        project = project.copy(pages = ArrayList(project.pages))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("حذف", fontSize = 11.sp)
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
                                onClick = {
                                    if (currentPageIndex > 0) currentPageIndex--
                                },
                                enabled = currentPageIndex > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("◀ قبلی")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                "صفحه ${currentPageIndex + 1} از ${project.pages.size}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (currentPageIndex < project.pages.size - 1) currentPageIndex++
                                },
                                enabled = currentPageIndex < project.pages.size - 1,
                                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("بعدی ▶")
                            }
                        }

                        Button(
                            onClick = {
                                val newPage = Page(pageNumber = project.pages.size + 1)
                                project.pages.add(newPage)
                                currentPageIndex = project.pages.size - 1
                                project = project.copy(pages = ArrayList(project.pages))
                            },
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
                page = currentPage,
                selectedItem = selectedItem,
                onSelectItem = { item -> selectedItem = item },
                onItemMoved = { item, newX, newY ->
                    item.x = newX
                    item.y = newY
                    project = project.copy(pages = ArrayList(project.pages))
                }
            )
        }
    }
}
