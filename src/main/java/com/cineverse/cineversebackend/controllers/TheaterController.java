package com.cineverse.cineversebackend.controllers;

import com.cineverse.cineversebackend.models.Theater;
import com.cineverse.cineversebackend.repositories.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin
public class TheaterController {

    @Autowired
    private TheaterRepository theaterRepository;

    // --- PUBLIC ENDPOINTS ---

    @GetMapping("/api/theaters")
    public List<Theater> getAllTheaters(@RequestParam(value = "city", required = false) String city) {
        if (city != null && !city.trim().isEmpty()) {
            return theaterRepository.findByCityIgnoreCase(city);
        }
        return theaterRepository.findAll();
    }

    @GetMapping("/api/theaters/{id}")
    public ResponseEntity<?> getTheaterById(@PathVariable("id") String id) {
        Optional<Theater> theaterOpt = theaterRepository.findById(id);
        if (theaterOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(theaterOpt.get());
    }

    // --- ADMIN ENDPOINTS (Secured under /api/admin/**) ---

    @PostMapping("/api/admin/theaters")
    public ResponseEntity<?> createTheater(@RequestBody Theater theater) {
        if (theater.getName() == null || theater.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Theater name is required"));
        }
        
        // Ensure screen IDs are generated
        if (theater.getScreens() != null) {
            for (Theater.Screen screen : theater.getScreens()) {
                if (screen.getId() == null || screen.getId().isEmpty()) {
                    screen.setId(UUID.randomUUID().toString());
                }
            }
        }
        
        Theater savedTheater = theaterRepository.save(theater);
        return ResponseEntity.ok(savedTheater);
    }

    @PutMapping("/api/admin/theaters/{id}")
    public ResponseEntity<?> updateTheater(@PathVariable("id") String id, @RequestBody Theater theaterDetails) {
        Optional<Theater> theaterOpt = theaterRepository.findById(id);
        if (theaterOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Theater theater = theaterOpt.get();
        theater.setName(theaterDetails.getName());
        theater.setCity(theaterDetails.getCity());
        theater.setAddress(theaterDetails.getAddress());
        
        if (theaterDetails.getScreens() != null) {
            for (Theater.Screen screen : theaterDetails.getScreens()) {
                if (screen.getId() == null || screen.getId().isEmpty()) {
                    screen.setId(UUID.randomUUID().toString());
                }
            }
            theater.setScreens(theaterDetails.getScreens());
        }

        Theater updatedTheater = theaterRepository.save(theater);
        return ResponseEntity.ok(updatedTheater);
    }

    @DeleteMapping("/api/admin/theaters/{id}")
    public ResponseEntity<?> deleteTheater(@PathVariable("id") String id) {
        if (!theaterRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        theaterRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Theater deleted successfully"));
    }
}
