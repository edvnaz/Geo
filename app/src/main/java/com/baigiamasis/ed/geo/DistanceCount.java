package com.baigiamasis.ed.geo;

import android.location.Location;

import java.text.DecimalFormat;

class DistanceCount {

    private float distance = 0;
    private double latitude, longitude;
    private Location crntLocation;
    private Location newLocation;

    DistanceCount(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        setPointLocation(this.latitude, this.longitude);
    }

    private void setPointLocation(double latitude, double longitude) {
        newLocation = new Location("newlocation");
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
    }

    private void setCurrentLocation(double currentLatitude, double currentLongitude) {
        crntLocation = new Location("crntlocation");
        crntLocation.setLatitude(currentLatitude);
        crntLocation.setLongitude(currentLongitude);
    }


    private String countDistance() {
        distance = crntLocation.distanceTo(newLocation);  //in meters
        DecimalFormat f = new DecimalFormat("##.0");
        return String.valueOf(f.format(distance / 1000));
//        distance = crntLocation.distanceTo(newLocation) / 1000; // in km
    }

    String returnDistance(double currentLatitude, double currentLongitude) {
        setCurrentLocation(currentLatitude, currentLongitude);

        return countDistance();
    }


}
