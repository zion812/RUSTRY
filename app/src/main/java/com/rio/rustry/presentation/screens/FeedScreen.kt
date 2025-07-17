package com.rio.rustry.presentation.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rio.rustry.domain.model.UserType
import com.rio.rustry.domain.model.SocialPost

/**
 * Social feed screen for posts, likes, and comments
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavHostController,
    userType: UserType = UserType.GENERAL
) {
    var showCreatePost by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    
    val posts = getSamplePosts()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Social Feed") },
                actions = {
                    IconButton(onClick = { showCreatePost = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Post",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { /* Handle notifications */ }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePost = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Create Post",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stories/Quick Actions Section
            item {
                QuickActionsCard(userType = userType)
            }
            
            // Posts
            items(posts) { post ->
                SocialPostCard(
                    post = post,
                    currentUserType = userType,
                    onLikeClick = { /* Handle like */ },
                    onCommentClick = { /* Handle comment */ },
                    onShareClick = { /* Handle share */ },
                    onUserClick = { /* Navigate to user profile */ },
                    onProductClick = { productId ->
                        navController.navigate("product_details/$productId")
                    }
                )
            }
            
            // Load more indicator
            item {
                if (posts.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
    
    // Create Post Dialog
    if (showCreatePost) {
        CreatePostDialog(
            userType = userType,
            onDismiss = { showCreatePost = false },
            onPostCreated = { 
                showCreatePost = false
                // Refresh feed
            }
        )
    }
}

@Composable
fun QuickActionsCard(userType: UserType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (userType) {
                    UserType.GENERAL -> {
                        QuickActionButton(
                            icon = Icons.Default.Camera,
                            label = "Share Photo",
                            onClick = { /* Handle photo share */ }
                        )
                        QuickActionButton(
                            icon = Icons.Default.QuestionAnswer,
                            label = "Ask Question",
                            onClick = { /* Handle question */ }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Star,
                            label = "Review",
                            onClick = { /* Handle review */ }
                        )
                    }
                    UserType.FARMER -> {
                        QuickActionButton(
                            icon = Icons.Default.Inventory,
                            label = "List Product",
                            onClick = { /* Handle product listing */ }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Campaign,
                            label = "Promote",
                            onClick = { /* Handle promotion */ }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Tips,
                            label = "Share Tip",
                            onClick = { /* Handle tip sharing */ }
                        )
                    }
                    UserType.HIGH_LEVEL -> {
                        QuickActionButton(
                            icon = Icons.Default.Science,
                            label = "Research",
                            onClick = { /* Handle research post */ }
                        )
                        QuickActionButton(
                            icon = Icons.Default.School,
                            label = "Educate",
                            onClick = { /* Handle educational post */ }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Analytics,
                            label = "Analysis",
                            onClick = { /* Handle analysis post */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SocialPostCard(
    post: SocialPost,
    currentUserType: UserType,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onUserClick: () -> Unit,
    onProductClick: (String) -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // User Avatar
                    Card(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.authorName.first().toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = post.authorName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (post.authorVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                        }
                        Text(
                            text = "${post.authorType.name} • ${getTimeAgo(post.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = { /* Handle menu */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Hashtags
            if (post.hashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.hashtags.joinToString(" ") { "#$it" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Media placeholder
            if (post.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                when (post.mediaType) {
                                    "video" -> Icons.Default.PlayArrow
                                    "audio" -> Icons.Default.AudioFile
                                    else -> Icons.Default.Image
                                },
                                contentDescription = "Media",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = post.mediaType.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Product showcase
            if (post.isShowcase && post.productId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    onClick = { onProductClick(post.productId) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = "Product",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Product Details",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Engagement Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${post.likeCount} likes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${post.commentCount} comments • ${post.shareCount} shares",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = {
                        isLiked = !isLiked
                        onLikeClick()
                    }
                ) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(18.dp),
                        tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Like")
                }
                
                TextButton(
                    onClick = { 
                        showComments = !showComments
                        onCommentClick() 
                    }
                ) {
                    Icon(
                        Icons.Default.Comment,
                        contentDescription = "Comment",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Comment")
                }
                
                TextButton(onClick = onShareClick) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
            
            // Comments Section
            if (showComments) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Add Comment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Write a comment...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                // Add comment
                                commentText = ""
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Sample Comments
                repeat(2) { index ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "U",
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "User ${index + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Great post! Very informative.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(
    userType: UserType,
    onDismiss: () -> Unit,
    onPostCreated: () -> Unit
) {
    var postContent by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("") }
    var isPromotion by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Post") },
        text = {
            Column {
                OutlinedTextField(
                    value = postContent,
                    onValueChange = { postContent = it },
                    label = { Text("What's on your mind?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = hashtags,
                    onValueChange = { hashtags = it },
                    label = { Text("Hashtags (optional)") },
                    placeholder = { Text("poultry farming chickens") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (userType == UserType.FARMER || userType == UserType.HIGH_LEVEL) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPromotion,
                            onCheckedChange = { isPromotion = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Promotional post",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (postContent.isNotBlank()) {
                        // Create post
                        onPostCreated()
                    }
                },
                enabled = postContent.isNotBlank()
            ) {
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper functions
private fun getSamplePosts(): List<SocialPost> {
    return listOf(
        SocialPost(
            id = "1",
            authorId = "user1",
            authorName = "Ravi Kumar",
            authorType = UserType.FARMER,
            authorVerified = true,
            content = "Just received a new batch of healthy Kadaknath chicks! They're looking great and very active. Perfect for breeding programs. 🐓",
            mediaUrls = listOf("image1.jpg"),
            mediaType = "image",
            hashtags = listOf("kadaknath", "chicks", "breeding", "healthy"),
            location = "Hyderabad",
            isShowcase = true,
            productId = "product1",
            likeCount = 24,
            commentCount = 8,
            shareCount = 3,
            viewCount = 156,
            createdAt = System.currentTimeMillis() - 3600000 // 1 hour ago
        ),
        SocialPost(
            id = "2",
            authorId = "user2",
            authorName = "Dr. Sunita Farms",
            authorType = UserType.HIGH_LEVEL,
            authorVerified = true,
            content = "Important vaccination schedule reminder for all poultry farmers. Newcastle disease vaccination should be done every 3 months for optimal protection. Here's a detailed guide...",
            hashtags = listOf("vaccination", "newcastle", "health", "farming", "tips"),
            location = "Warangal",
            likeCount = 45,
            commentCount = 12,
            shareCount = 18,
            viewCount = 289,
            createdAt = System.currentTimeMillis() - 7200000 // 2 hours ago
        ),
        SocialPost(
            id = "3",
            authorId = "user3",
            authorName = "Green Valley Poultry",
            authorType = UserType.FARMER,
            authorVerified = false,
            content = "Looking for advice on organic feed options for free-range chickens. What has worked best for your flock? Any recommendations?",
            hashtags = listOf("organic", "feed", "freerange", "advice"),
            location = "Vijayawada",
            likeCount = 12,
            commentCount = 15,
            shareCount = 2,
            viewCount = 98,
            createdAt = System.currentTimeMillis() - 10800000 // 3 hours ago
        )
    )
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}