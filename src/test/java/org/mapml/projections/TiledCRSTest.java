package org.mapml.projections;

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
 * Copyright Â© [YEAR] W3CÂ® (MIT, ERCIM, Keio, Beihang)."
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mapml.MapMLConstants;
import org.mapml.projections.TiledCRS.TileComparator;

/**
 *
 * @author Peter.Rushforth@canada.ca
 */
public class TiledCRSTest {
  
  public TiledCRSTest() {
  }
  
  @Before
  public void setUp() {
      MapMLConstants.PAGESIZE = 100;
  }
  
  @Test
  public void testOSMTTILEProjection() {
      TiledCRS osmtile = new TiledCRS("OSMTILE");
      
      // a location
      LatLng latlng = new LatLng(45.398043D,-75.70683D);
      
      // the location above measured off a Leaflet map:
      Point expected = new Point(9720828.0D,12017718.0D);
      
      // the location in pixels calculated with proj4j
      Point actual = osmtile.latLngToPoint(latlng, 17);
      
      // 10cm seems close enough...
      assertEquals(expected.x, actual.x, 0.1);
      assertEquals(expected.y, actual.y, 0.1);

      // reverse the process
      LatLng unprojected = osmtile.pointToLatLng(actual,17);
      assertEquals(latlng.lat, unprojected.lat, 0.00000001);
      assertEquals(latlng.lng, unprojected.lng, 0.00000001);
      
  }
  @Test
  public void testOSMTILESorting() {
      TiledCRS osmtile = new TiledCRS("OSMTILE");

      // this query has 8 tiles in it (at zoom=15)
      LatLngBounds query = new LatLngBounds(new LatLng(45.39079543037812,-75.7205629348755), new LatLng(45.40525984235134,-75.693097114563));
      List<TileCoordinates> tiles = osmtile.getTilesForExtent(query, 15, 0);
      assertEquals("Expect 8 tiles for extent: ", 8, tiles.size());

      ArrayList<TileCoordinates> expectedOrder = new ArrayList<>();
      // observed order in default Leaflet results:
      expectedOrder.add(new TileCoordinates(9492,11736,15));
      expectedOrder.add(new TileCoordinates(9493,11736,15));
      expectedOrder.add(new TileCoordinates(9492,11735,15));
      expectedOrder.add(new TileCoordinates(9493,11735,15));
      expectedOrder.add(new TileCoordinates(9491,11736,15));
      expectedOrder.add(new TileCoordinates(9494,11736,15));
      expectedOrder.add(new TileCoordinates(9491,11735,15));
      expectedOrder.add(new TileCoordinates(9494,11735,15));

      Bounds pb = osmtile.getPixelBounds(query, 15);
      Point centre  = pb.getCentre().divideBy(256);
      Collections.sort(expectedOrder, osmtile.new TileComparator(centre));

      ListIterator<TileCoordinates> li = tiles.listIterator();
      while (li.hasNext()) {
        int index = li.nextIndex();
        TileCoordinates actual = li.next();
        TileCoordinates expected = expectedOrder.get(index);
        boolean coordinatesAreTheSame = (expected.x == actual.x && expected.y == actual.y && expected.z == actual.z);
        assertTrue(coordinatesAreTheSame);
      }
  }
  @Test
  public void testOSMTILEPaging() {
      // now test the tiles returned for defined extents and scales
      TiledCRS osmtile = new TiledCRS("OSMTILE");
      
      osmtile.setPageSize(5);
      LatLngBounds query = new LatLngBounds(new LatLng(45.39079543037812,-75.7205629348755), new LatLng(45.40525984235134,-75.693097114563));
      List<TileCoordinates> tiles = osmtile.getTilesForExtent(query, 15, 0);
      assertEquals("Expect 5 tiles for first page of extent: ", 5, tiles.size());
      tiles = osmtile.getTilesForExtent(query, 15, 5);
      assertEquals("Expect 3 tiles for second page extent: ", 3, tiles.size());
      
  }
  @Test
  public void testOSMTILEScaleSet() {
      TiledCRS osmtile = new TiledCRS("OSMTILE");
      // assure that there are at least 18 zoom levels.  Uncertain how many
      // would be standard.  Seems that most Web maps go up to 19 levels or so...
      // probably would be wise to go a bit higher for OSMTILE
  }
  public void testOSMTILEBounds() {
    // assure that the limits of the tiled projection are respected at different
    // zoom levels.
    // what does *respected* mean???  wrapped? exception?  empty result?
    
  }
  @Test
  public void testCBMLCCProjection() {
      TiledCRS cbmlcc = new TiledCRS("CBMTILE");
      
      // a location
      LatLng latlng = new LatLng(45.398043, -75.70683);

      // the location above measured off a Proj4Leaflet map at zoom=17:
      Point expected = new Point(7810966.680052839,8527151.324584836);
      
      // the location in meters calculated with proj4j and scaled by this class
      Point actual = cbmlcc.latLngToPoint(latlng, 17);
      
      // one centimeter seems close enough
      assertEquals(expected.x, actual.x, 0.01);
      assertEquals(expected.y, actual.y, 0.01);

      // reverse the process
      LatLng unprojected = cbmlcc.pointToLatLng(actual, 17);
      assertEquals(latlng.lat, unprojected.lat, 0.00000001);
      assertEquals(latlng.lng, unprojected.lng, 0.00000001);
    
  }
  @Test
  public void testCBMLCCSorting() {
    
      TiledCRS cbmlcc = new TiledCRS("CBMTILE");
      
      // this query has 9 tiles in it (at zoom=17)
      LatLngBounds query = new LatLngBounds(new LatLng(45.39257399473906, -75.72876150576825), new LatLng(45.40352437491144, -75.68484627676823));
      List<TileCoordinates> tiles = cbmlcc.getTilesForExtent(query, 17, 0);
      assertEquals("Expect 9 tiles for extent: ", 9, tiles.size());
      
      ArrayList<TileCoordinates> expectedOrder = new ArrayList<>();
      // observed order in Proj4Leaflet results:
      expectedOrder.add(new TileCoordinates(30511,33309,17));
      expectedOrder.add(new TileCoordinates(30511,33308,17));
      expectedOrder.add(new TileCoordinates(30510,33309,17));
      expectedOrder.add(new TileCoordinates(30512,33309,17));
      expectedOrder.add(new TileCoordinates(30511,33310,17));
      expectedOrder.add(new TileCoordinates(30510,33308,17));
      expectedOrder.add(new TileCoordinates(30512,33308,17));
      expectedOrder.add(new TileCoordinates(30510,33310,17));
      expectedOrder.add(new TileCoordinates(30512,33310,17));

      Bounds pb = cbmlcc.getPixelBounds(query, 17);
      Point centre  = pb.getCentre().divideBy(256);
      Collections.sort(expectedOrder, cbmlcc.new TileComparator(centre));

      ListIterator<TileCoordinates> li = tiles.listIterator();
      while (li.hasNext()) {
        int index = li.nextIndex();
        TileCoordinates actual = li.next();
        TileCoordinates expected = expectedOrder.get(index);
        boolean coordinatesAreTheSame = (expected.x == actual.x && expected.y == actual.y && expected.z == actual.z);
        assertTrue(coordinatesAreTheSame);
      }
  }
  public void testCBMLCCPaging() {
    
  }
  public void testCBMLCCScales() {
    
  }
  public void testCBMLCCBounds() {
    
  }
}
