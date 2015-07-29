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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mapml.util.Bbox;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.mapml.MapMLPrinter.TileCoordinates;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author Peter Rushforth
 */
public class MapMLPrinterTest {
  private MapMLPrinter printer;
  public MapMLPrinterTest() {
  }
  
  @Before
  public void setUp() {
    this.printer = new MapMLPrinter();
    MapMLServiceBounds b = new MapMLServiceBounds(0,18, new Bbox("-180,-85.0511,180,85.0511"));
    printer.setServiceBounds(b);
    printer.setTileServers("a,b,c");
    String[] templates = {"http://{s}.example.com/tile/{z}/{x}/{y}/","http://none.foobar.com/tile/{z}/{x}/{y}/"};
    printer.setTileUrlTemplates(templates);
    printer.setPageSize(100);
    printer.setLicenseUrl("http://example.org/license");
    printer.setLicenseText("Example license text");
  }
  
  @Test
  public void testXml() {
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(ba);
    printer.printMapMLDoc("application/xml", 0, "http://example.com", 15, new Bbox("-75.72056293487547,45.39079543037812,-75.69309711456299,45.40525984235134"), out);
    out.flush();
    String result = ba.toString();
    assertNotNull(result);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);
    dbf.setNamespaceAware(false);
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      try  {
          Document d = db.parse(new ByteArrayInputStream(ba.toByteArray()));
          // message,expected,actual
          assertEquals("MapML document must begin with <mapml> element","mapml",d.getDocumentElement().getNodeName());
          assertEquals("MapML document must have one <head> element",1,d.getElementsByTagName("head").getLength());
          assertEquals("MapML document must have one <body> element",1,d.getElementsByTagName("body").getLength());
          assertEquals("test MapML document must have 6 input elements",6,d.getElementsByTagName("input").getLength());
          assertEquals("test MapML document must have 16 tile elements",16,d.getElementsByTagName("tile").getLength());
      } catch (Exception se) {
          fail("Error parsing MapML - document not well-formed");
      }
    } catch (ParserConfigurationException e) {}
  }
  @Test
  public void testLicense() {
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(ba);
    printer.printMapMLDoc("application/xml", 0, "http://example.com", 15, new Bbox("-75.72056293487547,45.39079543037812,-75.69309711456299,45.40525984235134"), out);
    out.flush();
    String result = ba.toString();
    assertNotNull(result);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);
    dbf.setNamespaceAware(false);
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      try  {
          Document d = db.parse(new ByteArrayInputStream(ba.toByteArray()));
          Element licenseLink = null;
          NodeList links = d.getElementsByTagName("link");
          for (int i=0;i<links.getLength();i++) {
              if (links.item(i).getAttributes().getNamedItem("rel").getNodeValue().equalsIgnoreCase("license")) {
                  licenseLink = (Element)links.item(i);
                  break;
              }
          }
          assertNotNull(licenseLink);
          assertTrue(licenseLink.getAttribute("href").equals("http://example.org/license"));
          assertTrue(licenseLink.getAttribute("text").equals("Example license text"));
      } catch (Exception se) {
          fail("Error parsing MapML - document not well-formed");
      }
    } catch (ParserConfigurationException e) {}
  }
  @Test
  public void testOrderedFromCentreOut() {
      String[] template = {"http://none.foobar.com/tile/{z}/{x}/{y}/"};
      printer.setTileUrlTemplates(template);
      
      int zoom = 15;
      Bbox requestBbox = new Bbox("-75.72056293487547,45.39079543037812,-75.69309711456299,45.40525984235134");
      double cenLat = requestBbox.getEnvelope().centre().y;
      double cenLon = requestBbox.getEnvelope().centre().x;
      MapMLPrinter.TileDecimalCoordinates centre = printer.getTileDecimalCoordinates(zoom,cenLat,cenLon);
      double dist, prev = 0.0D;
      

      ByteArrayOutputStream ba = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(ba);
      printer.printMapMLDoc("application/xml", 0, "http://example.com", zoom ,requestBbox, out);
      out.flush();
      String result = ba.toString();
      assertNotNull(result);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setValidating(false);
      dbf.setNamespaceAware(false);
      try {
        DocumentBuilder db = dbf.newDocumentBuilder();
        try  {
            Document d = db.parse(new ByteArrayInputStream(ba.toByteArray()));
            // message,expected,actual
            NodeList nl = d.getElementsByTagName("tile");
            for (int i=0;i< nl.getLength();i++) {
              NamedNodeMap atts = nl.item(i).getAttributes();
              long x = Long.parseLong(atts.getNamedItem("x").getNodeValue());
              long y = Long.parseLong(atts.getNamedItem("y").getNodeValue());
              MapMLPrinter.TileCoordinates tc = printer.new TileCoordinates(zoom,x,y);
              dist = centre.distanceTo(tc);
              assertTrue("Tiles must be ordered in increasing distance from request centre", dist >= prev);
              prev = dist;
            }
            
        } catch (Exception se) {
            fail("Error parsing MapML - document not well-formed");
        }
      } catch (ParserConfigurationException e) {}
  }
  @Test
  public void testPaging() {
      String[] template = {"http://none.foobar.com/tile/{z}/{x}/{y}/"};
      printer.setTileUrlTemplates(template);
      printer.setPageSize(4);
      int zoom = 15;
      // this bbox should return 8 tiles
      Bbox requestBbox = new Bbox("-75.72056293487547,45.39079543037812,-75.69309711456299,45.40525984235134");
      
      ByteArrayOutputStream ba = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(ba);
      printer.printMapMLDoc("application/xml", 0, "http://example.com", zoom ,requestBbox, out);
      out.flush();
      String result = ba.toString();
      assertNotNull(result);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setValidating(false);
      dbf.setNamespaceAware(false);
      try {
        DocumentBuilder db = dbf.newDocumentBuilder();
        Element nextLink = null;
        try  {
            Document d = db.parse(new ByteArrayInputStream(ba.toByteArray()));
            // message,expected,actual
            assertEquals("Paged MapML document should have 4 tile elements",4,d.getElementsByTagName("tile").getLength());
            NodeList links = d.getElementsByTagName("link");
            for (int i=0;i<links.getLength();i++) {
              if (links.item(i).getAttributes().getNamedItem("rel").getNodeValue().equalsIgnoreCase("next")) {
                nextLink = (Element)links.item(i);
                break;
              }
            }
            String href = nextLink != null? nextLink.getAttribute("href"):"";
            Pattern p = Pattern.compile("(.*)(start=(\\d*))(.*)");
            Matcher m = p.matcher(href);
            int start = 0;
            if (m.matches()) {
              start = Integer.parseInt(m.group(3));
            } else {
              fail("next link not found");
            }
            ba = new ByteArrayOutputStream();
            out = new PrintWriter(ba);
            printer.printMapMLDoc("application/xml", start, "http://example.com", zoom ,requestBbox, out);
            out.flush();
            result = ba.toString();
            assertNotNull(result);
            d = db.parse(new ByteArrayInputStream(ba.toByteArray()));
            assertEquals("Second page of MapML document should have 4 tile elements",4,d.getElementsByTagName("tile").getLength());
            links = d.getElementsByTagName("link");
            for (int i=0;i<links.getLength();i++) {
              Element lnk = (Element)links.item(0);
              if (lnk.getAttribute("rel").equals("next"))
                  fail("Should be no third page (no links at all) in second page of test MapML document");
            }
            
        } catch (Exception se) {
            fail("Error parsing MapML - document not well-formed");
        }
      } catch (ParserConfigurationException e) {}
  }
}
