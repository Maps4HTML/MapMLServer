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

package org.mapml;

import org.mapml.util.Bbox;

  /**
   * This class represents the bounds of the MapML service, such that a request
   * may be checked to see if it is within, overlaps or is disjoint to the
   * service bounds.
  */
public class MapMLServiceBounds {
    int minZoom;
    int maxZoom;
    Bbox bbox;
    final String projection;
    /**
     * Create the service bounds.
     * @param minZoom the minimum zoom level (smallest scale) 
     * @param maxZoom the maximum zoom level (largest scale)
     * @param bbox  the horizontal extent of the service, in WGS84
     */
    public MapMLServiceBounds(int minZoom, int maxZoom, Bbox bbox){
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.bbox = bbox;
        this.projection = "WGS84";
    }
    public double getWest() { return bbox.getWest(); }
    public double getSouth() { return bbox.getSouth(); }
    public double getEast() { return bbox.getEast(); }
    public double getNorth() { return bbox.getNorth(); }
    public int getMinZoom() { return this.minZoom; }
    public int getMaxZoom() { return this.maxZoom; }
    public Bbox getBbox() { return this.bbox; }
    /**
     * Determine if the given bbox (other) intersects this bbox AND if the
     * given zoom level is within the min/max range of zoom level for this
     * @param zoom -1 is a noop, otherwise checks that minZoom <= zoom <= maxZoom
     * @param other null is a noop, otherwise checks that the other envelope intersects 
     * the envelope of this object
     * @return  this.bounds intersects other.bounds
     */
    public boolean intersects(int zoom, Bbox other) {
        if (zoom == -1) return false;
        if (other == null) return false;
        return (this.minZoom <= zoom && zoom <= this.maxZoom && 
            this.bbox.getEnvelope().intersects(other.getEnvelope()));
    }
}