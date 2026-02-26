import java.util.*;

public class ParkingLotSystem {

    private static final int TOTAL_SPOTS = 500;

    private enum Status { EMPTY, OCCUPIED, DELETED }

    private static class SpotEntry {
        String plate;
        long entryTime;
        Status status = Status.EMPTY;
        int probes; // number of probes needed to assign this spot
    }

    private SpotEntry[] table = new SpotEntry[TOTAL_SPOTS];

    // Statistics
    private int currentOccupied = 0;
    private double totalProbes = 0;
    private int totalParks = 0;
    private int[] hourlyOccupancy = new int[24];

    public ParkingLotSystem() {
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            table[i] = new SpotEntry();
        }
    }

    // Simple custom hash function for license plates
    private int hash(String plate) {
        return Math.abs(plate.hashCode()) % TOTAL_SPOTS;
    }

    /** Assign a parking spot using linear probing */
    public String parkVehicle(String plate) {
        int preferred = hash(plate);
        int probes = 0;

        for (int i = 0; i < TOTAL_SPOTS; i++) {
            int spot = (preferred + i) % TOTAL_SPOTS;

            if (table[spot].status == Status.EMPTY || table[spot].status == Status.DELETED) {
                table[spot].status = Status.OCCUPIED;
                table[spot].plate = plate;
                table[spot].entryTime = System.currentTimeMillis();
                table[spot].probes = probes;

                currentOccupied++;
                totalParks++;
                totalProbes += probes;

                updateHourlyStats();

                return "Assigned spot #" + spot + " (" + probes + " probes)";
            }

            probes++;
        }

        return "Parking Lot Full!";
    }

    /** Free spot and compute billing */
    public String exitVehicle(String plate) {
        int preferred = hash(plate);

        for (int i = 0; i < TOTAL_SPOTS; i++) {
            int spot = (preferred + i) % TOTAL_SPOTS;

            if (table[spot].status == Status.OCCUPIED && table[spot].plate.equals(plate)) {
                long durationMillis = System.currentTimeMillis() - table[spot].entryTime;

                table[spot].status = Status.DELETED;
                currentOccupied--;

                double hours = durationMillis / (1000.0 * 60 * 60);
                double fee = hours * 5.50;  // $5.50 per hour

                return "Spot #" + spot + " freed, Duration: " +
                        String.format("%.2f", hours) + "h, Fee: $" +
                        String.format("%.2f", fee);
            }
        }

        return "Vehicle not found.";
    }

    /** Finds nearest available spot based on entrance proximity */
    public int findNearestAvailableSpot() {
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            if (table[i].status == Status.EMPTY) return i;
        }
        return -1;
    }

    /** Updates hourly occupancy statistics */
    private void updateHourlyStats() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hourlyOccupancy[hour] += 1;
    }

    /** Retrieve parking lot statistics */
    public String getStatistics() {
        double occupancy = (currentOccupied / (double) TOTAL_SPOTS) * 100;

        int peakHour = 0;
        int max = 0;
        for (int i = 0; i < 24; i++) {
            if (hourlyOccupancy[i] > max) {
                max = hourlyOccupancy[i];
                peakHour = i;
            }
        }

        double avgProbes = (totalParks == 0) ? 0 : totalProbes / totalParks;

        return "Occupancy: " + String.format("%.1f", occupancy) + "%\n" +
               "Avg Probes: " + String.format("%.2f", avgProbes) + "\n" +
               "Peak Hour: " + peakHour + ":00–" + (peakHour + 1) + ":00";
    }
}
