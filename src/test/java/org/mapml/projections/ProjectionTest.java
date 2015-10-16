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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter.Rushforth@canada.ca
 */
public class ProjectionTest {
  private Projection proj;
  
  public ProjectionTest() {
  }
  
  @Test
  public void testWebMercator() {
      // Spherical/Web Mercator
      proj = new Projection("EPSG:3857");
      
      // a location
      LatLng latlng = new LatLng(45.398043,-75.70683);
      
      // the location above measured off  a Leaflet map:
      Point expected = new Point(-8427645.7651,5684404.3994);
      
      // the location in meters calculated with proj4j
      Point actual = proj.project(latlng);
      
      assertEquals(expected.x, actual.x, 0.0001);
      assertEquals(expected.y, actual.y, 0.0001);

      // reverse the process
      LatLng unprojected = proj.unproject(actual);
      assertEquals(latlng.lat, unprojected.lat, 0.00000001);
      assertEquals(latlng.lng, unprojected.lng, 0.00000001);
  }
  @Test
  public void testLambertConformalConic() {
      
      // NRCan LCC proj4 parameters for Canada:
      // +proj=lcc +lat_1=49 +lat_2=77 +lat_0=49 +lon_0=-95 +x_0=0 +y_0=0 +ellps=GRS80 +datum=NAD83 +units=m +no_defs
      proj = new Projection("EPSG:3978");
      
      // a location
      LatLng latlng = new LatLng(45.398043,-75.70683);

      // the location above measured off  a Proj4Leaflet map:
      Point expected = new Point(1510675.3477557,-172566.0893862);
      
      // the location in meters calculated with proj4j
      Point actual = proj.project(latlng);
      
      assertEquals(expected.x, actual.x, 0.0000001);
      assertEquals(expected.y, actual.y, 0.0000001);

      // reverse the process
      LatLng unprojected = proj.unproject(actual);
      assertEquals(latlng.lat, unprojected.lat, 0.00000001);
      assertEquals(latlng.lng, unprojected.lng, 0.00000001);
  }
  @Test
  public void testPolarStereoGraphic() {
      
      proj = new Projection("EPSG:5936");
      
      // a location
      LatLng latlng = new LatLng(75.576217,-41.060996);

      // the location above  - not measured off anything!  Will hopefully round trip...
      Point expected = new Point(3352693.445,2464159.508);
      
      // the location in meters calculated with proj4j
      Point actual = proj.project(latlng);
      
      assertEquals(expected.x, actual.x, 0.001);
      assertEquals(expected.y, actual.y, 0.001);

      // reverse the process
      LatLng unprojected = proj.unproject(actual);
      assertEquals(latlng.lat, unprojected.lat, 0.00000001);
      assertEquals(latlng.lng, unprojected.lng, 0.00000001);
  }
}
