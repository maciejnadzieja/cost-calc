package com.costcalc.api.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.web.servlet.i18n.SessionLocaleResolver

class LocaleConfigTest {
    
    @Test
    fun `should resolve Polish locale from Accept-Language header when no session locale`() {
        // given
        val request = mock(HttpServletRequest::class.java)
        val session = mock(HttpSession::class.java)
        `when`(request.session).thenReturn(session)
        `when`(request.getHeader("Accept-Language")).thenReturn("pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7,uk;q=0.6")
        
        val resolver = LocaleConfig.SmartLocaleResolver()
        resolver.setDefaultLocale(Locale.ENGLISH)
        
        // when
        val resolvedLocale = resolver.resolveLocale(request)
        
        // then
        assertEquals("pl", resolvedLocale.language)
    }
    
    @Test
    fun `should use session locale if present`() {
        // given
        val request = mock(HttpServletRequest::class.java)
        val session = mock(HttpSession::class.java)
        `when`(request.session).thenReturn(session)
        `when`(request.getHeader("Accept-Language")).thenReturn("pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7,uk;q=0.6")
        
        val resolver = LocaleConfig.SmartLocaleResolver()
        resolver.setDefaultLocale(Locale.ENGLISH)
        
        // Mock session attribute for locale
        `when`(session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME))
            .thenReturn(Locale.ENGLISH)
        
        // when
        val resolvedLocale = resolver.resolveLocale(request)
        
        // then
        assertEquals("en", resolvedLocale.language)
    }
    
    @Test
    fun `should fallback to default locale for unsupported language`() {
        // given
        val request = mock(HttpServletRequest::class.java)
        val session = mock(HttpSession::class.java)
        `when`(request.session).thenReturn(session)
        `when`(request.getHeader("Accept-Language")).thenReturn("uk-UA,uk;q=0.9")
        
        val resolver = LocaleConfig.SmartLocaleResolver()
        resolver.setDefaultLocale(Locale.ENGLISH)
        
        // when
        val resolvedLocale = resolver.resolveLocale(request)
        
        // then
        assertEquals("en", resolvedLocale.language)
    }
} 