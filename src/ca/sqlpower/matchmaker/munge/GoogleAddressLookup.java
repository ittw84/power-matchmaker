/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.matchmaker.MatchMakerEngine.EngineMode;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;

public class GoogleAddressLookup extends AbstractMungeStep {

	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
    
	private String googleMapsApiKey;
	private String googleGeocoderURL;
    
    /**
     * Minimum number of seconds between lookup requests. Google throttles access
     * to their geocoder service, so unless you have a prior arrangement with Google,
     * be sure to set this value high enough to avoid being flagged as an abuser.
     * Presently (August 2008), 2.0 is a reasonable lower limit.
     */
	private double rateLimit;

    /**
     * The last time a lookup request was issued. This step will block so as to
     * respect the {@link #rateLimit} if it is called too frequently.
     */
    private long lastLookupTime;
    
    /**
     * The status code returned with the Google result.  Even if the lookup fails or the
     * API key is incorrect, this three-digit status code will still be set.  It will only
     * come out null if the URL for the geocoder service is incorrect (therefore there
     * could be no response from the Google Maps Geocoder).
     * <p>
     * The meaning of the status codes is available in
     * <a href="http://www.google.com/apis/maps/documentation/reference.html#GGeoStatusCode">
     * the Google Maps API documentation</a>.
     */
    private MungeStepOutput<BigDecimal> statusCode;
    
    private MungeStepOutput<String> country;
    private MungeStepOutput<String> adminArea;
    private MungeStepOutput<String> subAdminArea;
    private MungeStepOutput<String> locality;
    private MungeStepOutput<String> street;
    private MungeStepOutput<String> postCode;
    private MungeStepOutput<BigDecimal> latitude;
    private MungeStepOutput<BigDecimal> longitude;
    
    /**
     * The accuracy constant for the location information.  Constant codes are
     * documented in
     * <a href="http://www.google.com/apis/maps/documentation/reference.html#GGeoAddressAccuracy">
     * the Google Maps API documentation</a>.
     */
    private MungeStepOutput<BigDecimal> accuracy;
    
    @Constructor
    public GoogleAddressLookup() {
        super("Google Maps Address Lookup",false);
    }

    @Override
	public void init() {
		super.addInput(new InputDescriptor("Address", String.class));
        
        addChild(statusCode = new MungeStepOutput<BigDecimal>("Lookup Status", BigDecimal.class));
        addChild(country = new MungeStepOutput<String>("Country Code", String.class));
        addChild(adminArea = new MungeStepOutput<String>("Administrative Area", String.class));
        addChild(subAdminArea = new MungeStepOutput<String>("Sub-Administrative Area", String.class));
        addChild(locality = new MungeStepOutput<String>("Locality", String.class));
        addChild(street = new MungeStepOutput<String>("Street Address", String.class));
        addChild(postCode = new MungeStepOutput<String>("Postal Code", String.class));
        addChild(latitude = new MungeStepOutput<BigDecimal>("Latitude", BigDecimal.class));
        addChild(longitude = new MungeStepOutput<BigDecimal>("Longitude", BigDecimal.class));
        addChild(accuracy = new MungeStepOutput<BigDecimal>("Accuracy Code", BigDecimal.class));
        
        googleMapsApiKey = "";
        googleGeocoderURL = "http://maps.google.com/maps/geo";
        rateLimit = 2.0;
	}
     
    @Override
    public void doOpen(EngineMode mode, Logger logger) throws Exception {
        String key = googleMapsApiKey;
        if (key == null || key.length() == 0) {
        	throw new IllegalStateException("Google Address Lookup transformer was " +
        			"called without a Google Maps API Key. " +
        			"Check your Google Address Lookup transformer settings.");
        }
    }

