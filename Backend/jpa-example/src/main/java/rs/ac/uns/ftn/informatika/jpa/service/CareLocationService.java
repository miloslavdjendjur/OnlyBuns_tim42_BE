package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.model.CareLocation;
import rs.ac.uns.ftn.informatika.jpa.repository.CareLocationRepository;

import java.util.List;

@Service
public class CareLocationService {

    private final CareLocationRepository repository;

    public CareLocationService(CareLocationRepository repository) {
        this.repository = repository;
    }

    public void save(CareLocation location) {
        repository.save(location);
    }

    public List<CareLocation> findAll() {
        return repository.findAll();
    }
}

