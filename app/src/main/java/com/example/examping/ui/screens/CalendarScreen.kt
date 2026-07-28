package com.example.examping.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChevronLeft
import androidx.compose.material.icons.automirrored.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.examping.ui.components.ExamCard
import com.example.examping.ui.theme.DarkBorder
import com.example.examping.ui.theme.DarkCard
import com.example.examping.ui.theme.DarkSurface
import com.example.examping.ui.viewmodel.ExamViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    val exams by viewModel.exams.collectAsState()

    var calendarCursor by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateIso by remember { mutableStateOf<String?>(null) }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("id", "ID")) }
    val dayNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    // Map of ISO date -> exam count
    val examsByDate = remember(exams) {
        exams.groupingBy { it.tanggal }.eachCount()
    }

    // Days calculation
    val daysInMonth = remember(calendarCursor) {
        val cal = calendarCursor.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
        
        val list = mutableListOf<String?>()
        for (i in 0 until firstDayOfWeek) list.add(null)
        
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        
        for (d in 1..maxDays) {
            val iso = String.format(Locale.US, "%04d-%02d-%02d", year, month, d)
            list.add(iso)
        }
        list
    }

    val selectedExams = remember(exams, selectedDateIso) {
        if (selectedDateIso == null) emptyList()
        else exams.filter { it.tanggal == selectedDateIso }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Kalender",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Titik menandai tanggal berujian",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar Card Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            color = DarkCard
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month Header Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val next = calendarCursor.clone() as Calendar
                        next.add(Calendar.MONTH, -1)
                        calendarCursor = next
                        selectedDateIso = null
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ChevronLeft, contentDescription = "Bulan sebelumnya")
                    }

                    Text(
                        text = monthFormat.format(calendarCursor.time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = {
                        val next = calendarCursor.clone() as Calendar
                        next.add(Calendar.MONTH, 1)
                        calendarCursor = next
                        selectedDateIso = null
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ChevronRight, contentDescription = "Bulan berikutnya")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day Headers (Sen - Min)
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayNames.forEach { dayName ->
                        Text(
                            text = dayName,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grid of Days
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(daysInMonth) { iso ->
                        if (iso == null) {
                            Box(modifier = Modifier.aspectRatio(1f))
                        } else {
                            val count = examsByDate[iso] ?: 0
                            val isSelected = selectedDateIso == iso
                            val dayNum = iso.split("-").last().toInt().toString()

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> DarkSurface
                                        }
                                    )
                                    .clickable {
                                        selectedDateIso = if (count > 0) iso else null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White
                                    )
                                    if (count > 0) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.primary
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Date Exams
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (selectedDateIso != null) {
                if (selectedExams.isEmpty()) {
                    item {
                        Text(
                            text = "Tidak ada ujian pada tanggal ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(selectedExams, key = { it.id }) { exam ->
                        ExamCard(
                            exam = exam,
                            onDelete = { viewModel.deleteExam(it.id) }
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "Ketuk tanggal bertitik untuk melihat detail ujiannya.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
