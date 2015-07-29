/*
 * Copyright 2015 Canada Centre for Mapping and Earth Observation, 
 * Earth Sciences Sector, Natural Resources Canada.
 *
 * License
 *
 * By obtaining and/or copying this work, you (the licensee) agree that you have 
 * read, understood, and will comply with the following terms and conditions.
 * 
 * Permission to copy, modify, and distribute this work, with or without 
 * modification, for any purpose and without fee or royalty is hereby granted, 
 * provided that you include the following on ALL copies of the work or portions 
 * thereof, including modifications:
 *
 * The full text of this NOTICE in a location viewable to users of the 
 * redistributed or derivative work.
 *
 * Any pre-existing intellectual property disclaimers, notices, or terms and 
 * conditions. If none exist, the W3C Software and Document Short Notice should 
 * be included.
 * 
 * Notice of any changes or modifications, through a copyright statement on the 
 * new code or document such as "This software or document includes material 
 * copied from or derived from [title and URI of the W3C document]. 
 * Copyright © [YEAR] W3C® (MIT, ERCIM, Keio, Beihang)."
 * 
 * Disclaimers
 *
 * THIS WORK IS PROVIDED "AS IS," AND COPYRIGHT HOLDERS MAKE NO REPRESENTATIONS 
 * OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO, WARRANTIES OF 
 * MERCHANTABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT THE USE OF THE 
 * SOFTWARE OR DOCUMENT WILL NOT INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, 
 * TRADEMARKS OR OTHER RIGHTS.
 * COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL OR 
 * CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR DOCUMENT.
 * 
 * The name and trademarks of copyright holders may NOT be used in advertising or 
 * publicity pertaining to the work without specific, written prior permission. 
 * Title to copyright in this work will at all times remain with copyright holders.
*/
package org.mapml.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapml.exceptions.BadRequestException;
import com.vividsolutions.jts.geom.Envelope;

public class Bbox {

    private final double west, south, east, north;
    private static final Logger log =  LogManager.getLogger();
    private Envelope env = null;

    public Bbox(String bbox) {
        List<Double> ord;
        try {
            ord = parse(bbox);
        } catch (Exception e) {
            String msg = "Error parsing bbox parameter: " + bbox
                            + e.getMessage();
            log.error(msg);
            throw new BadRequestException(msg, e);
        }
        this.west = ord.get(0);
        this.south = ord.get(1);
        this.east = ord.get(2);
        this.north = ord.get(3);
        this.env = new Envelope (west,east,south,north);
    }

    public boolean isValid() {
        if (this.env.getArea() == 0D || this.env.getHeight() == 0D || this.env.getWidth() == 0D) {
          String msg = "Bad bbox / xmin / ymin/ xmax/ymax:" + this.env.toString();
          log.error(msg);
          return false;
        }
        if (this.west >= this.east || this.south >= this.north) {
          String msg = "Bad bbox / xmin / ymin / xmax / ymax:" + this.env.toString();
          log.error(msg);
          return false;
        }
        return true;
    }

    public Bbox(List<Double> bbox) {
        this.west = bbox.get(0);
        this.south = bbox.get(1);
        this.east = bbox.get(2);
        this.north = bbox.get(3);
        this.env = new Envelope (west,east,south,north);
        if (!isValid()) {
            throw new BadRequestException("Invalid bbox parameter(s) west: "
                +this.west+" south: "+this.south+" east: "+this.east+" north: "+this.north);
        }
    }

    public Envelope getEnvelope() {
      return env;
    }

    /**
     * Parse a string into a List of doubles in presumed order: west,south,east,north
     * @param bbox comma-separated list of doubles in west,south,east,north order
     * @return a List in the order west,south,east,north
     */
    public static List<Double> parse(String bbox) {

        List<String> ordinates = commaSeparatedStringToStringArray(bbox);

        if (ordinates.size() != 4) {
            throw new BadRequestException(
                            "Number of ordinates in bbox must be 4: west,south,east,north.");
        }

        List<Double> ordDoubles = new ArrayList<>();
        ordDoubles.add(Double.valueOf(ordinates.get(0)));
        ordDoubles.add(Double.valueOf(ordinates.get(1)));
        ordDoubles.add(Double.valueOf(ordinates.get(2)));
        ordDoubles.add(Double.valueOf(ordinates.get(3)));

        double minLong = ordDoubles.get(0);
        double minLat = ordDoubles.get(1);
        double maxLong = ordDoubles.get(2);
        double maxLat = ordDoubles.get(3);

        String msg = null;
        if (minLong >= maxLong) {
            msg = "minLong must be greater than maxLong: " + minLong + " " + maxLong;
        } else if (minLat >= maxLat) {
            msg = "minLat must be greater than maxLat: " + minLat + " " + maxLat;
        }

        if (msg != null)
            throw new BadRequestException(msg);

        return ordDoubles;
    }

    public double getWest() {
        return west;
    }

    public double getSouth() {
        return south;
    }

    public double getEast() {
        return east;
    }

    public double getNorth() {
        return north;
    }

    public String toString() {
        return west + "," + south + "," + east + "," + north;
    }

    private static List<String> commaSeparatedStringToStringArray(String aString) {
        String[] splittArray = null;
        if (aString != null && !aString.equalsIgnoreCase("")) {
            splittArray = aString.split(",");
        }
        if (splittArray.length != 4) {
            throw new BadRequestException("Invalid number of bbox parameters: "+splittArray.length);
        }
        return Arrays.asList(splittArray);
    }
}