package rs.ac.uns.ftn.informatika.jpa.metrics;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ActiveUserTrackingFilter extends OncePerRequestFilter {

    private final ActiveUsersMeter activeUsersMeter;

    public ActiveUserTrackingFilter(ActiveUsersMeter activeUsersMeter) {
        this.activeUsersMeter = activeUsersMeter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            String idOrUsername = null;

            if (principal instanceof UserDetails) {
                idOrUsername = ((UserDetails) principal).getUsername();
            } else {
                idOrUsername = String.valueOf(principal);
            }

            if (idOrUsername != null && !"anonymousUser".equals(idOrUsername)) {
                activeUsersMeter.markActive(idOrUsername);
            }
        }

        filterChain.doFilter(request, response);
    }
}
