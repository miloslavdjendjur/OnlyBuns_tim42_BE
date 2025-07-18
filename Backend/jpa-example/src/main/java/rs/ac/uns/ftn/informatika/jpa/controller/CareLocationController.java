package rs.ac.uns.ftn.informatika.jpa.controller;

import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.dto.CareLocationDTO;
import rs.ac.uns.ftn.informatika.jpa.model.CareLocation;
import rs.ac.uns.ftn.informatika.jpa.repository.CareLocationRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/care-locations")
@CrossOrigin
public class CareLocationController {

    private final CareLocationRepository repository;

    public CareLocationController(CareLocationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<CareLocationDTO> getAll() {
        return repository.findAll().stream()
                .map(cl -> new CareLocationDTO(cl.getId(), cl.getNaziv(), cl.getLokacija()))
                .collect(Collectors.toList());
    }
}
