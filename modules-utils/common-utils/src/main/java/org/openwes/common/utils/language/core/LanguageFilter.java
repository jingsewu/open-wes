package org.openwes.common.utils.language.core;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter(filterName = "languageFilter", urlPatterns = "/*")
@Slf4j
public class LanguageFilter implements Filter {

    public static final String LANG = "Locale";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        LanguageContext.setLanguage(httpRequest.getHeader(LANG));
        try {
            chain.doFilter(request, response);
        } finally {
            LanguageContext.remove();
        }
    }
}
