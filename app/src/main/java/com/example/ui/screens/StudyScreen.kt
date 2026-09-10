package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ErrorLogEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.StudyMaterialEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    studyMaterials: List<StudyMaterialEntity>,
    notes: List<NoteEntity>,
    errorLogs: List<ErrorLogEntity>,
    onAddStudyMaterial: (String, String, String, String, String, String) -> Unit,
    onTogglePinMaterial: (StudyMaterialEntity) -> Unit,
    onDeleteStudyMaterial: (Long) -> Unit,
    onAddNote: (String, String, String, String) -> Unit,
    onToggleFavoriteNote: (NoteEntity) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onAddErrorLog: (String, String, String) -> Unit,
    onToggleResolveErrorLog: (ErrorLogEntity) -> Unit,
    onDeleteErrorLog: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Materials, 1: Notes, 2: Error Log
    var selectedSubject by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddErrorDialog by remember { mutableStateOf(false) }

    val subjects = listOf("ALL", "Physics", "Chemistry", "Biology", "Math", "English", "Bangla", "ICT", "General")

    val filteredMaterials = remember(studyMaterials, selectedSubject, searchQuery) {
        studyMaterials.filter { item ->
            val subjMatch = if (selectedSubject == "ALL") true else item.subject.equals(selectedSubject, ignoreCase = true)
            val searchMatch = searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true)
            subjMatch && searchMatch
        }
    }

    val filteredNotes = remember(notes, selectedSubject, searchQuery) {
        notes.filter { note ->
            val subjMatch = if (selectedSubject == "ALL") true else note.subject.equals(selectedSubject, ignoreCase = true)
            val searchMatch = searchQuery.isBlank() || note.title.contains(searchQuery, ignoreCase = true) || note.content.contains(searchQuery, ignoreCase = true)
            subjMatch && searchMatch
        }
    }

    val filteredErrorLogs = remember(errorLogs, selectedSubject, searchQuery) {
        errorLogs.filter { log ->
            val subjMatch = if (selectedSubject == "ALL") true else log.subject.equals(selectedSubject, ignoreCase = true)
            val searchMatch = searchQuery.isBlank() || log.topic.contains(searchQuery, ignoreCase = true) || log.note.contains(searchQuery, ignoreCase = true)
            subjMatch && searchMatch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> showAddMaterialDialog = true
                        1 -> showAddNoteDialog = true
                        2 -> showAddErrorDialog = true
                    }
                },
                containerColor = AmberGold,
                contentColor = Color(0xFF211823),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("study_hub_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("study_screen")
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AmberGold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Materials (${studyMaterials.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Notes (${notes.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Error Log (${errorLogs.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            // Search Bar & Subject Filter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search materials, formulas, weak topics...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("study_search_bar")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjects) { subj ->
                        val isSelected = selectedSubject == subj
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Tab Content List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (filteredMaterials.isEmpty()) {
                            item {
                                EmptyStudyState("No study materials found. Tap '+' to add chapter notes, formula sheets, or question banks.")
                            }
                        } else {
                            items(filteredMaterials, key = { it.id }) { material ->
                                MaterialCard(
                                    material = material,
                                    onTogglePin = { onTogglePinMaterial(material) },
                                    onDelete = { onDeleteStudyMaterial(material.id) }
                                )
                            }
                        }
                    }
                    1 -> {
                        if (filteredNotes.isEmpty()) {
                            item {
                                EmptyStudyState("No study notes saved yet. Tap '+' to create formula summaries, revision keys, or quick reminders.")
                            }
                        } else {
                            items(filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onToggleFav = { onToggleFavoriteNote(note) },
                                    onDelete = { onDeleteNote(note.id) }
                                )
                            }
                        }
                    }
                    2 -> {
                        if (filteredErrorLogs.isEmpty()) {
                            item {
                                EmptyStudyState("No error logs found. Tap '+' to log questions missed and weak concepts to drill on Fridays.")
                            }
                        } else {
                            items(filteredErrorLogs, key = { it.id }) { log ->
                                ErrorLogDetailCard(
                                    log = log,
                                    onToggleResolve = { onToggleResolveErrorLog(log) },
                                    onDelete = { onDeleteErrorLog(log.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddMaterialDialog) {
        AddMaterialDialog(
            onDismiss = { showAddMaterialDialog = false },
            onConfirm = { title, subj, type, desc, content, tags ->
                onAddStudyMaterial(title, subj, type, desc, content, tags)
                showAddMaterialDialog = false
            }
        )
    }

    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { title, content, subj, category ->
                onAddNote(title, content, subj, category)
                showAddNoteDialog = false
            }
        )
    }

    if (showAddErrorDialog) {
        AddErrorLogDialog(
            onDismiss = { showAddErrorDialog = false },
            onConfirm = { topic, subj, note ->
                onAddErrorLog(topic, subj, note)
                showAddErrorDialog = false
            }
        )
    }
}

@Composable
private fun MaterialCard(
    material: StudyMaterialEntity,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("material_card_${material.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = material.subject,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SteelBlue.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = material.type.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SteelBlue
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (material.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (material.isPinned) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = material.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            if (material.description.isNotBlank()) {
                Text(
                    text = material.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (material.linkOrContent.isNotBlank()) {
                Surface(
                    onClick = { expanded = !expanded },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (expanded) "Hide Content" else "View Content / Formulas",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = material.linkOrContent,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onToggleFav: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = note.subject,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SageGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = note.category.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SageGreen
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onToggleFav, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (note.isFavorite) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ErrorLogDetailCard(
    log: ErrorLogEntity,
    onToggleResolve: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("error_log_detail_${log.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = log.isResolved,
                onCheckedChange = { onToggleResolve() },
                colors = CheckboxDefaults.colors(checkedColor = SageGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = log.subject,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.topic,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (log.isResolved) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                )
                if (log.note.isNotBlank()) {
                    Text(
                        text = log.note,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyStudyState(msg: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMaterialDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var type by remember { mutableStateOf("FORMULA_SHEET") }
    var description by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    val subjects = listOf("Physics", "Chemistry", "Biology", "Math", "English", "Bangla", "ICT", "General")
    val types = listOf("FORMULA_SHEET", "CHAPTER_NOTES", "QUESTION_BANK", "VIDEO", "DOC")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Study Material", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    placeholder = { Text("e.g. Physics Optics Formula Guide") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Brief Summary") },
                    placeholder = { Text("e.g. Lens maker formula & prisms") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content / Formulas / Link") },
                    placeholder = { Text("Paste key formulas, notes, or web references") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Text("Subject", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { s ->
                        FilterChip(selected = subject == s, onClick = { subject = s }, label = { Text(s) })
                    }
                }
                Text("Resource Type", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(types) { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t.replace("_", " ")) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, subject, type, description, content, tags)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF211823))
            ) {
                Text("Add Material", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var category by remember { mutableStateOf("FORMULA") }

    val subjects = listOf("Physics", "Chemistry", "Biology", "Math", "English", "Bangla", "ICT", "General")
    val categories = listOf("QUICK_NOTE", "FORMULA", "REVISION_KEY", "MOCK_ANALYSIS")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Study Note", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note Content *") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Subject", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { s ->
                        FilterChip(selected = subject == s, onClick = { subject = s }, label = { Text(s) })
                    }
                }
                Text("Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c.replace("_", " ")) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(title, content, subject, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF211823))
            ) {
                Text("Save Note", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddErrorLogDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var topic by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }

    val subjects = listOf("Physics", "Chemistry", "Biology", "Math", "English", "Bangla", "ICT", "General")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Weak Topic", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Weak Topic / Concept *") },
                    placeholder = { Text("e.g. Rotational Dynamics — torque derivation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Actionable Revision Note") },
                    placeholder = { Text("e.g. Re-solve practice problem 12 on Friday") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Subject", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { s ->
                        FilterChip(selected = subject == s, onClick = { subject = s }, label = { Text(s) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topic.isNotBlank()) {
                        onConfirm(topic, subject, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF211823))
            ) {
                Text("Log Weakness", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
