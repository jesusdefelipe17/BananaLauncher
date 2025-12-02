package com.example.afo.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afo.social.*
import com.example.afo.ui.theme.*
import kotlinx.coroutines.launch

enum class SocialTab {
    FRIENDS,
    REQUESTS,
    MESSAGES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen() {
    val context = LocalContext.current
    val socialManager = remember { SocialManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(SocialTab.FRIENDS) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }

    val friends by socialManager.friends.collectAsState(initial = emptyList())
    val friendRequests by socialManager.friendRequests.collectAsState(initial = emptyList())
    val messages by socialManager.messages.collectAsState(initial = emptyMap())
    val currentUser by socialManager.currentUser.collectAsState(initial = null)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header con tabs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shadowElevation = 4.dp
            ) {
                Column {
                    // Título
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Social",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            if (currentUser != null) {
                                Text(
                                    text = "@${currentUser!!.username}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { showAddFriendDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = "Agregar amigo",
                                tint = PrimaryBlue
                            )
                        }
                    }

                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = TextPrimary
                    ) {
                        Tab(
                            selected = selectedTab == SocialTab.FRIENDS,
                            onClick = { selectedTab = SocialTab.FRIENDS },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.People,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text("Amigos (${friends.size})")
                                }
                            },
                            selectedContentColor = PrimaryBlue,
                            unselectedContentColor = TextSecondary
                        )

                        Tab(
                            selected = selectedTab == SocialTab.REQUESTS,
                            onClick = { selectedTab = SocialTab.REQUESTS },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.GroupAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text("Solicitudes")
                                    if (friendRequests.isNotEmpty()) {
                                        Badge {
                                            Text(friendRequests.size.toString())
                                        }
                                    }
                                }
                            },
                            selectedContentColor = PrimaryBlue,
                            unselectedContentColor = TextSecondary
                        )

                        Tab(
                            selected = selectedTab == SocialTab.MESSAGES,
                            onClick = { selectedTab = SocialTab.MESSAGES },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Message,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text("Mensajes")
                                    val unreadCount = socialManager.getUnreadMessagesCount()
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = Color.Red
                                        ) {
                                            Text(unreadCount.toString())
                                        }
                                    }
                                }
                            },
                            selectedContentColor = PrimaryBlue,
                            unselectedContentColor = TextSecondary
                        )
                    }
                }
            }

            // Contenido según la tab seleccionada
            AnimatedContent(
                targetState = selectedTab,
                label = "socialTabs"
            ) { tab ->
                when (tab) {
                    SocialTab.FRIENDS -> {
                        FriendsListContent(
                            friends = friends,
                            onFriendClick = { friend ->
                                selectedFriend = friend
                                showChatDialog = true
                            }
                        )
                    }
                    SocialTab.REQUESTS -> {
                        FriendRequestsContent(
                            requests = friendRequests,
                            onAccept = { request ->
                                scope.launch {
                                    socialManager.acceptFriendRequest(request.id)
                                }
                            },
                            onReject = { request ->
                                scope.launch {
                                    socialManager.rejectFriendRequest(request.id)
                                }
                            }
                        )
                    }
                    SocialTab.MESSAGES -> {
                        MessagesListContent(
                            friends = friends,
                            messages = messages,
                            onChatClick = { friend ->
                                selectedFriend = friend
                                showChatDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Diálogo para agregar amigos
        if (showAddFriendDialog) {
            AddFriendDialog(
                onDismiss = { showAddFriendDialog = false },
                onSendRequest = { userId ->
                    scope.launch {
                        socialManager.sendFriendRequest(userId)
                        showAddFriendDialog = false
                    }
                }
            )
        }

        // Diálogo de chat
        if (showChatDialog && selectedFriend != null) {
            ChatDialog(
                friend = selectedFriend!!,
                messages = messages[selectedFriend!!.user.id] ?: emptyList(),
                currentUserId = currentUser?.id ?: "",
                onDismiss = {
                    showChatDialog = false
                    selectedFriend = null
                },
                onSendMessage = { content ->
                    scope.launch {
                        socialManager.sendMessage(selectedFriend!!.user.id, content)
                    }
                }
            )
        }
    }
}

@Composable
private fun FriendsListContent(
    friends: List<Friend>,
    onFriendClick: (Friend) -> Unit
) {
    if (friends.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.People,
            title = "Sin amigos aún",
            message = "Agrega amigos para jugar juntos y compartir tu progreso"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Amigos online primero
            val onlineFriends = friends.filter { it.user.isOnline }
            val offlineFriends = friends.filter { !it.user.isOnline }

            if (onlineFriends.isNotEmpty()) {
                item {
                    Text(
                        text = "ONLINE (${onlineFriends.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(onlineFriends) { friend ->
                    FriendCard(
                        friend = friend,
                        onClick = { onFriendClick(friend) }
                    )
                }
            }

            if (offlineFriends.isNotEmpty()) {
                item {
                    Text(
                        text = "OFFLINE (${offlineFriends.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(offlineFriends) { friend ->
                    FriendCard(
                        friend = friend,
                        onClick = { onFriendClick(friend) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendCard(
    friend: Friend,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con indicador online
            Box {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = PrimaryBlue.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = PrimaryBlue
                        )
                    }
                }

                if (friend.user.isOnline) {
                    Surface(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd),
                        shape = CircleShape,
                        color = SuccessGreen,
                        border = androidx.compose.foundation.BorderStroke(2.dp, CardBackground)
                    ) {}
                }
            }

            // Info del amigo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = friend.user.username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (friend.isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Favorito",
                            tint = FavoriteGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = when {
                        friend.user.isOnline && friend.user.currentGame != null ->
                            "🎮 Jugando ${friend.user.currentGame}"
                        friend.user.isOnline -> "En línea"
                        else -> "Desconectado"
                    },
                    fontSize = 13.sp,
                    color = if (friend.user.isOnline) SuccessGreen else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Botón de mensaje
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Filled.Message,
                    contentDescription = "Mensaje",
                    tint = PrimaryBlue
                )
            }
        }
    }
}

@Composable
private fun FriendRequestsContent(
    requests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit
) {
    if (requests.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.GroupAdd,
            title = "Sin solicitudes",
            message = "No tienes solicitudes de amistad pendientes"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests) { request ->
                FriendRequestCard(
                    request = request,
                    onAccept = { onAccept(request) },
                    onReject = { onReject(request) }
                )
            }
        }
    }
}

@Composable
private fun FriendRequestCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = PrimaryBlue
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = request.fromUser.username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Quiere ser tu amigo",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onAccept,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = SuccessGreen.copy(alpha = 0.2f),
                        contentColor = SuccessGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Aceptar"
                    )
                }

                IconButton(
                    onClick = onReject,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Red.copy(alpha = 0.2f),
                        contentColor = Color.Red
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Rechazar"
                    )
                }
            }
        }
    }
}

