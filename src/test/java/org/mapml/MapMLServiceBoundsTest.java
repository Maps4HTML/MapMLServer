/*
 * Copyright 2016 Canada Centre for Mapping and Earth Observation, 
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

import org.junit.Test;
import static org.junit.Assert.*;
import org.mapml.projections.Bounds;
import org.mapml.projections.LatLng;
import org.mapml.projections.Point;
import org.mapml.projections.TiledCRS;

/**
 *
 * @author Peter.Rushforth@canada.ca
 */
public class MapMLServiceBoundsTest {
  
  @Test
  public void testServiceBounds() {
    TiledCRS osmTiledCRS = new TiledCRS("OSMTILE");
    TiledCRS cbmTiledCRS = new TiledCRS("CBMTILE");
    
    // MapMLServiceBounds(int minZoom, int maxZoom, Bounds bounds, TiledCRS tiledCRS)
    Point min = osmTiledCRS.project(new LatLng(-85.0511287798,-180));
    Point max = osmTiledCRS.project(new LatLng(85.0511287798,180));
    Bounds osmBounds = new Bounds(min,max);
    min = cbmTiledCRS.project(new LatLng(-85.0511287798,-180));
    max = cbmTiledCRS.project(new LatLng(85.0511287798,180));
    Bounds cbmBounds = new Bounds(min,max);

    MapMLServiceBounds msb = null;
    try {
       msb = new MapMLServiceBounds(0,18, osmBounds, osmTiledCRS);
       assertNotNull(msb);
       msb = new MapMLServiceBounds(3,15, osmBounds, osmTiledCRS);
       assertNotNull(msb);
       msb = new MapMLServiceBounds(0,19, cbmBounds, cbmTiledCRS);
       assertNotNull(msb);
       msb = new MapMLServiceBounds(7,10, cbmBounds, cbmTiledCRS);
       assertNotNull(msb);
    } catch (RuntimeException e) {
       fail("Error creating service bounds: "+e.getMessage());
    }
    try {
      msb = new MapMLServiceBounds(7,26, cbmBounds, cbmTiledCRS);
      fail("Failed due to no exception thrown during attempted bad service bounds creation");
    } catch (RuntimeException e) {
      assertNotNull(msb);
    }
    try {
      msb = new MapMLServiceBounds(0,26, cbmBounds, cbmTiledCRS);
      fail("Failed due to no exception thrown during attempted bad service bounds creation");
    } catch (RuntimeException e) {
      assertNotNull(msb);
    }
    msb = new MapMLServiceBounds(0,18, osmBounds, osmTiledCRS);
    Bounds b = null;
    try {
        b = msb.getPixelBounds(7);
    } catch (RuntimeException e) {
        fail("Exception during query for pixel bounds of zoom level 7 of MapMLServiceBounds");
    }
    b = null;
    try {
        b = msb.getPixelBounds(-1);
        fail("No exception thrown when accessing bounds for negative zoom level");
    } catch (RuntimeException e) {
        assertNull(b);
    }
    
    
  }

  
}
