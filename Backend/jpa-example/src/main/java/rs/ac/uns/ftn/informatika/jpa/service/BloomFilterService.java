package rs.ac.uns.ftn.informatika.jpa.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class BloomFilterService {
    private final UserRepository userRepository;
    private BloomFilter<String> bloomFilter;

    @Autowired
    public BloomFilterService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        List<String> allUsernames = userRepository.findAllUsernames();
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), allUsernames.size() + 1000);
        for (String username : allUsernames) {
            bloomFilter.put(username);
        }
    }

    public boolean maybeUsernameExists(String username) {
        return bloomFilter.mightContain(username);
    }

    public void addUsername(String username) {
        bloomFilter.put(username);
    }
}