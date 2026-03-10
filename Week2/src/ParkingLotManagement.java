import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class ParkingLotManagement {

    private static class Vehicle {
        String licensePlate;
        LocalDateTime entryTime;

        Vehicle(String licensePlate) {
            this.licensePlate = licensePlate;
            this.entryTime = LocalDateTime.now();
        }
    }

    private static final int CAPACITY = 500;
    private Vehicle[] spots = new Vehicle[CAPACITY];
    private int totalProbes = 0;
    private int vehiclesParked = 0;

    // Hash function: licensePlate → preferred spot
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % CAPACITY;
    }

    // Park vehicle
    public synchronized String parkVehicle(String licensePlate) {

        int preferredSpot = hash(licensePlate);
        int spot = preferredSpot;
        int probes = 0;

        while (spots[spot] != null) {
            probes++;
            spot = (spot + 1) % CAPACITY;
            if (spot == preferredSpot) {
                return "Parking Full!";
            }
        }

        spots[spot] = new Vehicle(licensePlate);
        totalProbes += probes;
        vehiclesParked++;

        return "Assigned spot #" + spot + " (" + probes + " probes)";
    }

    // Exit vehicle
    public synchronized String exitVehicle(String licensePlate) {

        int preferredSpot = hash(licensePlate);
        int spot = preferredSpot;
        int probes = 0;

        while (spots[spot] != null) {
            if (spots[spot].licensePlate.equals(licensePlate)) {
                Vehicle v = spots[spot];
                spots[spot] = null;
                vehiclesParked--;

                Duration duration = Duration.between(v.entryTime, LocalDateTime.now());
                double hours = duration.toMinutes() / 60.0;
                double fee = Math.ceil(hours * 5); // $5 per hour

                return "Spot #" + spot + " freed, Duration: " +
                        String.format("%.2f", hours) + "h, Fee: $" + String.format("%.2f", fee);
            }

            probes++;
            spot = (spot + 1) % CAPACITY;
            if (spot == preferredSpot) break;
        }

        return "Vehicle not found!";
    }

    // Get parking statistics
    public synchronized void getStatistics() {

        double occupancy = vehiclesParked * 100.0 / CAPACITY;
        double avgProbes = vehiclesParked == 0 ? 0 : totalProbes * 1.0 / vehiclesParked;

        System.out.println("Occupancy: " + String.format("%.2f", occupancy) + "%");
        System.out.println("Avg Probes: " + String.format("%.2f", avgProbes));
    }

    public static void main(String[] args) throws InterruptedException {

        ParkingLotManagement parkingLot = new ParkingLotManagement();

        System.out.println(parkingLot.parkVehicle("ABC-1234"));
        System.out.println(parkingLot.parkVehicle("ABC-1235"));
        System.out.println(parkingLot.parkVehicle("XYZ-9999"));

        Thread.sleep(1000); // Simulate some time passing

        System.out.println(parkingLot.exitVehicle("ABC-1234"));
        System.out.println(parkingLot.exitVehicle("XYZ-9999"));

        parkingLot.getStatistics();
    }
}