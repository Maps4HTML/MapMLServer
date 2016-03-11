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

import org.mapml.exceptions.MapMLException;
import org.mapml.projections.Bounds;
import org.mapml.projections.TiledCRS;

  /**
   * This class represents the bounds of the MapML service, such that a request
   * may be checked to see if it is within, overlaps or is disjoint to the
   * service bounds.
  */
public class MapMLServiceBounds {
    int minZoom;
    int maxZoom;
    Bounds bounds;
    Bounds[] pixelBounds;
    final String projection;
    TiledCRS tiledCRS;
    public static int MAX_LEVELS = 26;
    /**
     * Create the service bounds.
     * @param minZoom the minimum zoom level (smallest scale) 
     * @param maxZoom the maximum zoom level (largest scale)
     * @param bounds  the horizontal extent of the service, in projected *but not scaled*, units
     * @param projection the name of the Tiled Coordinate Reference System
     */
    public MapMLServiceBounds(int minZoom, int maxZoom, Bounds bounds, TiledCRS tiledCRS) {
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.bounds = bounds;
        this.tiledCRS = tiledCRS;
        this.projection = tiledCRS.getName();
        if (minZoom < 0) {
          throw new MapMLException("bad minZoom: "+ minZoom);
        }
        int levels = maxZoom - minZoom + 1;
        if (levels > MAX_LEVELS) {
          throw new MapMLException("Number of zoom levels ("+levels+") in range: "
                  +minZoom+"-"+maxZoom+" exceeds maximum: "+MAX_LEVELS);
        }
        if (maxZoom > tiledCRS.getMaxZoom()) {
          throw new MapMLException("maxZoom supplied ("+maxZoom+
                  ") exceeds maximum zoom of tiled CRS definition for "
                  +this.projection +"("+tiledCRS.getMaxZoom()+")");
        }
        this.pixelBounds = new Bounds[MAX_LEVELS];
        for (int i = minZoom; i <= maxZoom;i++) {
            this.pixelBounds[i] = tiledCRS.getPixelBounds(bounds, i);
        }
    }
    public double getXmin() { return bounds.getMin().x; }
    public double getYmin() { return bounds.getMin().y; }
    public double getXmax() { return bounds.getMax().x; }
    public double getYmax() { return bounds.getMax().y; }
    public int getMinZoom() { return this.minZoom; }
    public int getMaxZoom() { return this.maxZoom; }
    public Bounds getBounds() { return this.bounds; }
    public Bounds getPixelBounds(int zoom) {
        if (!(minZoom <= zoom && zoom <= maxZoom)) throw new MapMLException("Bad zoom ("+zoom+") bounds requested");
        return this.pixelBounds[zoom];
    }
    /**
     * Determine if the given bounds (other) intersects this bounds AND if the
     * given zoom level is within the min/max range of zoom level for this
     * @param zoom -1 is a noop, otherwise checks that minZoom <= zoom <= maxZoom
     * @param other null is a noop, otherwise checks that the other envelope intersects 
     * the envelope of this object
     * @return  this.bounds intersects other.bounds
     */
    public boolean intersects(int zoom, Bounds other) {
        if (zoom == -1) return false;
        if (other == null) return false;
        return this.pixelBounds[zoom].intersects(other);
    }
}