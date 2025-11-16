package app.isaruff.composehaven.concepts.toolbar.two_stage_toolbar


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class DemoCard(
    val title: String,
    val subtitle: String,
    val value: String,
    val change: String,
    val isPositive: Boolean,
    val startColor: Color,
    val endColor: Color,
    val icon: ImageVector
)

private data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: Double,
    val date: String,
    val status: TransactionStatus,
    val type: TransactionType,
    val iconColor: Color
)

private enum class TransactionStatus { COMPLETED, PENDING, FAILED }
private enum class TransactionType { INCOME, EXPENSE }

private val demoList = listOf(
    DemoCard(
        "Total Balance",
        "4 accounts",
        "$24,582.45",
        "+12.5%",
        true,
        Color(0xFF667EEA),
        Color(0xFF764BA2),
        Icons.Default.AccountBox
    ),
    DemoCard(
        "Investments",
        "Portfolio",
        "$18,420.30",
        "+8.2%",
        true,
        Color(0xFF11998E),
        Color(0xFF38EF7D),
        Icons.Default.KeyboardArrowUp
    ),
    DemoCard(
        "Credit Cards",
        "3 active",
        "$3,240.00",
        "-2.1%",
        false,
        Color(0xFFFA709A),
        Color(0xFFFEE140),
        Icons.Default.Star
    ),
    DemoCard(
        "Payments",
        "This month",
        "$12,840.50",
        "+15.8%",
        true,
        Color(0xFFFF6B6B),
        Color(0xFFFFE66D),
        Icons.Default.Home
    ),
    DemoCard(
        "Savings",
        "Goals: 3/5",
        "$8,950.00",
        "+5.3%",
        true,
        Color(0xFF4E54C8),
        Color(0xFF8F94FB),
        Icons.Default.Settings
    )
)

