package org.openwes.station.infrastructure.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter(filterName = "HttpStationFilter", urlPatterns = "/*")
@Slf4j
public class HttpStationFilter implements Filter {

    @Override
    public void destroy() { // nothing to do
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String stationCode = request.getParameter(HttpStationContext.STATION_CODE);

        if (StringUtils.isBlank(stationCode)) {
            stationCode = ((HttpServletRequest) request).getHeader(HttpStationContext.STATION_CODE);
        }

        if (StringUtils.isNotBlank(stationCode) && !StringUtils.equalsIgnoreCase("null", stationCode.trim())) {
            HttpStationContext.setWorkStationId(Long.parseLong(stationCode));
        }

        try {
            chain.doFilter(request, response);
        } finally {
            HttpStationContext.removeStationCode();
        }
    }

    public static void main(String[] args) {
        System.out.println(StringUtils.isNotBlank(null));
    }
}
