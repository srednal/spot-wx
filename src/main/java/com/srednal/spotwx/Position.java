package com.srednal.spotwx;

public class Position {
  public final double latitude;
  public final double longitude;

  public Position(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public Position(String latHeader, String lonHeader) throws NumberFormatException {
    latitude = Double.parseDouble(latHeader);
    longitude = Double.parseDouble(lonHeader);
  }

  @Override
  public String toString() {
    return "Lat=%s, Lon=%s".formatted(latitude, longitude);
  }
}
