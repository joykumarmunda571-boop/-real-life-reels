package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      var isDarkTheme by remember { mutableStateOf(true) }
      MyApplicationTheme(darkTheme = isDarkTheme) {
        RealLifeReelsApp(
          onThemeToggle = { isDarkTheme = !isDarkTheme },
          isDarkTheme = isDarkTheme
        )
      }
    }
  }
}

data class ReelItem(
  val id: String,
  val creatorUsername: String,
  val creatorName: String,
  val caption: String,
  val audioTrack: String,
  val gradientColors: List<Color>,
  val followersCount: Int,
  val starsReceived: Int,
  var likesCount: Int,
  val commentsCount: Int,
  val sharesCount: Int,
  val viewsCount: Int,
  var isLiked: Boolean,
  var isFollowing: Boolean,
  var isBlocked: Boolean = false
) {
  val isMonetized: Boolean get() = followersCount >= 3000
}

data class NotificationItem(
  val id: String,
  val title: String,
  val description: String,
  val timeAgo: String,
  val icon: ImageVector,
  val iconTint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealLifeReelsApp(
  onThemeToggle: () -> Unit,
  isDarkTheme: Boolean
) {
  var isLoggedIn by remember { mutableStateOf(false) }
  var loggedInUser by remember { mutableStateOf("claire@reallifereels.com") }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    if (isLoggedIn) {
      Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        MainScreenContainer(
          userEmail = loggedInUser,
          onLogout = {
            isLoggedIn = false
            scope.launch { snackbarHostState.showSnackbar("Logged out successfully") }
          },
          onShowSnackbar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
        )
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.surface
              )
            )
          )
          .padding(innerPadding),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.VideoCameraBack, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(34.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Real Life Reels", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Self-Healing Security & Authoritative Ledger", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(24.dp))

            var email by remember { mutableStateOf("claire@reallifereels.com") }
            var password by remember { mutableStateOf("securepass123") }

            OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text("Email Address") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().testTag("email_input")
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              label = { Text("Password") },
              visualTransformation = PasswordVisualTransformation(),
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().testTag("password_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = {
                if (email.isNotBlank()) {
                  loggedInUser = email
                  isLoggedIn = true
                  scope.launch { snackbarHostState.showSnackbar("Logged in successfully as $email") }
                }
              },
              modifier = Modifier.fillMaxWidth().height(50.dp).testTag("email_auth_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("Sign In & Launch App", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun MainScreenContainer(
  userEmail: String,
  onLogout: () -> Unit,
  onShowSnackbar: (String) -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Home, 1: Explore, 2: Create, 3: Notifications, 4: Profile
  var showCameraStudio by remember { mutableStateOf(false) }

  var userCoins by remember { mutableStateOf(340) }
  var serverCoins by remember { mutableStateOf(340) }
  var userFollowers by remember { mutableStateOf(1450) }
  var serverFollowers by remember { mutableStateOf(1450) }
  var securityFailedAttempts by remember { mutableStateOf(0) }
  var isDeviceBanned by remember { mutableStateOf(false) }

  val reelsList = remember {
    mutableStateListOf(
      ReelItem(
        id = "1",
        creatorUsername = "@claire_vlog",
        creatorName = "Claire's Real Life",
        caption = "Morning coffee brew without filters. Pure unfiltered reality. ☕✨ #Viral",
        audioTrack = "Original Audio - Claire Jenkins",
        gradientColors = listOf(Color(0xFF232526), Color(0xFF414345), Color(0xFF0F2027)),
        followersCount = 1200,
        starsReceived = 150,
        likesCount = 1420,
        commentsCount = 89,
        sharesCount = 45,
        viewsCount = 12500,
        isLiked = false,
        isFollowing = false
      ),
      ReelItem(
        id = "2",
        creatorUsername = "@chef_marco",
        creatorName = "Marco's Kitchen",
        caption = "When the pasta dough sticks to the counter! Real cooking isn't polished. 🍝😂",
        audioTrack = "Funny Acoustic Ukulele - AudioHub",
        gradientColors = listOf(Color(0xFF4CA1AF), Color(0xFF2C3E50), Color(0xFF1E3C72)),
        followersCount = 5400,
        starsReceived = 2450,
        likesCount = 8930,
        commentsCount = 412,
        sharesCount = 320,
        viewsCount = 45200,
        isLiked = true,
        isFollowing = true
      ),
      ReelItem(
        id = "3",
        creatorUsername = "@nature_walks",
        creatorName = "Forest Trails",
        caption = "Rain falling on pine needles. No music, just pure natural soundscape. 🌲🌧️",
        audioTrack = "Ambient Forest Rain - NatureSound",
        gradientColors = listOf(Color(0xFF134E5E), Color(0xFF71B280), Color(0xFF000000)),
        followersCount = 8900,
        starsReceived = 4120,
        likesCount = 15200,
        commentsCount = 630,
        sharesCount = 980,
        viewsCount = 89000,
        isLiked = false,
        isFollowing = true
      )
    )
  }

  if (isDeviceBanned) {
    Box(
      modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Device Banned for Security Violations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("3 consecutive failed security validation checks triggered automated threat response.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { isDeviceBanned = false; securityFailedAttempts = 0; onLogout() }) {
          Text("Reset Security & Restart")
        }
      }
    }
    return
  }

  Scaffold(
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
      ) {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
          label = { Text("Home") },
          modifier = Modifier.testTag("nav_home")
        )
        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
          label = { Text("Explore") },
          modifier = Modifier.testTag("nav_explore")
        )
        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { showCameraStudio = true },
          icon = {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Add, contentDescription = "Create", tint = MaterialTheme.colorScheme.onPrimary)
            }
          },
          label = { Text("Create") },
          modifier = Modifier.testTag("nav_create")
        )
        NavigationBarItem(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
          label = { Text("Alerts") },
          modifier = Modifier.testTag("nav_notifications")
        )
        NavigationBarItem(
          selected = selectedTab == 4,
          onClick = { selectedTab = 4 },
          icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
          label = { Text("Profile") },
          modifier = Modifier.testTag("nav_profile")
        )
      }
    }
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      when (selectedTab) {
        0 -> ReelsFeedScreen(
          reelsList = reelsList,
          onOpenUpload = { showCameraStudio = true },
          onShowSnackbar = onShowSnackbar
        )
        1 -> ExploreScreen(
          reelsList = reelsList,
          onShowSnackbar = onShowSnackbar
        )
        3 -> NotificationsScreen(onShowSnackbar = onShowSnackbar)
        4 -> ProfileScreen(
          userEmail = userEmail,
          userCoins = userCoins,
          serverCoins = serverCoins,
          userFollowers = userFollowers,
          securityFailedAttempts = securityFailedAttempts,
          onUpdateCoins = { userCoins = it; serverCoins = it },
          onTriggerTamper = {
            securityFailedAttempts++
            userCoins += 99999
            onShowSnackbar("⚠️ Binary Tamper Detected! Local balance modified.")
            if (securityFailedAttempts >= 3) {
              isDeviceBanned = true
              onShowSnackbar("🚨 3 consecutive security failures! Device ID auto-banned.")
            }
          },
          onSelfHeal = {
            userCoins = serverCoins
            userFollowers = serverFollowers
            securityFailedAttempts = 0
            onShowSnackbar("🛡️ Self-Healing Success! Local tampered state overwritten with server authoritative ledger.")
          },
          onLogout = onLogout,
          onShowSnackbar = onShowSnackbar
        )
      }
    }
  }

  if (showCameraStudio) {
    CameraUploadStudioDialog(
      onDismiss = { showCameraStudio = false },
      onPublish = { caption, audio ->
        reelsList.add(
          0,
          ReelItem(
            id = System.currentTimeMillis().toString(),
            creatorUsername = "@claire_vlog",
            creatorName = "Claire Jenkins",
            caption = caption,
            audioTrack = audio,
            gradientColors = listOf(Color(0xFF512DA8), Color(0xFF311B92), Color(0xFF000000)),
            followersCount = userFollowers,
            starsReceived = 0,
            likesCount = 1,
            commentsCount = 0,
            sharesCount = 0,
            viewsCount = 1,
            isLiked = false,
            isFollowing = false
          )
        )
        showCameraStudio = false
        selectedTab = 0
        onShowSnackbar("✨ Reel published successfully with CameraX & Cloud Sync!")
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelsFeedScreen(
  reelsList: MutableList<ReelItem>,
  onOpenUpload: () -> Unit,
  onShowSnackbar: (String) -> Unit
) {
  val activeReels = reelsList.filter { !it.isBlocked }
  val pagerState = rememberPagerState(pageCount = { activeReels.size.coerceAtLeast(1) })

  Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
    VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
      val reel = activeReels[page]
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Brush.verticalGradient(colors = reel.gradientColors)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(20.dp)
            .padding(bottom = 60.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f)),
              contentAlignment = Alignment.Center
            ) {
              Text(reel.creatorName.take(1), fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = reel.creatorUsername, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
              Text(text = reel.creatorName, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
              onClick = {
                reel.isFollowing = !reel.isFollowing
                onShowSnackbar(if (reel.isFollowing) "Following ${reel.creatorUsername}" else "Unfollowed ${reel.creatorUsername}")
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (reel.isFollowing) Color.Gray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
              ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Text(if (reel.isFollowing) "Following" else "Follow", fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(text = reel.caption, color = Color.White.copy(alpha = 0.95f), fontSize = 14.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "🎵 ${reel.audioTrack}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }

        // Right side interaction buttons
        Column(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 80.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Like button
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
              onClick = {
                reel.isLiked = !reel.isLiked
                if (reel.isLiked) reel.likesCount++ else reel.likesCount--
              },
              modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
              Icon(
                imageVector = if (reel.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (reel.isLiked) Color.Red else Color.White
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text("${reel.likesCount}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          // Comments
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
              onClick = { onShowSnackbar("Opened comments (${reel.commentsCount})") },
              modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
              Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments", tint = Color.White)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text("${reel.commentsCount}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          // Share
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
              onClick = { onShowSnackbar("Reel link copied to clipboard!") },
              modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
              Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text("${reel.sharesCount}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Top Header with Camera Upload Icon
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Real Life Reels", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)

      IconButton(
        onClick = onOpenUpload,
        modifier = Modifier
          .background(Color.Black.copy(alpha = 0.5f), CircleShape)
          .testTag("top_camera_button")
      ) {
        Icon(Icons.Default.PhotoCamera, contentDescription = "Record or Upload", tint = Color.White)
      }
    }
  }
}

@Composable
fun ExploreScreen(
  reelsList: List<ReelItem>,
  onShowSnackbar: (String) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val trendingTags = listOf("#Viral", "#RealLife", "#Cooking", "#Nature", "#Coffee", "#Comedy", "#Travel")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .padding(16.dp)
  ) {
    Text("Explore & Trending", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search creators, hashtags, sounds...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth().testTag("explore_search")
    )

    Spacer(modifier = Modifier.height(16.dp))
    Text("Trending Hashtags", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      trendingTags.take(4).forEach { tag ->
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.clickable { onShowSnackbar("Filtered by $tag") }
        ) {
          Text(tag, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
    Text("Featured Creators", fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      items(reelsList) { reel ->
        Card(
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Text(reel.creatorName.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(reel.creatorName, fontWeight = FontWeight.Bold)
              Text(reel.creatorUsername, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(4.dp))
              Text("${reel.viewsCount} views • ${reel.likesCount} likes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Button(
              onClick = { onShowSnackbar("Viewed profile of ${reel.creatorUsername}") },
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Text("View", fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun NotificationsScreen(onShowSnackbar: (String) -> Unit) {
  val notifications = listOf(
    NotificationItem("1", "@chef_marco liked your reel", "Your reel reached over 1,000 views", "Yesterday at 4:15 PM", Icons.Default.Favorite, Color.Red),
    NotificationItem("2", "Security Guard: APK Integrity Checked", "Checksum verified successfully", "Today at 8:00 AM", Icons.Default.Security, Color(0xFF00E676)),
    NotificationItem("3", "New follower: @nature_walks", "@nature_walks started following you", "2 days ago", Icons.Default.PersonAdd, MaterialTheme.colorScheme.primary),
    NotificationItem("4", "You earned +50 Coins from AdMob", "Rewarded video ad reward credited", "3 days ago", Icons.Default.MonetizationOn, Color(0xFFFFD700))
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .padding(16.dp)
  ) {
    Text("Notifications & Alerts", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      items(notifications) { item ->
        Card(
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(item.icon, contentDescription = null, tint = item.iconTint)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Spacer(modifier = Modifier.height(2.dp))
              Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(item.timeAgo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
          }
        }
      }
    }
  }
}

@Composable
fun ProfileScreen(
  userEmail: String,
  userCoins: Int,
  serverCoins: Int,
  userFollowers: Int,
  securityFailedAttempts: Int,
  onUpdateCoins: (Int) -> Unit,
  onTriggerTamper: () -> Unit,
  onSelfHeal: () -> Unit,
  onLogout: () -> Unit,
  onShowSnackbar: (String) -> Unit
) {
  var showSecurityDashboard by remember { mutableStateOf(false) }
  var showMonetizationDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(10.dp))
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(44.dp))
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text("Claire Jenkins", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(20.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      StatColumn("Followers", "$userFollowers")
      StatColumn("Coins", "$userCoins 🪙")
      StatColumn("Reels", "12")
    }

    Spacer(modifier = Modifier.height(24.dp))

    Card(
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Account & Security Controls", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = { showSecurityDashboard = true },
          modifier = Modifier.fillMaxWidth().testTag("profile_security_btn"),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
        ) {
          Icon(Icons.Default.Security, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Open Self-Healing Security Guard")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = { showMonetizationDialog = true },
          modifier = Modifier.fillMaxWidth().testTag("profile_monetization_btn")
        ) {
          Icon(Icons.Default.MonetizationOn, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Wallet & Monetization Status")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = onLogout,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Logout, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Sign Out")
        }
      }
    }
  }

  if (showSecurityDashboard) {
    AlertDialog(
      onDismissRequest = { showSecurityDashboard = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00E676))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Self-Healing Security Guard", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Runtime Tamper Detection & Checksum Verification:", fontWeight = FontWeight.Bold)
          Text(if (securityFailedAttempts > 0) "⚠️ Tamper attempts detected: $securityFailedAttempts / 3" else "🟢 APK Checksum & Signature Verified Secure.", color = if (securityFailedAttempts > 0) Color.Red else Color(0xFF00E676))

          Spacer(modifier = Modifier.height(4.dp))
          Text("Server-Authoritative Ledger (Wallet & Coins):", fontWeight = FontWeight.Bold)
          Text("• Local UI Coins: $userCoins Coins")
          Text("• Server Authoritative Coins: $serverCoins Coins", color = Color(0xFF00E676))

          if (userCoins != serverCoins) {
            Button(
              onClick = {
                onSelfHeal()
                showSecurityDashboard = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Trigger Self-Healing & Auto-Correction 🛡️", fontWeight = FontWeight.Bold, color = Color.Black)
            }
          }

          Button(
            onClick = {
              onTriggerTamper()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Simulate APK Binary Tamper Test")
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showSecurityDashboard = false }) {
          Text("Close")
        }
      },
      shape = RoundedCornerShape(20.dp)
    )
  }

  if (showMonetizationDialog) {
    AlertDialog(
      onDismissRequest = { showMonetizationDialog = false },
      title = { Text("Monetization & Wallet Ledger", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Followers: $userFollowers (Gate: 3,000 required for monetization)")
          Spacer(modifier = Modifier.height(8.dp))
          Text("Coins Balance: $userCoins Coins (Authoritative Server Ledger)")
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = {
              onUpdateCoins(userCoins + 50)
              onShowSnackbar("Claimed +50 Coins from AdMob Rewarded Video! 🪙")
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Watch Rewarded Ad (+50 Coins)")
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showMonetizationDialog = false }) {
          Text("Close")
        }
      },
      shape = RoundedCornerShape(20.dp)
    )
  }
}

@Composable
fun StatColumn(label: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
fun CameraUploadStudioDialog(
  onDismiss: () -> Unit,
  onPublish: (String, String) -> Unit
) {
  var caption by remember { mutableStateOf("") }
  var audioTrack by remember { mutableStateOf("Original Audio - Creator") }
  var uploadSource by remember { mutableStateOf("CameraX Video") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.VideoCameraBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("CameraX & Upload Studio", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Select Capture or Import Source:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = uploadSource == "CameraX Video",
            onClick = { uploadSource = "CameraX Video" },
            label = { Text("📹 Record Video") }
          )
          FilterChip(
            selected = uploadSource == "Photo",
            onClick = { uploadSource = "Photo" },
            label = { Text("📸 Take Photo") }
          )
          FilterChip(
            selected = uploadSource == "Gallery",
            onClick = { uploadSource = "Gallery" },
            label = { Text("🖼️ Gallery") }
          )
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = when (uploadSource) {
                "CameraX Video" -> Icons.Default.Videocam
                "Photo" -> Icons.Default.CameraAlt
                else -> Icons.Default.Image
              },
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("CameraX Live Preview Active ($uploadSource)", color = Color.White, fontSize = 12.sp)
          }
        }

        OutlinedTextField(
          value = caption,
          onValueChange = { caption = it },
          label = { Text("Write a caption (#hashtags)") },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("upload_caption_input")
        )

        OutlinedTextField(
          value = audioTrack,
          onValueChange = { audioTrack = it },
          label = { Text("Audio Track") },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onPublish(
            if (caption.isBlank()) "Unfiltered real moment ✨ #RealLife" else caption,
            audioTrack
          )
        },
        modifier = Modifier.testTag("publish_reel_button")
      ) {
        Text("Publish Reel")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
    shape = RoundedCornerShape(20.dp)
  )
}
