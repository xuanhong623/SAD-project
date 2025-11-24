package com.example.flightrescue.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flightrescue.database.DataBase;
import com.example.flightrescue.model.FlightProfileRequest;
import com.example.flightrescue.model.User;
import com.example.flightrescue.storage.InMemoryData;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    @PostMapping("/{username}/flight-profile")
    public ResponseEntity<User> saveFlightProfile(
            @PathVariable String username,
            @RequestBody FlightProfileRequest req) {

        // Optional<User> userOpt = InMemoryData.users.stream()
        //         .filter(u -> u.getUsername().equals(username))
        //         .findFirst();

        DataBase db = new DataBase();
        try{
            User user = new User(username, req.getFullName(), req.getHotelName(), req.getHotelAddress(), req.getFlightId());
            db.InputUserData(user, DataBase.db);
            return ResponseEntity.ok(user);

        }catch(Exception e){
            e.printStackTrace();
        }

        // if (!userOpt.isPresent()) {
        //     return ResponseEntity.notFound().build();
        // }



        // User user = userOpt.get();
        // user.setFullName(req.getFullName());
        // user.setHotelName(req.getHotelName());
        // user.setHotelAddress(req.getHotelAddress());
        // user.setFlightId(req.getFlightId());
        // user.setProfileCompleted(true);
        return null;

    }
}
