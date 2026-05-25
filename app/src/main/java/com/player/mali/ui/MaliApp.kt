package com.player.mali.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import com.player.mali.data.MoneyTransaction
import com.player.mali.data.TransactionType
import com.player.mali.viewmodel.MaliUiState
import com.player.mali.viewmodel.MaliViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AppGreen = Color(0xFF16A34A)
private val AppRed = Color(0xFFDC2626)
private val AppBlue = Color(0xFF2563EB)
private val AppBg = Color(0xFFF6F7FB)
private val AppText = Color(0xFF111827)
private val Muted = Color(0xFF6B7280)

private val MaliLightColors = lightColorScheme(
    primary = AppBlue,
    secondary = AppGreen,
    background = AppBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AppText,
    onSurface = AppText
)

private enum class Screen(val title: String, val icon: String) {
    Home("الرئيسية", "🏠"),
    Transactions("المعاملات", "↕️"),
    Budget("الميزانية", "📊"),
    Reports("التقارير", "📈"),
    Settings("الإعدادات", "⚙️")
}

@Composable
fun MaliApp(viewModel: MaliViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var showAddDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = MaliLightColors) {
            Scaffold(
                containerColor = AppBg,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = AppBlue,
                        contentColor = Color.White
                    ) { Text("+", fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                },
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        Screen.entries.forEach { screen ->
                            NavigationBarItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                icon = { Text(screen.icon, fontSize = 20.sp) },
                                label = { Text(screen.title, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    color = AppBg
                ) {
                    when (currentScreen) {
                        Screen.Home -> HomeScreen(uiState)
                        Screen.Transactions -> TransactionsScreen(uiState, onDelete = viewModel::deleteTransaction)
                        Screen.Budget -> ComingSoonScreen("الميزانية", "هنا سنضيف حدود المصاريف الشهرية لكل تصنيف.")
                        Screen.Reports -> ComingSoonScreen("التقارير", "هنا ستظهر الرسوم والتحليل الشهري لأموالك.")
                        Screen.Settings -> ComingSoonScreen("الإعدادات", "لاحقًا: القفل، البصمة، النسخ الاحتياطي والتصدير.")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { type, amount, category, note, method ->
                viewModel.addTransaction(type, amount, category, note, method)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun HomeScreen(uiState: MaliUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "مالي",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppText
            )
            Text("مسير أموالك الشخصي بدون إنترنت", color = Muted, fontSize = 14.sp)
        }
        item {
            BalanceCard(uiState.balance)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    title = "المداخيل",
                    value = formatDzd(uiState.totalIncome),
                    color = AppGreen
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    title = "المصاريف",
                    value = formatDzd(uiState.totalExpense),
                    color = AppRed
                )
            }
        }
        item {
            SectionHeader("آخر المعاملات")
        }
        if (uiState.transactions.isEmpty()) {
            item { EmptyCard("لا توجد معاملات بعد. اضغط + لإضافة أول مدخول أو مصروف.") }
        } else {
            items(uiState.transactions.take(5)) { transaction ->
                TransactionRow(transaction = transaction, onDelete = null)
            }
        }
    }
}

@Composable
private fun TransactionsScreen(uiState: MaliUiState, onDelete: (MoneyTransaction) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("المعاملات", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = AppText)
            Text("كل المداخيل والمصاريف المحفوظة محليًا", color = Muted)
        }
        if (uiState.transactions.isEmpty()) {
            item { EmptyCard("لا توجد معاملات لعرضها.") }
        } else {
            items(uiState.transactions) { transaction ->
                TransactionRow(transaction = transaction, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AppText),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text("الرصيد الحالي", color = Color(0xFFD1D5DB), fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatDzd(balance),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(12.dp))
            Text("هذا الرقم = مجموع المداخيل - مجموع المصاريف", color = Color(0xFF9CA3AF), fontSize = 13.sp)
        }
    }
}

@Composable
private fun MiniStatCard(modifier: Modifier, title: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AppText)
}

@Composable
private fun EmptyCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            color = Muted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TransactionRow(transaction: MoneyTransaction, onDelete: ((MoneyTransaction) -> Unit)?) {
    val isIncome = transaction.type == TransactionType.INCOME
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isIncome) "⬆️" else "⬇️",
                fontSize = 24.sp,
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isIncome) Color(0xFFEAF7EE) else Color(0xFFFDECEC), RoundedCornerShape(14.dp))
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(transaction.category, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppText)
                Text(
                    buildString {
                        append(transaction.paymentMethod)
                        if (transaction.note.isNotBlank()) append(" • ${transaction.note}")
                        append(" • ${formatDate(transaction.dateMillis)}")
                    },
                    color = Muted,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isIncome) "+" else "-") + formatDzd(transaction.amount),
                    color = if (isIncome) AppGreen else AppRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                if (onDelete != null) {
                    TextButton(onClick = { onDelete(transaction) }) { Text("حذف", color = AppRed) }
                }
            }
        }
    }
}

@Composable
private fun ComingSoonScreen(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = AppText)
        Spacer(Modifier.height(12.dp))
        Text(subtitle, color = Muted, textAlign = TextAlign.Center, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (TransactionType, String, String, String, String) -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("نقدًا") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة معاملة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { type = TransactionType.EXPENSE },
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == TransactionType.EXPENSE) AppRed else Color(0xFFE5E7EB))
                    ) { Text("مصروف") }
                    Button(
                        onClick = { type = TransactionType.INCOME },
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == TransactionType.INCOME) AppGreen else Color(0xFFE5E7EB))
                    ) { Text("مدخول") }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("المبلغ بالدينار") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = method,
                    onValueChange = { method = it },
                    label = { Text("طريقة الدفع") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(type, amount, category, note, method) }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

private fun formatDzd(value: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ar", "DZ"))
    return formatter.format(value) + " دج"
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("yyyy/MM/dd", Locale("ar", "DZ")).format(Date(millis))
}
