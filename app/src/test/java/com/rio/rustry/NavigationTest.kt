package com.rio.rustry

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.rio.rustry.navigation.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Navigation integration tests
 * Tests the complete navigation flow with Hilt integration
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testNavigationRouteCreation() {
        // Test marketplace route creation
        val marketplaceRoute = NavigationRoute.Marketplace.createRoute(
            breed = "Rhode Island Red",
            location = "Test Farm",
            search = "healthy"
        )
        
        assertThat(marketplaceRoute).contains("breed=Rhode Island Red")
        assertThat(marketplaceRoute).contains("location=Test Farm")
        assertThat(marketplaceRoute).contains("search=healthy")
    }

    @Test
    fun testFowlDetailRouteCreation() {
        val fowlId = "test-fowl-123"
        val route = NavigationRoute.FowlDetail.createRoute(fowlId)
        
        assertThat(route).isEqualTo("fowl_detail/$fowlId")
    }

    @Test
    fun testDeepLinkParsing() {
        // Test marketplace deep link parsing
        val marketplaceUri = "rustry://marketplace?breed=Leghorn&location=Farm1"
        val marketplaceDeepLink = DeepLinkHandler.parseMarketplaceDeepLink(marketplaceUri)
        
        assertThat(marketplaceDeepLink).isNotNull()
        assertThat(marketplaceDeepLink?.breed).isEqualTo("Leghorn")
        assertThat(marketplaceDeepLink?.location).isEqualTo("Farm1")
    }

    @Test
    fun testFowlDeepLinkParsing() {
        val fowlUri = "rustry://fowl/test-fowl-456"
        val fowlId = DeepLinkHandler.parseFowlDeepLink(fowlUri)
        
        assertThat(fowlId).isEqualTo("test-fowl-456")
    }

    @Test
    fun testNavigationValidator() {
        // Test fowl ID validation
        assertThat(NavigationValidator.validateFowlId("valid-fowl-id")).isTrue()
        assertThat(NavigationValidator.validateFowlId("")).isFalse()
        assertThat(NavigationValidator.validateFowlId(null)).isFalse()
        assertThat(NavigationValidator.validateFowlId("ab")).isFalse() // Too short
        
        // Test search query validation
        assertThat(NavigationValidator.validateSearchQuery("chicken")).isTrue()
        assertThat(NavigationValidator.validateSearchQuery("")).isFalse()
        assertThat(NavigationValidator.validateSearchQuery("   ")).isFalse()
        assertThat(NavigationValidator.validateSearchQuery(null)).isFalse()
    }

    @Test
    fun testShareableLinkCreation() {
        // Test marketplace shareable link
        val marketplaceLink = DeepLinkHandler.createShareableMarketplaceLink(
            breed = "Rhode Island Red",
            search = "healthy chickens"
        )
        
        assertThat(marketplaceLink).startsWith("https://rustry.app/marketplace")
        assertThat(marketplaceLink).contains("breed=")
        assertThat(marketplaceLink).contains("search=")
        
        // Test fowl shareable link
        val fowlLink = DeepLinkHandler.createShareableFowlLink("test-fowl-789")
        assertThat(fowlLink).isEqualTo("https://rustry.app/fowl/test-fowl-789")
    }

    @Test
    fun testNavigationConstants() {
        // Verify navigation constants are properly defined
        assertThat(NavigationConstants.ANIMATION_DURATION).isEqualTo(300)
        assertThat(NavigationConstants.DEEP_LINK_SCHEME).isEqualTo("rustry")
        assertThat(NavigationConstants.WEB_LINK_DOMAIN).isEqualTo("rustry.app")
    }

    @Test
    fun testMarketplaceDeepLinkWithAllParameters() {
        val uri = "rustry://marketplace?breed=Leghorn&location=Farm1&search=healthy&minPrice=100&maxPrice=500"
        val deepLink = DeepLinkHandler.parseMarketplaceDeepLink(uri)
        
        assertThat(deepLink).isNotNull()
        assertThat(deepLink?.breed).isEqualTo("Leghorn")
        assertThat(deepLink?.location).isEqualTo("Farm1")
        assertThat(deepLink?.search).isEqualTo("healthy")
        assertThat(deepLink?.minPrice).isEqualTo(100.0)
        assertThat(deepLink?.maxPrice).isEqualTo(500.0)
    }

    @Test
    fun testInvalidDeepLinkHandling() {
        // Test invalid marketplace deep link
        val invalidUri = "invalid://not-a-valid-uri"
        val marketplaceDeepLink = DeepLinkHandler.parseMarketplaceDeepLink(invalidUri)
        assertThat(marketplaceDeepLink).isNull()
        
        // Test invalid fowl deep link
        val fowlId = DeepLinkHandler.parseFowlDeepLink(invalidUri)
        assertThat(fowlId).isNull()
    }

    @Test
    fun testRouteSanitization() {
        val dirtyRoute = "  MARKETPLACE/TEST  "
        val cleanRoute = NavigationValidator.sanitizeRoute(dirtyRoute)
        
        assertThat(cleanRoute).isEqualTo("marketplace/test")
    }

    @Test
    fun testWebDeepLinkParsing() {
        // Test web-based deep links
        val webUri = "https://rustry.app/marketplace?breed=Silkie&location=Organic%20Farm"
        val deepLink = DeepLinkHandler.parseMarketplaceDeepLink(webUri)
        
        assertThat(deepLink).isNotNull()
        assertThat(deepLink?.breed).isEqualTo("Silkie")
        assertThat(deepLink?.location).isEqualTo("Organic Farm") // URL decoded
    }

    @Test
    fun testEmptyMarketplaceRoute() {
        val emptyRoute = NavigationRoute.Marketplace.createRoute()
        assertThat(emptyRoute).isEqualTo("marketplace")
    }

    @Test
    fun testChatRouteCreation() {
        val userId = "user-123"
        val chatRoute = NavigationRoute.Chat.createRoute(userId)
        assertThat(chatRoute).isEqualTo("chat/$userId")
    }

    @Test
    fun testSearchRouteCreation() {
        val query = "healthy chickens"
        val searchRoute = NavigationRoute.Search.createRoute(query)
        assertThat(searchRoute).isEqualTo("search?q=$query")
    }
}