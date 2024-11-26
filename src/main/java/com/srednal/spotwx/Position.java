package com.srednal.spotwx;

public class Position {

  public final double latitude;
  public final double longitude;

  public Position(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  @Override
  public String toString() {
    return "Lat=%1$f, Lon=%2$f => https://www.google.com/maps/?q=%1$f,%2$f".formatted(latitude, longitude);
  }
}