@Composable
private fun MessagesListContent(
    friends: List<Friend>,
    messages: Map<String, List<Message>>,
    onChatClick: (Friend) -> Unit
) {
    // Ordenar amigos por último mensaje
    val friendsWithMessages = friends.filter {
        messages[it.user.id]?.isNotEmpty() == true
    }.sortedByDescending { friend ->
        messages[friend.user.id]?.maxOfOrNull { it.timestamp } ?: 0
    }

    if (friendsWithMessages.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Message,
            title = "Sin mensajes",
            message = "Inicia una conversación con tus amigos"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friendsWithMessages) { friend ->
                val friendMessages = messages[friend.user.id] ?: emptyList()
                val lastMessage = friendMessages.lastOrNull()
                val unreadCount = friendMessages.count { !it.isRead }

                MessagePreviewCard(
                    friend = friend,
                    lastMessage = lastMessage,
                    unreadCount = unreadCount,
                    onClick = { onChatClick(friend) }
                )
            }
        }
    }
}

@Composable
private fun MessagePreviewCard(
    friend: Friend,
    lastMessage: Message?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (unreadCount > 0)
                PrimaryBlue.copy(alpha = 0.1f)
            else
                CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = PrimaryBlue
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friend.user.username,
                    fontSize = 16.sp,
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                    color = TextPrimary
                )

                if (lastMessage != null) {
                    Text(
                        text = lastMessage.content,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (unreadCount > 0) {
                Badge(
                    containerColor = PrimaryBlue
                ) {
                    Text(unreadCount.toString())
                }
            }
        }
    }
}

@Composable
private fun AddFriendDialog(
    onDismiss: () -> Unit,
    onSendRequest: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar amigo",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre de usuario") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "💡 Pide a tus amigos su nombre de usuario para agregarlos",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        // Aquí buscarías el usuario y enviarías la solicitud
                        onSendRequest(searchQuery)
                    }
                },
                enabled = searchQuery.isNotBlank()
            ) {
                Text("Enviar solicitud")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDialog(
    friend: Friend,
    messages: List<Message>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header del chat
                TopAppBar(
                    title = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = PrimaryBlue.copy(alpha = 0.3f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = friend.user.username,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (friend.user.isOnline) {
                                    Text(
                                        text = "En línea",
                                        fontSize = 12.sp,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                HorizontalDivider()

                // Mensajes
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { message ->
                        MessageBubble(
                            message = message,
                            isCurrentUser = message.senderId == currentUserId
                        )
                    }
                }

                HorizontalDivider()

                // Input de mensaje
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Escribe un mensaje...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White,
                            disabledContainerColor = PrimaryBlue.copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Enviar"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isCurrentUser) PrimaryBlue else CardBackground,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (isCurrentUser) Color.White else TextPrimary
                )

                Text(
                    text = formatTimestamp(message.timestamp),
                    fontSize = 11.sp,
                    color = if (isCurrentUser)
                        Color.White.copy(alpha = 0.7f)
                    else
                        TextSecondary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Ahora"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        else -> "${diff / 86400_000}d"
    }
}

