import java.util.*;

public class DispatchCenter {
    public static final int NUM_OF_TAXIS = 50;
    public static String[]  AREA_NAMES = {"Downtown", "Airport", "North", "South", "East", "West"};

    private HashMap<Integer, Taxi>              taxis;
    private HashMap<String, ArrayList<Taxi>>    areas;
    private int[][]                             stats;

    // Constructor
    public DispatchCenter() {
        taxis = new HashMap<Integer, Taxi>();
        areas = new HashMap<String, ArrayList<Taxi>>();
        stats = new int[AREA_NAMES.length][AREA_NAMES.length];

        // Initialize 50 taxis with random plates and areas
        for (int i = 0; i < NUM_OF_TAXIS; i++)
            addTaxi(new Taxi(100 + (int)(Math.random() * 900)), AREA_NAMES[(int)(Math.random() * 6)]);
    }

    public int[][] getStats() { return stats; }
    public HashMap<String, ArrayList<Taxi>> getAreas() { return areas; }

    // Update the statistics for a taxi going from the pickup location to the drop-off location
    public void updateStats(String pickup, String dropOff) {
        for (int d = 0; d < AREA_NAMES.length; d++) {
            for (int p = 0; p < AREA_NAMES.length; p++) {
                if (dropOff.equals(AREA_NAMES[d]) && pickup.equals(AREA_NAMES[p]))
                    stats[d][p]++;
            }
        }
    }

    // Determine the travel times from one area to another
    public static int computeTravelTimeFrom(String pickup, String dropOff) {
        int[][] travelTimes = {
                {10, 40, 20, 20, 20, 20},
                {40, 10, 40, 40, 20, 60},
                {20, 40, 10, 40, 20, 20},
                {20, 40, 40, 10, 20, 20},
                {20, 20, 20, 20, 10, 40},
                {20, 60, 20, 20, 40, 10}};

        for (int d = 0; d < AREA_NAMES.length; d++) {
            for (int p = 0; p < AREA_NAMES.length; p++) {
                if (dropOff.equals(AREA_NAMES[d]) && pickup.equals(AREA_NAMES[p]))
                    return travelTimes[d][p];
            }
        }
        return 0;
    }

    // Add a taxi to the HashMaps
    public void addTaxi(Taxi aTaxi, String area) {
        taxis.put(aTaxi.getPlateNumber(), aTaxi);
        if (!areas.containsKey(area))
            areas.put(area, new ArrayList<Taxi>());
        areas.get(area).add(aTaxi);
    }

    // Return a list of all available taxis within a certain area
    private ArrayList<Taxi> availableTaxisInArea(String s) {
        ArrayList<Taxi> result = new ArrayList<Taxi>();
        for (Taxi taxi: areas.get(s)) {
            if (taxi.isAvailable())
                result.add(taxi);
        }
        return result;
    }

    // Return a list of all busy taxis
    public ArrayList<Taxi> getBusyTaxis() {
        ArrayList<Taxi> result = new ArrayList<Taxi>();
        Set<String> allAreas = areas.keySet();
        for (String area: allAreas) {
            for (Taxi taxi: areas.get(area)) {
                if (!taxi.isAvailable())
                    result.add(taxi);
            }
        }
        return result;
    }

    // Find a taxi to satisfy the given request
    public Taxi sendTaxiForRequest(ClientRequest request) {
        // Check if there is a taxi available in the requested pickup area
        if (!availableTaxisInArea(request.getPickupLocation()).isEmpty()) {
            Taxi taxi = availableTaxisInArea(request.getPickupLocation()).get(0);
            areas.get(request.getPickupLocation()).remove(taxi);
            areas.get(request.getDropoffLocation()).add(taxi);
            taxi.setDestination(request.getDropoffLocation());
            taxi.setAvailable(false);
            taxi.setEstimatedTimeToDest(computeTravelTimeFrom(request.getPickupLocation(), request.getDropoffLocation()));
            updateStats(request.getPickupLocation(), request.getDropoffLocation());
            return taxi;
        }
        else {
            if (getBusyTaxis().size() != NUM_OF_TAXIS) {
                Set<String> allAreas = areas.keySet();
                for (String area: allAreas) {
                    if (!area.equals(request.getPickupLocation())) {
                        for (Taxi taxi: areas.get(area)) {
                            if (taxi.isAvailable()) {
                                areas.get(area).remove(taxi);
                                areas.get(request.getDropoffLocation()).add(taxi);
                                taxi.setDestination(request.getDropoffLocation());
                                taxi.setAvailable(false);
                                taxi.setEstimatedTimeToDest(computeTravelTimeFrom(area, request.getPickupLocation()) +
                                        computeTravelTimeFrom(request.getPickupLocation(), request.getDropoffLocation()));
                                updateStats(request.getPickupLocation(), request.getDropoffLocation());
                                return taxi;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
