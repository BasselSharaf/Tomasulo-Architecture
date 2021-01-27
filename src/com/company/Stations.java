package com.company;

public class Stations {
    int size;
    ReservationStation[] stations;
    String name;
    public Stations(int s, int startTag, String name) {
        size = s;
        stations = new ReservationStation[s];
        for(int i = 0; i < s; i++)
            stations[i] = new ReservationStation(startTag++);
        this.name = name;
    }

    public ReservationStation get(int idx) {
        return stations[idx];
    }

    public void print_stations() {
    	System.out.printf("%s Stations :%n", name);
    	for(int i = 0; i < size; i++)
    		stations[i].print_station();
    }
}
