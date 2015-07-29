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

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class BboxTest {
  
  public BboxTest() {
  }

  @Test
  public void testConstruction() {
      String tooFewNumbers = "123.5,89.1,45";
      String tooManyNumbers = "123,13,135,45,92";
      double xmin,ymin,xmax,ymax,temp;
      xmin = -75.72056293487547D;
      ymin = 45.39079543037812D;
      xmax = -75.69309711456299D;
      ymax = 45.40525984235134D;
      Bbox bbox;
      try {
          bbox = new Bbox(tooFewNumbers);
          fail("Failed to reject invalid number of bbox parameters: "+ tooFewNumbers);
      } catch(RuntimeException rte) {}
      try {
          bbox = new Bbox(tooManyNumbers);
          fail("Failed to reject invalid number of bbox parameters: "+ tooManyNumbers);
      } catch (RuntimeException re) {}
      bbox = new Bbox(Arrays.asList(xmin,ymin,xmax,ymax));
      assertTrue("Valid bbox isValid",bbox.isValid());
      try {
          bbox = new Bbox(Arrays.asList(xmax,ymin,xmin,ymax));
          fail("Failed to reject xmin > xmax bbox");
      } catch (RuntimeException re) {}
      try {
          bbox = new Bbox(Arrays.asList(xmin,ymax,xmax,ymin));
          fail("Failed to reject ymin > ymax bbox");
      } catch (RuntimeException re) {}
      try {
          bbox = new Bbox(Arrays.asList(xmin,ymax,xmin,ymin));
          fail("Failed to reject xmin == xmax bbox");
      } catch (RuntimeException re) {}
      try {
          bbox = new Bbox(Arrays.asList(xmin,ymin,xmax,ymin));
          fail("Failed to reject ymin == ymax bbox");
      } catch (RuntimeException re) {}
  }
}
