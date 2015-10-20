package daniel.stadtgeschichten.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Pair;

import java.util.ArrayList;

import daniel.stadtgeschichten.model.Circle;
import daniel.stadtgeschichten.model.Spot;

/**
 * This adapter holds methods for keeping track of the location.
 */
public class LocationHelper
{
    /**
     * Radius of the earth
     */
    private static final double EARTH_RADIUS = 6371.0; // kilometers

    /**
     * Minimum time interval between location updates, in milliseconds
     */
    private static final long LOCATION_UPDATE_TIME_INTERVAL = 500;

    /**
     * Minimum number of measurements to do before comparing locations
     */
    private static final int MINIMUM_MEASUREMENTS = 5;

    /**
     * Log tag
     */
    private static final String LOG_TAG = "LocationHelper";

    /**
     * Logger
     */
    private Logger logger;

    /**
     * Location manager used to fetch the current location
     */
    private LocationManager locationManager;

    /**
     * Location listener that is registered in the location manager
     */
    private LocationListener locationListener;

    /**
     * Current number of measurements
     */
    private long currentMeasurements;

    /**
     * List of location listener
     */
    private final ArrayList<OnLocationListener> listeners = new ArrayList<>();

    /**
     * Listener interface to implement by callee
     */
    public interface OnLocationListener
    {
        void onLocationArrived(Circle circle);
        void onFirstLocation();
    }

    /**
     * Possible spots
     */
    private Spot[] spots = new Spot[0];

    /**
     * Last received location
     */
    private Location lastLocation;

    /**
     * Initialize a LocationHelper object.
     * @param context application's context
     * @param listener listener to add
     */
    public LocationHelper(final Context context, final OnLocationListener listener)
    {
        addListener(listener);

        logger = Logger.getLogger(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                logger.d(LOG_TAG, "Current location: latitude: " + location.getLatitude() +
                        ", longitude: " + location.getLongitude() + ", accuracy: " +
                        location.getAccuracy());

                lastLocation = location;

                currentMeasurements++;
                if (currentMeasurements >= MINIMUM_MEASUREMENTS)
                {
                    // Inform listeners about first location that is taken into account.
                    if (currentMeasurements == MINIMUM_MEASUREMENTS)
                        for (OnLocationListener l: listeners)
                            l.onFirstLocation();

                    // Inform the listeners about the circle that matches the current location.
                    Circle circle = getMatchedCircle(location);
                    if (circle != null)
                        for (OnLocationListener l : listeners)
                            l.onLocationArrived(circle);
                }
                else
                    logger.d(LOG_TAG, "Warming up: Ignored last location.");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                // TODO
            }

            @Override
            public void onProviderEnabled(String provider)
            {
                // Do nothing special.
            }

            @Override
            public void onProviderDisabled(String provider)
            {
                // TODO
            }
        };
    }

    /**
     * Get the smallest circle that contains the provided location.
     * @param location location
     * @return matched circle, null if there has been no match
     */
    private Circle getMatchedCircle(Location location)
    {
        Circle result = null;
        for (Spot spot : spots)
        {
            for (Circle circle : spot.getCircles())
            {
                // As the circles are ordered, the smallest one will always be taken.
                if (hasLocationInRadius(location, spot.getLatitude(), spot.getLongitude(),
                        circle.getRadius() / 1000.0, circle.getTitle())
                        && (result == null || circle.getRadius() <= result.getRadius()))
                    // Check if there is an smaller circle of one spot that may be overlapped by a
                    // bigger circle of another spot.
                {
                    result = circle;
                    break;
                }
            }
        }
        if (result == null)
            logger.d(LOG_TAG, "Picked: none");
        else
            logger.d(LOG_TAG, "Picked: circle with title " + result.getTitle() + ", radius " +
                    result.getRadius());
        return result;
    }

    /**
     * Set the possible spots.
     * @param spots possible spots
     */
    public void setSpots(Spot[] spots)
    {
        this.spots = spots;
    }

    /**
     * Start logging by registering the location listener in the location manager.
     */
    public void startLogging()
    {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_TIME_INTERVAL, 0, locationListener);
    }

    /**
     * Stop logging by removing the location listener from the location manager.
     */
    public void stopLogging()
    {
        locationManager.removeUpdates(locationListener);
    }

    /**
     * Check whether the given location is strictly in the circle with a center defined by the given
     * latitude and longitude as as well as radius.
     * @param location location
     * @param latitude latitude of the circle's center
     * @param longitude longitude of the circle's center
     * @param radius radius of the circle, in kilometers
     * @return true if that is the case, false otherwise
     */
    private boolean hasLocationInRadius(Location location, double latitude, double longitude,
                                        double radius, String title)
    {
        double distance = distFrom(location.getLatitude(), location.getLongitude(), latitude,
                longitude);

        logger.d(LOG_TAG, "Distance to " + title + " with radius " + radius + "km: " + distance +
                "km");
        return distance < radius;
    }

    /**
     * Calculate the distance between two given locations using the Haversine formula.
     * @param lat1 latitude of first location
     * @param lng1 longitude of firs location
     * @param lat2 latitude of second location
     * @param lng2 longitude of second location
     * @return distance in kilometers
     */
    private static double distFrom(double lat1, double lng1, double lat2, double lng2)
    {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /**
     * Get the currently closest spot.
     * @return pair of doubles: latitude and longitude of the closest spot; 360.0 twice if no
     * closest spot is found
     */
    public Pair<Double, Double> getClosestSpot()
    {
        Spot nearestSpot = null;
        double minDistance = Double.MAX_VALUE;
        for (Spot spot : spots)
        {
            double currentDistance = distFrom(spot.getLatitude(), spot.getLongitude(),
                    lastLocation.getLatitude(), lastLocation.getLongitude());
            if (currentDistance < minDistance)
            {
                nearestSpot = spot;
                minDistance = currentDistance;
            }
        }
        if (nearestSpot == null)
            return Pair.create(360.0, 360.0);
        else
            return Pair.create(nearestSpot.getLatitude(), nearestSpot.getLongitude());
    }

    /**
     * Register a listener.
     * @param listener listener to add
     */
    private void addListener(OnLocationListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Unregister a listener.
     * @param listener listener to remove
     */
    private void removeListener(OnLocationListener listener)
    {
        listeners.remove(listener);
    }
}
