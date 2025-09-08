package rs.ac.uns.ftn.informatika.jpa.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("locations", "topPostsAllTime", "topPostsLastWeek");
        // pozdrav za kolegu Miloslava koji je ovo dodao i nije mi rekao i onda mi nije radilo kreiranje posta
    }
}
