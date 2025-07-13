package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.model.Image;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.repository.LocationRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @CachePut(value = "locations", key = "#result.id")
    public Location createLocation(double latitude, double longitude, String address) {
        Location location = new Location(latitude, longitude, address);
        return locationRepository.save(location);
    }

    @Cacheable("locations")
    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @CacheEvict(value = "locations", key = "#locationId")
    public void deleteLocation(Long locationId) {
        locationRepository.deleteById(locationId);
        System.out.println("Location with ID " + locationId + " has been deleted and removed from the cache.");
    }

    @CacheEvict(value = "locations", allEntries = true)
    public void evictAllCaches() {
        System.out.println("All entries in the 'locations' cache have been cleared.");
        // CacheEvict ima logiku za sve ovo buk
        // Ovo CacheEvict treba iskoriti kod metoda za deletovanje npr.
    }
}
