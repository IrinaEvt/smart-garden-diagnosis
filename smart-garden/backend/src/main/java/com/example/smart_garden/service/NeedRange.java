package com.example.smart_garden.service;

public class NeedRange {
    private double min;
    private double max;

    public NeedRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean isWithin(double value) {
        return value >= min && value <= max;
    }

    @Override
    public String toString() {
        return String.format("%.1f - %.1f", min, max);
    }

    public NeedRange interpretRange(String level, String parameter) {
        switch (parameter) {
            case "temperature":
                return "high".equalsIgnoreCase(level)
                        ? new NeedRange(30.0, 40.0)
                        : new NeedRange(2.0, 20.0);
            case "light":
                return "high".equalsIgnoreCase(level)
                        ? new NeedRange(700, 1000)
                        : new NeedRange(200, 400);
            case "humidity":
                return "high".equalsIgnoreCase(level)
                        ? new NeedRange(60, 80)
                        : new NeedRange(20, 30);
            case "soilMoisture":
                return "high".equalsIgnoreCase(level)
                        ? new NeedRange(70, 100)
                        : new NeedRange(30, 69);
            default:
                return new NeedRange(0, 0);
        }


    }

    public boolean isInRange(Double value) {
        return value != null && value >= min && value <= max;
    }

    // гетъри
    public double getMin() { return min; }
    public double getMax() { return max; }
}

