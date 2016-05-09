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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.mapml.projections.Bounds;
import org.mapml.projections.LatLng;
import org.mapml.projections.Point;
import org.mapml.projections.TiledCRS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
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
    this.printer = new MapMLPrinter("OSMTILE");
    TiledCRS tcrs = printer.getTiledCRS();
    
    Point min = tcrs.project(new LatLng(-85.0511287798,-180));
    Point max = tcrs.project(new LatLng(85.0511287798,180));
    
    
    MapMLServiceBounds b = new MapMLServiceBounds(0,18, new Bounds(min,max), tcrs);
    printer.setServiceBounds(b);
    printer.setTileServers("a,b,c");
    String[] tileServerTemplates = {"http://{s}.example.com/tile/{z}/{x}/{y}/","http://none.foobar.com/tile/?z={z}&x={x}&y={y}/"};
    printer.setTileUrlTemplates(tileServerTemplates);
    String[] wmsServerTemplates = {"http://foo.example.com/wms/?W={w}&H={h}&BBOX={xmin},{ymin},{xmax},{ymax}","http://bar.example.com/wms/{xmin}/{ymin}/{xmax}/{ymax}/{w}/{h}/"};
    printer.setWmsUrlTemplates(wmsServerTemplates);
    tcrs.setPageSize(100);
    printer.setLicenseUrl("http://example.org/license");
    printer.setLicenseTitle("Tooltip info for license");
    printer.setLegendUrl("http://example.org/legend");
  }
  
  @Test
  public void testXml() {
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(ba);
    
    TiledCRS tcrs = printer.getTiledCRS();
    Point min = tcrs.latLngToPoint(new LatLng(45.39079543037812,-75.72056293487547),15);
    Point max = tcrs.latLngToPoint(new LatLng(45.40525984235134,-75.69309711456299),15);
    Bounds query = new Bounds(min, max);
    
    printer.printMapMLDoc("application/xml", 0, "http://example.com", 15, query, "OSMTILE", out);
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
          assertEquals("test MapML document must have 2 image elements", 2, d.getElementsByTagName("image").getLength());
      } catch (Exception se) {
          fail("Error parsing MapML - document not well-formed");
      }
    } catch (ParserConfigurationException e) {}
  }
  @Test
  public void testLicense() {
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(ba);
    TiledCRS tcrs = printer.getTiledCRS();
    Point min = tcrs.project(new LatLng(45.39079543037812,-75.72056293487547));
    Point max = tcrs.project(new LatLng(45.40525984235134,-75.69309711456299));
    
    int zoom = 15;
    
    Bounds query = tcrs.getPixelBounds(new Bounds(min, max), zoom);
    printer.printMapMLDoc("application/xml", 0, "http://example.com", zoom, query, "OSMTILE", out);
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
          assertTrue(licenseLink.getAttribute("title").equals("Tooltip info for license"));
      } catch (Exception se) {
          fail("Error parsing MapML - document not well-formed");
      }
    } catch (ParserConfigurationException e) {}
  }
  @Test
  public void testLegend() {
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(ba);
    TiledCRS tcrs = printer.getTiledCRS();
    Point min = tcrs.project(new LatLng(45.39079543037812,-75.72056293487547));
    Point max = tcrs.project(new LatLng(45.40525984235134,-75.69309711456299));
    
    int zoom = 15;
    
    Bounds query = tcrs.getPixelBounds(new Bounds(min, max), zoom);
    printer.printMapMLDoc("application/xml", 0, "http://example.com", zoom, query, "OSMTILE", out);
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
          Element legendLink = null;
          NodeList links = d.getElementsByTagName("link");
          for (int i=0;i<links.getLength();i++) {
              if (links.item(i).getAttributes().getNamedItem("rel").getNodeValue().equalsIgnoreCase("legend")) {
                  legendLink = (Element)links.item(i);
                  break;
              }
          }
          assertNotNull(legendLink);
          assertTrue(legendLink.getAttribute("href").equals("http://example.org/legend"));
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
      TiledCRS tcrs = printer.getTiledCRS();
      Point min = tcrs.project(new LatLng(45.39079543037812,-75.72056293487547));
      Point max = tcrs.project(new LatLng(45.40525984235134,-75.69309711456299));

      Bounds query = tcrs.getPixelBounds(new Bounds(min, max), zoom);
      Point centre = query.getCentre().divideBy(tcrs.getTileSize());
      double dist, prev = 0.0D;
      

      ByteArrayOutputStream ba = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(ba);
      printer.printMapMLDoc("application/xml", 0, "http://example.com", zoom ,query, "OSMTILE", out);
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
              long x = Long.parseLong(atts.getNamedItem("col").getNodeValue());
              long y = Long.parseLong(atts.getNamedItem("row").getNodeValue());
              Point tc = new Point(x+0.5,y+0.5);
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
  public void testBoundsOutsideServiceBounds() {

      int zoom = 0;
      // the min/max of the service at level 0 = 0,0 256,256, this extent is outside that
      Point min = new Point(-45,-45);
      Point max = new Point(-1,-1);
      Bounds query = new Bounds(min, max);
      
      ByteArrayOutputStream ba = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(ba);

      printer.printMapMLDoc("application/xml", 0, "http://example.com", zoom ,query, "OSMTILE", out);
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
            
            NodeList metaTags = d.getElementsByTagName("meta");
            int count = 0;
            for (int i=0;i<d.getElementsByTagName("meta").getLength();i++) {
              Node metaTag = metaTags.item(i);
              if (metaTag.getAttributes().getNamedItem("area") != null) {
                  count = Integer.parseInt(metaTag.getAttributes().getNamedItem("content").getNodeValue());
                  assertEquals("Tile area of test MapML document must be equal to 1",1,count);
              }
            }
            assertEquals("test MapML document must have 6 input elements",6,d.getElementsByTagName("input").getLength());
            assertEquals("test MapML document outside of service bounds must have 0 tile elements",0,d.getElementsByTagName("tile").getLength());
        } catch (Exception se) {
            fail("Error parsing MapML - document not well-formed");
        }
      } catch (ParserConfigurationException e) {}
  }
  //@Test
  public void testBoundsIntersectingServiceBounds() {
    fail();
  }
  @Test
  public void testNullBounds() {
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(ba);
    printer.printMapMLDoc("application/xml", 0, "http://example.com", 15, null, "OSMTILE", out);
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
          assertEquals("test MapML document must have 0 tile elements",0,d.getElementsByTagName("tile").getLength());
      } catch (Exception se) {
          fail("Error parsing MapML - document not well-formed");
      }
    } catch (ParserConfigurationException e) {}
  }
  
  public void testUnsupportedProjection() {
        // TODO move and refactor the following test to the Servlet level.
//        try {
//            request.setParameter("projection","foo");
//            projection = (String)QueryParam.projection.parse(request);
//            fail("Failed to reject invalid projection value");
//        } catch (RuntimeException e) {}
            // make sure the available projection is printed, not the 'default' OSMTILE value.
            // this allows the request to proceed where a bare URL is requested,
            // the service will reply with the necessary extent to allow the client
            // to proceed.
  }
          
  @Test
  public void testPaging() {
      String[] template = {"http://none.foobar.com/tile/{z}/{x}/{y}/"};
      printer.setTileUrlTemplates(template);
      int zoom = 15;
      
      TiledCRS tcrs = printer.getTiledCRS();
      tcrs.setPageSize(4);
      Point min = tcrs.latLngToPoint(new LatLng(45.39079543037812,-75.72056293487547),zoom);
      Point max = tcrs.latLngToPoint(new LatLng(45.40525984235134,-75.69309711456299),zoom);
      // this bbox should return 8 tiles
      Bounds query = new Bounds(min, max);
      
      ByteArrayOutputStream ba = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(ba);
      printer.printMapMLDoc("application/xml", 0, "http://example.com", zoom ,query, "OSMTILE", out);
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
            printer.printMapMLDoc("application/xml", start, "http://example.com", zoom ,query, "OSMTILE", out);
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