    @Override
    public Boolean doCall() throws Exception {
    	// if this step was loaded and init() was never called,
    	// we need to assign names to the loaded outputs.
    	if (statusCode == null) { // if one is null, they will all be null
    		List<MungeStepOutput> outputs = getChildren(MungeStepOutput.class); 
    		statusCode = outputs.get(0);
    		country = outputs.get(1);
    		adminArea = outputs.get(2);
    		subAdminArea = outputs.get(3);
    		locality = outputs.get(4);
    		street = outputs.get(5);
    		postCode = outputs.get(6);
    		latitude = outputs.get(7);
    		longitude = outputs.get(8);
    		accuracy = outputs.get(9);
    	}
    	
    	// Clear out all the output values in case the request fails!
        for (MungeStepOutput<?> output : getChildren(MungeStepOutput.class)) {
            output.setData(null);
        }
        
        String key = googleMapsApiKey;
        String url = googleGeocoderURL;
        String address = (String) getMSOInputs().get(0).getData();
        url += "?output=json&key="+key+"&q="+URLEncoder.encode(address, "utf-8");
        
        String responseText = readURL(url);
        logger.debug("Address Lookup Response for \""+address+"\": " + responseText);
        JSONObject response = new JSONObject(responseText);

        JSONObject status = response.getJSONObject("Status");
        int statusCode = status.getInt("code");
        this.statusCode.setData(BigDecimal.valueOf(statusCode));
        
        if (!response.has("Placemark")) {
            logger.error("Address lookup for " + address + " failed. Google error code was " + statusCode + ".");
            return Boolean.TRUE;
        }
        
        JSONArray placemarks = response.getJSONArray("Placemark");
        // TODO count the number of placemarks in the response and provide it as an output
        
        JSONObject placemark = placemarks.getJSONObject(0);
        JSONObject addressDetails = placemark.getJSONObject("AddressDetails");
        JSONObject country = addressDetails.getJSONObject("Country");
        this.country.setData(country.getString("CountryNameCode"));

        JSONObject adminArea = country.getJSONObject("AdministrativeArea");
        this.adminArea.setData(adminArea.getString("AdministrativeAreaName"));

        // The topology of Canadian lookup requests changed some time in 2008:
        // The SubAdministrativeArea section vanished, and Locality moved up
        // to be a direct child of AdministrativeArea. The following code is
        // designed to cope with both situations, in case lookups in other
        // countries still return a SubAdministrativeArea.
        if (adminArea.has("SubAdministrativeArea")) {
            JSONObject subAdminArea = adminArea.getJSONObject("SubAdministrativeArea");
            this.subAdminArea.setData(subAdminArea.getString("SubAdministrativeAreaName"));

            if (subAdminArea.has("Locality")) {
                JSONObject locality = subAdminArea.getJSONObject("Locality");
                updateLocalityOutputs(locality);
            }
        } else if (adminArea.has("Locality")) {
            JSONObject locality = adminArea.getJSONObject("Locality");
            updateLocalityOutputs(locality);
        }
        
        this.accuracy.setData(BigDecimal.valueOf(addressDetails.getInt("Accuracy")));
        
        JSONObject location = placemark.getJSONObject("Point");
        JSONArray coordinates = location.getJSONArray("coordinates");
        this.longitude.setData(BigDecimal.valueOf(coordinates.getDouble(0)));
        this.latitude.setData(BigDecimal.valueOf(coordinates.getDouble(1)));
        
        return Boolean.TRUE;
    }

    private void updateLocalityOutputs(JSONObject locality)
            throws JSONException {
        this.locality.setData(locality.getString("LocalityName"));

        JSONObject thoroughfare = locality.getJSONObject("Thoroughfare");
        this.street.setData(thoroughfare.getString("ThoroughfareName"));

        JSONObject postalCode = locality.getJSONObject("PostalCode");
        this.postCode.setData(postalCode.getString("PostalCodeNumber"));
    }

    /**
     * Requests the given HTTP URL and returns the response body. This method
     * respects the {@link #LOOKUP_RATE_LIMIT} parameter by delaying the request
     * until sufficient time has passed.
     * 
     * @param url The URL to request
     * @return The body content returned by the server, as a string.
     * @throws IOException If the remote HTTP server returns a failure result code.
     */
    private String readURL(String url) throws IOException {
        long rateLimitMS = (long) (getRateLimit() * 1000.0);
        long nextAllowedLookupTime = lastLookupTime + rateLimitMS;
        while (nextAllowedLookupTime > System.currentTimeMillis()) {
            try {
                Thread.sleep(nextAllowedLookupTime - System.currentTimeMillis());
            } catch (InterruptedException e) {
                // we'll just go around the loop again
            }
        }
        HttpURLConnection dest = (HttpURLConnection) new URL(url).openConnection();
        dest.setDoOutput(false);
        dest.setDoInput(true);
        dest.setRequestMethod("GET");
        dest.connect();

        InputStream inStream = dest.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
        StringBuilder sb = new StringBuilder(1000);
        char[] cbuf = new char[1000];
        int size;
        while ((size = in.read(cbuf, 0, cbuf.length)) > 0) {
            sb.append(cbuf, 0, size);
        }
        in.close();
        dest.disconnect();
        
        lastLookupTime = System.currentTimeMillis();

        return sb.toString();
    }

    @Accessor
	public String getGoogleMapsApiKey() {
		return googleMapsApiKey;
	}

    @Mutator
	public void setGoogleMapsApiKey(String googleMapsApiKey) {
    	String oldKey = this.googleMapsApiKey;
		this.googleMapsApiKey = googleMapsApiKey;
		firePropertyChange("googleMapsApiKey", oldKey, googleMapsApiKey);
	}

    @Accessor
	public String getGoogleGeocoderURL() {
		return googleGeocoderURL;
	}

    @Mutator
	public void setGoogleGeocoderURL(String googleGeocoderURL) {
    	String oldURL = this.googleGeocoderURL;
		this.googleGeocoderURL = googleGeocoderURL;
		firePropertyChange("googleGeocoderURL", oldURL, googleGeocoderURL);
	}
	
	@Accessor
	public double getRateLimit() {
		return rateLimit;
	}

	@Mutator
	public void setRateLimit(double rateLimit) {
		double oldLimit = this.rateLimit;
		this.rateLimit = rateLimit;
		firePropertyChange("rateLimit", oldLimit, rateLimit);
	}
	
	@Override
	protected void copyPropertiesForDuplicate(MungeStep copy) {
		GoogleAddressLookup step = (GoogleAddressLookup) copy;
		step.setGoogleGeocoderURL(getGoogleGeocoderURL());
		step.setGoogleMapsApiKey(getGoogleMapsApiKey());
		step.setRateLimit(getRateLimit());
	}
}
