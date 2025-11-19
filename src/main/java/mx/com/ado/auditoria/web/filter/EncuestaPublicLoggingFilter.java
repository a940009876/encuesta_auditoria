package mx.com.ado.auditoria.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter to log all requests to the public encuesta endpoints.
 */
public class EncuestaPublicLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(EncuestaPublicLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String path = request.getRequestURI();
        
        if (path != null && path.startsWith("/api/encuesta")) {
            LOG.info("=== FILTRO: Petición recibida ===");
            LOG.info("Método: {}", request.getMethod());
            LOG.info("URI: {}", path);
            LOG.info("Query String: {}", request.getQueryString());
            LOG.info("Remote Address: {}", request.getRemoteAddr());
            LOG.info("Headers - Authorization: {}", request.getHeader("Authorization"));
            LOG.info("Headers - X-XSRF-TOKEN: {}", request.getHeader("X-XSRF-TOKEN"));
        }
        
        filterChain.doFilter(request, response);
        
        if (path != null && path.startsWith("/api/encuesta")) {
            LOG.info("=== FILTRO: Respuesta enviada ===");
            LOG.info("Status: {}", response.getStatus());
        }
    }
}

