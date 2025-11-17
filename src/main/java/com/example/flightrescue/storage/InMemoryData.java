package com.example.flightrescue.storage;

import java.util.ArrayList;
import java.util.List;

import com.example.flightrescue.model.Flight;
import com.example.flightrescue.model.Plan;
import com.example.flightrescue.model.User;

public class InMemoryData {

    public static List<Flight> flights = new ArrayList<>();
    public static List<Plan> plans = new ArrayList<>();
    public static final List<User> users = new ArrayList<>();

}
