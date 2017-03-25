package com.pservice.manas.busservice;

/**
 * Created by HP on 01-12-2016.
 */

public class Bus {

    String drivername,busno,stops,crowd;

    public Bus(String drivername, String busno, String stops, String crowd) {
        this.drivername = drivername;
        this.busno = busno;
        this.stops = stops;
        this.crowd = crowd;
    }

    public String getDrivername() {
        return drivername;
    }

    public void setDrivername(String drivername) {
        this.drivername = drivername;
    }

    public String getBusno() {
        return busno;
    }

    public void setBusno(String busno) {
        this.busno = busno;
    }

    public String getStops() {
        return stops;
    }

    public void setStops(String stops) {
        this.stops = stops;
    }

    public String getCrowd() {
        return crowd;
    }
}
