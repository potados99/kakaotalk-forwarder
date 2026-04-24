package com.potados.kakaotalkforwarder.ui.history

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.potados.kakaotalkforwarder.R
import com.potados.kakaotalkforwarder.data.db.ForwardLog
import com.potados.kakaotalkforwarder.data.db.ForwardStatus

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.factory(LocalContext.current)
    ),
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<ForwardLog?>(null) }

    Box(modifier.fillMaxSize()) {
        if (logs.isEmpty()) {
            EmptyState(Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(logs, key = { it.id }) { log ->
                    HistoryItem(
                        log = log,
                        onClick = { selected = log },
                        onRetry = { viewModel.retry(log.id) },
                    )
                }
            }
        }
    }

    selected?.let { log ->
        DetailSheet(log, onDismiss = { selected = null })
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.history_empty),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(24.dp),
    )
}

@Composable
private fun HistoryItem(
    log: ForwardLog,
    onClick: () -> Unit,
    onRetry: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(log.status)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = log.senderTitle,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = log.createdAt.relative(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = log.menuText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (log.status == ForwardStatus.FAILED && !log.errorMessage.isNullOrBlank()) {
                    Text(
                        text = log.errorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (log.status == ForwardStatus.FAILED) {
                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(R.string.history_retry))
                }
            }
        }
    }
}

@Composable
private fun StatusDot(status: ForwardStatus) {
    val color = when (status) {
        ForwardStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        ForwardStatus.FAILED -> MaterialTheme.colorScheme.error
        ForwardStatus.PENDING -> MaterialTheme.colorScheme.tertiary
    }
    Surface(
        shape = CircleShape,
        color = color,
        modifier = Modifier,
    ) {
        Box(Modifier.size(10.dp)) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailSheet(log: ForwardLog, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.history_detail_title),
                style = MaterialTheme.typography.titleMedium,
            )
            LabeledRow(label = "발신자", value = log.senderTitle)
            LabeledRow(label = "상태", value = log.status.label())
            LabeledRow(
                label = "시각",
                value = DateUtils.formatDateTime(
                    LocalContext.current,
                    log.createdAt,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME,
                ),
            )
            log.httpCode?.let { LabeledRow(label = "HTTP", value = it.toString()) }
            log.errorMessage?.takeIf { it.isNotBlank() }?.let {
                LabeledRow(label = "에러", value = it)
            }
            Text(
                text = "본문",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(log.menuText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(56.dp),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ForwardStatus.label(): String = stringResource(
    when (this) {
        ForwardStatus.SUCCESS -> R.string.history_status_success
        ForwardStatus.FAILED -> R.string.history_status_failed
        ForwardStatus.PENDING -> R.string.history_status_pending
    }
)

private fun Long.relative(): String =
    DateUtils.getRelativeTimeSpanString(this, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
