package rs.ac.uns.ftn.informatika.jpa.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {
    private final Map<String, List<Instant>> ipRequestLog = new HashMap<>();
    private final int MAX_REQUESTS_PER_MINUTE = 5;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getRequestURI().contains("/auth/login")) {
            return true;
        }

        String ip = request.getRemoteAddr();
        Instant now = Instant.now();

        synchronized (ipRequestLog) {
            ipRequestLog.putIfAbsent(ip, new LinkedList<>());
            List<Instant> timestamps = ipRequestLog.get(ip);
            timestamps.removeIf(ts -> ts.isBefore(now.minusSeconds(60)));

            if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(429);
                response.getWriter().write("Too many login attempts. Try again later.");
                return false;
            }
            timestamps.add(now);
        }
        return true;
    }
}