private val transactionsList = listOf(
    Transaction(
        "1",
        "Salary Deposit",
        "Income",
        5420.00,
        "Nov 15",
        TransactionStatus.COMPLETED,
        TransactionType.INCOME,
        Color(0xFF10B981)
    ),
    Transaction(
        "2",
        "Rent Payment",
        "Housing",
        -1850.00,
        "Nov 14",
        TransactionStatus.COMPLETED,
        TransactionType.EXPENSE,
        Color(0xFFEF4444)
    ),
    Transaction(
        "3",
        "Grocery Store",
        "Food & Dining",
        -142.35,
        "Nov 14",
        TransactionStatus.COMPLETED,
        TransactionType.EXPENSE,
        Color(0xFFF59E0B)
    ),
    Transaction(
        "4",
        "Stock Purchase",
        "Investment",
        -500.00,
        "Nov 13",
        TransactionStatus.PENDING,
        TransactionType.EXPENSE,
        Color(0xFF8B5CF6)
    ),
    Transaction(
        "5",
        "Freelance Project",
        "Income",
        2100.00,
        "Nov 12",
        TransactionStatus.COMPLETED,
        TransactionType.INCOME,
        Color(0xFF10B981)
    ),
    Transaction(
        "6",
        "Electric Bill",
        "Utilities",
        -89.50,
        "Nov 12",
        TransactionStatus.COMPLETED,
        TransactionType.EXPENSE,
        Color(0xFF3B82F6)
    ),
    Transaction(
        "7",
        "Restaurant",
        "Food & Dining",
        -67.80,
        "Nov 11",
        TransactionStatus.COMPLETED,
        TransactionType.EXPENSE,
        Color(0xFFF59E0B)
    ),
    Transaction(
        "8",
        "Gas Station",
        "Transportation",
        -52.00,
        "Nov 10",
        TransactionStatus.COMPLETED,
        TransactionType.EXPENSE,
        Color(0xFF06B6D4)
    ),
    Transaction(
        "9",
        "Online Shopping",
        "Shopping",
        -234.99,
        "Nov 10",
        TransactionStatus.PENDING,
        TransactionType.EXPENSE,
        Color(0xFFEC4899)
    ),
    Transaction(
        "10",
        "Coffee Shop",
        "Food & Dining",
        -15.40,
        "Nov 9",
        TransactionStatus.COMPLETED,
        TransactionType.EXPENSE,
        Color(0xFFF59E0B)
    ),
    Transaction(
        "11",
        "Gym Membership",
        "Health",
        -59.99,
        "Nov 8",
        TransactionStatus.COMPLETED,
        TransactionType.EXPENSE,
        Color(0xFF14B8A6)
    ),
    Transaction(
        "12",
        "Dividend Payment",
        "Investment",
        145.30,
        "Nov 8",
        TransactionStatus.COMPLETED,
        TransactionType.INCOME,
        Color(0xFF10B981)
    ),
    Transaction(
        "13",
        "Phone Bill",
        "Utilities",
        -75.00,
        "Nov 7",
        TransactionStatus.COMPLETED,
        TransactionType.EXPENSE,
        Color(0xFF3B82F6)
    ),
    Transaction(
        "14",
        "Uber Ride",
        "Transportation",
        -28.50,
        "Nov 6",
        TransactionStatus.COMPLETED,
        TransactionType.EXPENSE,
        Color(0xFF06B6D4)
    ),
    Transaction(
        "15",
        "Book Purchase",
        "Education",
        -42.00,
        "Nov 5",
        TransactionStatus.COMPLETED,
        TransactionType.EXPENSE,
        Color(0xFF8B5CF6)
    ),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun TwoStageToolbarScaffoldPreview_Fintech() {
    val state = rememberTwoStageToolbarState()

    MaterialTheme {
        TwoStageToolbarScaffold(
            state = state,
            firstStage = {
                FirstStageRow(
                    items = demoList,
                    height = 180.dp
                )
            },
            secondStage = {
                SecondStageChips(items = demoList)
            },
            content = { nestedScrollConnection ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection)
                        .background(Color(0xFFF8FAFC)),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    item {
                        SectionHeader("Recent Transactions")
                    }

                    items(transactionsList) { transaction ->
                        EnhancedTransactionCard(transaction = transaction)
                    }
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun FirstStageRow(items: List<DemoCard>, height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF334155)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Financial Overview",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items) { item ->
                    EnhancedTopCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun EnhancedTopCard(item: DemoCard) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(170.dp)
            .scale(scale)
            .shadow(
                elevation = if (pressed) 4.dp else 8.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pressed = !pressed
            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .height(110.dp)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(item.startColor, item.endColor)
                        )
                    )
            ) {
                // Decorative circles
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-20).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = (-15).dp, y = 15.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A)
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (item.isPositive)
                                    Color(0xFF10B981).copy(alpha = 0.1f)
                                else
                                    Color(0xFFEF4444).copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (item.isPositive) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = item.change,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (item.isPositive) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecondStageChips(items: List<DemoCard>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1E293B),
        shadowElevation = 4.dp
    ) {
        LazyRow(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(item.startColor, item.endColor)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(40.dp)
                )
            }
        }
    }
}

@Composable
private fun EnhancedTransactionCard(transaction: Transaction) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(transaction.iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == TransactionType.INCOME)
                        Icons.Default.ArrowDropDown
                    else
                        Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = transaction.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B)
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCBD5E1))
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B)
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (transaction.amount > 0) "+$${
                        String.format(
                            "%.2f",
                            transaction.amount
                        )
                    }"
                    else "-$${String.format("%.2f", -transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.type == TransactionType.INCOME)
                            Color(0xFF10B981)
                        else
                            Color(0xFF0F172A)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (transaction.status) {
                                TransactionStatus.COMPLETED -> Color(0xFF10B981).copy(alpha = 0.1f)
                                TransactionStatus.PENDING -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                                TransactionStatus.FAILED -> Color(0xFFEF4444).copy(alpha = 0.1f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = transaction.status.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when (transaction.status) {
                                TransactionStatus.COMPLETED -> Color(0xFF10B981)
                                TransactionStatus.PENDING -> Color(0xFFF59E0B)
                                TransactionStatus.FAILED -> Color(0xFFEF4444)
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}