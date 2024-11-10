package com.srednal.spotwx;

public class Position {
  public final double latitude;
  public final double longitude;

  public Position(String latHeader, String lonHeader) throws NumberFormatException {
    latitude = Double.parseDouble(latHeader);
    longitude = Double.parseDouble(lonHeader);
  }

  @Override
  public String toString() {
    return "Lat=%f, Lon=%f => https://www.google.com/maps/?q=%f,%f".formatted(latitude, longitude, latitude, longitude);
  }
}
