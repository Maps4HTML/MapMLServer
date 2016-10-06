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
package org.mapml.uri;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 * @author prushfor
 */
public class QueryParamTest {
  
  public QueryParamTest() {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  @Test
  public void testParse() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServerName("none.example.com");
    request.setRequestURI("/foo");
    int zoom;
    double xmin,ymin,xmax,ymax;
    String projection,alt;
    long start;
    try {
        zoom = (Integer)QueryParam.zoom.parse(request);
        assertTrue("Default zoom value is -1", (zoom == -1));
        request.setParameter("zoom", "15");
        zoom = (Integer)QueryParam.zoom.parse(request);
        assertTrue("Should parse zoom value as 15", (zoom == 15));
        try {
            request.setParameter("zoom", "26");
            zoom = (Integer)QueryParam.zoom.parse(request);
            fail("Failed to reject invalid zoom value 26");
        } catch (RuntimeException re) {}
    } catch (RuntimeException re) {
        fail("Failure parsing zoom value.");
    }
    try {
        request.setParameter("zoom","foo");
        zoom = (Integer)QueryParam.zoom.parse(request);
        fail("Failure to reject invalid zoom value");
    } catch (RuntimeException re) {}
    try {
        request.setParameter("xmin", "-75.72056293487547");
        request.setParameter("ymin", "45.39079543037812");
        request.setParameter("xmax", "-75.69309711456299");
        request.setParameter("ymax", "45.40525984235134");
        xmin = (Double)QueryParam.xmin.parse(request);
        assertTrue("xmin value equals -75.72056293487547D",xmin==-75.72056293487547D);
        ymin = (Double)QueryParam.ymin.parse(request);
        assertTrue("ymin value equals 45.39079543037812D",ymin==45.39079543037812D);
        xmax = (Double)QueryParam.xmax.parse(request);
        assertTrue("xmax value equals -75.69309711456299D",xmax==-75.69309711456299D);
        ymax = (Double)QueryParam.ymax.parse(request);
        assertTrue("ymax value equals 45.40525984235134D",ymax==45.40525984235134D);
    } catch (RuntimeException re) {
        fail("Error parsing valid values of xmin,ymin,xmax,ymax");
    }
    try {
        projection = (String)QueryParam.projection.parse(request);
        assertTrue("Default projection should be OSMTILE",projection.equals("OSMTILE"));
        request.setParameter("projection", "WGS84");
        projection = (String)QueryParam.projection.parse(request);
        assertTrue("projection should be WGS84",projection.equals("WGS84"));
    } catch (RuntimeException re) {
        fail("Error parsing valid values of projection");
    }
    try {
        start = (Long)QueryParam.start.parse(request);
        assertTrue("Default start should be 0",start == 0L);
        request.setParameter("start","4");
        start = (Long)QueryParam.start.parse(request);
        assertTrue("Start param should be 4",start == 4L);
        try {
            request.setParameter("start","-4");
            start = (Long)QueryParam.start.parse(request);
            fail("Failed to reject invalid start value");
        } catch (RuntimeException re) {}
        try {
            request.setParameter("start","68719476736");
            start = (Long)QueryParam.start.parse(request);
            assertTrue("Able to parse maximum start value for zoom level 18",start == 68719476736L );
            request.setParameter("start","68719476737");
            start = (Long)QueryParam.start.parse(request);
            fail("Failed to reject invalid (too large) start value");
        } catch (RuntimeException re) {}
    } catch (RuntimeException re) {
        fail("Unexpected exception caught during test of start query parameter");
    }
  }
}
