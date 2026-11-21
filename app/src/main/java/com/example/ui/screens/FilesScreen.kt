package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.AppLanguage
import com.example.data.local.bi
import com.example.data.local.db.UserFileEntity
import com.example.ui.viewmodel.NursingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val fileFolders = listOf(
    "All",
    "Lecture Notes",
    "PDF Books",
    "Assignments",
    "Clinical Sheets",
    "Images",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: NursingViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val lang = prefs.language
    val userFiles by viewModel.userFiles.collectAsStateWithLifecycle()
    val studyItems by viewModel.studyItems.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var pendingPickedFile by remember { mutableStateOf<PickedFileMeta?>(null) }

    val context = LocalContext.current

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            pendingPickedFile = readUriMetadata(context, uri, defaultFolder = "PDF Books")
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            pendingPickedFile = readUriMetadata(context, uri, defaultFolder = "Images")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { documentPickerLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.testTag("add_file_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = bi(lang, "Add file", "ফাইল যোগ করুন"))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = bi(lang, "Nursing Files & Study Vault", "নার্সিং ফাইল ভল্ট ও লাইব্রেরি"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(bi(lang, "My Files (${userFiles.size})", "আমার ফাইলস (${userFiles.size})")) },
                            modifier = Modifier.testTag("files_tab_vault")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(bi(lang, "Notes & Books", "নোটস ও বই")) },
                            modifier = Modifier.testTag("files_tab_library")
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                PersonalFilesVaultTab(
                    lang = lang,
                    files = userFiles,
                    onPickDocument = { documentPickerLauncher.launch(arrayOf("*/*")) },
                    onPickImage = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onDeleteFile = viewModel::deleteUserFile
                )
            } else {
                NotesAndResourcesTab(
                    lang = lang,
                    items = studyItems.filter { it.itemType !in listOf("CAREER", "EXAM", "SCHOLARSHIP") },
                    onToggleBookmark = viewModel::toggleStudyBookmark,
                    onToggleOffline = viewModel::toggleStudyOffline
                )
            }
        }
    }

    pendingPickedFile?.let { picked ->
        AddFileMetadataDialog(
            lang = lang,
            initial = picked,
            onDismiss = { pendingPickedFile = null },
            onSave = { title, folder, notes ->
                viewModel.addUserFile(
                    title = title,
                    folder = folder,
                    uriString = picked.uri.toString(),
                    mimeType = picked.mimeType,
                    sizeBytes = picked.sizeBytes,
                    notes = notes
                )
                pendingPickedFile = null
            }
        )
    }
}

