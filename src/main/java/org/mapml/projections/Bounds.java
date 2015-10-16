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
package org.mapml.projections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.mapml.exceptions.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Bounds {
  
  protected Point min,max;
  private static final Logger log =  LogManager.getLogger();
  
  public Bounds(Point a, Point b) {
    this.extend(a);
    this.extend(b);
  }
  
  public Bounds(String bounds) {
        List<Double> ord;
        try {
            ord = parse(bounds);
        } catch (Exception e) {
            String msg = "Error parsing bounds parameter: " + bounds
                            + e.getMessage();
            log.error(msg);
            throw new BadRequestException(msg, e);
        }
        Point a = new Point(ord.get(0),ord.get(1));
        Point b = new Point(ord.get(2),ord.get(3));
        this.extend(a);
        this.extend(b);
  }
    /**
     * Parse a string into a List of doubles in presumed order: west,south,east,north
     * @param bounds comma-separated list of doubles in west,south,east,north order
     * @return a List in the order west,south,east,north
     */
    public static List<Double> parse(String bounds) {

        List<String> ordinates = commaSeparatedStringToStringArray(bounds);

        if (ordinates.size() != 4) {
            throw new BadRequestException(
                            "Number of ordinates in bounds must be 4.");
        }

        List<Double> ordDoubles = new ArrayList<>();
        ordDoubles.add(Double.valueOf(ordinates.get(0)));
        ordDoubles.add(Double.valueOf(ordinates.get(1)));
        ordDoubles.add(Double.valueOf(ordinates.get(2)));
        ordDoubles.add(Double.valueOf(ordinates.get(3)));

        return ordDoubles;
    }
    private static List<String> commaSeparatedStringToStringArray(String aString) {
        String[] splittArray = null;
        if (aString != null && !aString.equalsIgnoreCase("")) {
            splittArray = aString.split(",");
        }
        if (splittArray.length != 4) {
            throw new BadRequestException("Invalid number of bounds parameters: "+splittArray.length);
        }
        return Arrays.asList(splittArray);
    }
  
  final Bounds extend(Point point) {
    if (min == null && max == null) {
      min = point.clone();
      max = point.clone();
    } else {
      this.min.x = Math.min(point.x, this.min.x);
      this.max.x = Math.max(point.x, this.max.x);
      this.min.y = Math.min(point.y, this.min.y);
      this.max.y = Math.max(point.y, this.max.y);
    }
    return this;
  }
  
  public Point getCentre() {
      return new Point((this.min.x + this.max.x)/2,(this.min.y + this.max.y)/2);
  }
  
  public Point getMin() {
    return this.min;
  }
  
  public Point getMax() {
    return this.max;
  }
  public boolean intersects(Bounds other) {
    
      Point min = this.min;
      Point max = this.max;
      Point min2 = other.min;
      Point max2 = other.max;
      boolean xIntersects = (max2.x >= min.x) && (min2.x <= max.x);
      boolean yIntersects = (max2.y >= min.y) && (min2.y <= max.y);
      return xIntersects && yIntersects;
    
  }
  
}