@Composable
private fun PersonalFilesVaultTab(
    lang: AppLanguage,
    files: List<UserFileEntity>,
    onPickDocument: () -> Unit,
    onPickImage: () -> Unit,
    onDeleteFile: (String) -> Unit
) {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf("All") }

    val filtered = remember(files, search, selectedFolder) {
        files.filter { file ->
            (selectedFolder == "All" || file.folder == selectedFolder) &&
                (search.isBlank() || file.title.contains(search, true) || file.notes.contains(search, true) || file.folder.contains(search, true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 760.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = bi(lang, "Store PDFs, Lecture Slides, Notes & Images", "যেকোনো PDF, লেকচার নোট, অ্যাসাইনমেন্ট ও ছবি সংরক্ষণ করুন"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onPickDocument,
                            modifier = Modifier.weight(1f).testTag("upload_document_button")
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(bi(lang, "Add PDF / Doc", "PDF / ফাইল যোগ"))
                        }
                        OutlinedButton(
                            onClick = onPickImage,
                            modifier = Modifier.weight(1f).testTag("upload_image_file_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(bi(lang, "Add Photo", "ছবি যোগ"))
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(bi(lang, "Search your files...", "আপনার ফাইল খুঁজুন...")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fileFolders) { folder ->
                    FilterChip(
                        selected = selectedFolder == folder,
                        onClick = { selectedFolder = folder },
                        label = { Text(folder) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = bi(lang, "No files added yet", "এখনো কোনো ফাইল যোগ করা হয়নি"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = bi(
                                lang,
                                "Tap 'Add PDF / Doc' or '+' to save your nursing PDFs, lecture notes, care plans, and study photos here.",
                                "আপনার নার্সিং PDF, লেকচার নোট, কেয়ার প্ল্যান ও স্টাডি ছবি সংরক্ষণ করতে 'PDF / ফাইল যোগ' বাটনে চাপ দিন।"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { file ->
                UserFileCard(
                    file = file,
                    lang = lang,
                    onOpen = { openUserFile(context, file) },
                    onShare = { shareUserFile(context, file) },
                    onDelete = { onDeleteFile(file.id) }
                )
            }
        }
    }
}

@Composable
private fun UserFileCard(
    file: UserFileEntity,
    lang: AppLanguage,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val isImage = file.mimeType.startsWith("image/")
    val isPdf = file.mimeType.contains("pdf", ignoreCase = true)
    val dateLabel = remember(file.addedAt) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(file.addedAt))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                modifier = Modifier.size(52.dp)
            ) {
                if (isImage) {
                    AsyncImage(
                        model = file.uriString,
                        contentDescription = file.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = file.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${file.folder} • ${formatFileSize(file.sizeBytes)} • $dateLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (file.notes.isNotBlank()) {
                    Text(
                        text = file.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onOpen, modifier = Modifier.minimumInteractiveComponentSize()) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = bi(lang, "Open file", "ফাইল খুলুন"))
            }
            IconButton(onClick = onShare, modifier = Modifier.minimumInteractiveComponentSize()) {
                Icon(Icons.Default.Share, contentDescription = bi(lang, "Share file", "শেয়ার করুন"))
            }
            IconButton(onClick = onDelete, modifier = Modifier.minimumInteractiveComponentSize()) {
                Icon(Icons.Default.DeleteOutline, contentDescription = bi(lang, "Delete file", "মুছে ফেলুন"), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddFileMetadataDialog(
    lang: AppLanguage,
    initial: PickedFileMeta,
    onDismiss: () -> Unit,
    onSave: (title: String, folder: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf(initial.displayName) }
    var folder by remember { mutableStateOf(initial.defaultFolder) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(bi(lang, "Save File to Vault", "ভল্টে ফাইল সংরক্ষণ করুন")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(bi(lang, "File Title", "ফাইলের নাম")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(bi(lang, "Folder", "ফোল্ডার"), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    fileFolders.drop(1).forEach { f ->
                        FilterChip(
                            selected = folder == f,
                            onClick = { folder = f },
                            label = { Text(f) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(bi(lang, "Notes (optional)", "নোট (ঐচ্ছিক)")) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, folder, notes) }) {
                Text(bi(lang, "Save File", "সংরক্ষণ করুন"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(bi(lang, "Cancel", "বাতিল"))
            }
        }
    )
}

private data class PickedFileMeta(
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val defaultFolder: String
)

private fun readUriMetadata(context: Context, uri: Uri, defaultFolder: String): PickedFileMeta {
    val mimeType = context.contentResolver.getType(uri) ?: "*/*"
    var name = uri.lastPathSegment?.substringAfterLast('/') ?: "Nursing Document"
    var size = 0L
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: name
                if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
            }
        }
    }
    return PickedFileMeta(uri, name, mimeType, size, defaultFolder)
}

private fun openUserFile(context: Context, file: UserFileEntity) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(file.uriString), file.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, file.title))
    }
}

private fun shareUserFile(context: Context, file: UserFileEntity) {
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = file.mimeType
            putExtra(Intent.EXTRA_STREAM, Uri.parse(file.uriString))
            putExtra(Intent.EXTRA_TEXT, file.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share ${file.title}"))
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "Saved File"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format(Locale.US, "%.1f MB", mb)
}
