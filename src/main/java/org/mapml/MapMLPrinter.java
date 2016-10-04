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

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.mapml.projections.Bounds;
import org.mapml.projections.Point;
import org.mapml.projections.TileCoordinates;
import org.mapml.projections.TiledCRS;

/**
 * This class prints a MapML tile reference document.  It is created so that
 * the individual methods can be unit tested without setting up a mock 
 * servlet environment. It is intended to be instantiated once, initialized
 * with system parameters (bounds, tile service URI templates, servers),
 * and thereafter invoked to pring the mapml response document based on
 * required parameters.  The class is set up to limit the number of tile
 * references it will print before issuing a 'next' link.  This value
 * depends on how big the device is that one wants to serve.  If you were
 * serving a TV screen, it might be reasonable to bump it up above 100 (pageSize).
 * 
 * @author Peter Rushforth
 */

public class MapMLPrinter {
  private MapMLServiceBounds serviceBounds;
  private final TiledCRS tiledCRS;
  private String[] tileUrlTemplates;
  private final HashSet<String> tileServers = new HashSet<>();
  private String licenseUrl;
  private String licenseTitle;
  private String legendUrl;
  private String[] wmsUrlTemplates;
  private String title;

  public MapMLPrinter(String projection) {
    this.tiledCRS = new TiledCRS(projection);
  }
  /**
   * Set up the maximum tile coordinates on a per-zoom-level basis
   */
//  private void init() {
//    // establish the maximum tile coordinates for each zoom value
//    // we know that the minimum x and y are 0 at 85.0511D North and 180.0 West
//    for (int zoom = 0; zoom < 21 ;zoom++) {
//      maxima.put(zoom, getTileCoordinates(zoom, -85.0511D, 180.0D));
//    }
//  }
  
  
  /**
   * Set the service bounds (min/max zoom, extent)
   * @param serviceBounds 
   */
  public void setServiceBounds(MapMLServiceBounds serviceBounds) {
    this.serviceBounds = serviceBounds;
  }
  /**
   * Set the URI templates which will be used to generate URLs for tiles. The
   * order of the templates dictates the 'z-order' of the generated tile
   * references, with the lower array values being underneath higher values, i.e.
   * lower array values will be earlier in document order for a given tile in
   * the output MapML document.
   * @param tileUrlTemplates 
   */
  public void setTileUrlTemplates(String[] tileUrlTemplates) {
    this.tileUrlTemplates = tileUrlTemplates;
  }
  /**
   * Servers sometimes want to distribute load across multiple host names. If
   * the tileUrlTemplates have a server template to be filled in, ensure that
   * you provide a servers list, otherwise you will not generate correct
   * tile references.
   * @param servers comma-separated list of hostnames to rotate through.
   */
  public void setTileServers(String servers) {
    if (servers != null) {
      this.tileServers.addAll(Arrays.asList(servers.split(",")));
    }
  }
  public String[] getWmsUrlTemplates() {
    return wmsUrlTemplates;
  }

  public void setWmsUrlTemplates(String[] wmsUrlTemplates) {
    this.wmsUrlTemplates = wmsUrlTemplates;
    for(int i=0;i<this.wmsUrlTemplates.length;i++) {
        this.wmsUrlTemplates[i] = this.wmsUrlTemplates[i].trim();
    }
  }

  public void setTitle(String title) {
    this.title = title;
  }
  
  private int getPageSize() {
      return this.tiledCRS.getPageSize();
  }
  /**
   * The text that will be reflected in link[@rel=license]/@title.
   * @param text 
   */
  public void setLicenseTitle(String text) {
      this.licenseTitle = text;
  }
  /**
   * The URL to the license by virtue of which these tiles are served.
   * @param url 
   */
  public void setLicenseUrl(String url) {
      this.licenseUrl = url;
  }
  /**
   * The URL to the legend resource.
   * @param url 
   */
  public void setLegendUrl(String legendUrl) {
    this.legendUrl = legendUrl.replaceAll("&", "&amp;");
  }
  public TiledCRS getTiledCRS() {
      return this.tiledCRS;
  }
  /**
   * Print a mapml document on the output, given the parameters
   * @param responseType the mime type to reflect in the &lt;meta&gt element
   * @param start offset, given tiles are row,col ordered
   * @param base base URI to serialize as base element
   * @param zoom zoom level at which the tile references are generated
   * @param bounds the map extent to use for generating tile references in/touching
   * @param out the PrintWriter on which to print.
   */
  public void printMapMLDoc(String scheme, String responseType, long start, String base, int zoom, Bounds bounds, String projection, PrintWriter out) {
      long tileCount = bounds == null?0:this.tiledCRS.tileCount(zoom, bounds);
      // check that start is an integral multiple of pageSize
      // check that start is less than tileCount
      // check that next is less than tileCount
      long next = 0L;
      if (tileCount > getPageSize()) {
          next = start + getPageSize();
          if (next > tileCount) 
              next = 0L;
      }
    
      out.print("<mapml><head><title>"+title+"</title>");
      out.print("<meta http-equiv=\"Content-Type\" content=\""+responseType+"\"/>");
      out.print("<meta charset=\"utf-8\"/>");
      out.print("<meta name=\"projection\" content=\""+projection+"\"/>");
      out.print("<meta name=\"zoom\" content=\""+zoom+"\"/>");
      out.print("<meta name=\"area\" content=\""+tileCount+"\"/>");
      out.print("<base href=\""+base+"\"/>");
      out.print("<link rel=\"license\" href=\""+this.licenseUrl+"\" title=\""+this.licenseTitle+"\"/>");
      if (this.legendUrl != null) {
          out.print("<link rel=\"legend\" href=\""+this.legendUrl+"\"/>");
      }
      out.print("</head><body>");
      if (bounds == null || !serviceBounds.intersects(zoom, bounds)) {
          out.print(getExtentElement(base, zoom, bounds, projection));
      } else {
          out.print(getExtentElement(base, zoom, bounds, projection));
          if (next > 0 && next != tileCount) {
              out.print("<link rel=\"next\" href=\""+base+"?xmin="+bounds.getMin().x+"&amp;ymin="+bounds.getMin().y+"&amp;xmax="+bounds.getMax().x+"&amp;ymax="+bounds.getMax().y+"&amp;projection=OSMTILE&amp;zoom="+zoom+"&amp;start="+next+"\" type=\"text/mapml\"/> ");
          }
          // a servlet instance can serve tiles and/or wms request urls
          if (this.tileUrlTemplates != null) {
              out.print(getTileElements(scheme, zoom, bounds, start));
      }
          if (this.wmsUrlTemplates != null) {
              out.print(getImageElements(scheme, bounds, zoom));
          }
      }
      out.print("</body></mapml>");
  }
  protected String getImageElements(String scheme, Bounds bounds, int zoom) {
    StringBuilder images = new StringBuilder();
    
    if (wmsUrlTemplates.length != 0) {
      long width = (long)(bounds.getMax().x - bounds.getMin().x);
      long height = (long)(bounds.getMax().y - bounds.getMin().y);
      Point min = tiledCRS.untransform(bounds.getMin(), zoom);
      Point max = tiledCRS.untransform(bounds.getMax(), zoom);
      double xmin,ymin,xmax,ymax;
      xmin = Math.min(min.x, max.x);
      ymin = Math.min(min.y, max.y);
      xmax = Math.max(min.x, max.x);
      ymax = Math.max(min.y,max.y);
       for (String template : wmsUrlTemplates) {
          String src = template
             .replaceFirst("\\{scheme\\}", scheme+"")
             .replaceFirst("\\{xmin\\}", xmin+"").replaceFirst("\\{ymin\\}", ymin+"")
             .replaceFirst("\\{xmax\\}", xmax+"").replaceFirst("\\{ymax\\}", ymax+"")
             .replaceFirst("\\{w\\}", width+"").replaceFirst("\\{h\\}", height+"")
             .replaceAll("&", "&amp;");
          images.append("<image ")
           .append("src=\"").append(src).append("\"/>");
       }
       return images.toString();
    }
    return "";
  }
  /**
   * Gets the string representing the extent of the service, reflecting the
   * values of the zoom and bounds for the request in the value="" attributes.
   * 
   * @param url the URI at which the service is available
   * @param zoom the zoom for which the extent is to be generated
   * @param bounds the value to use for the extent
   * @return a String <extent> element
   */
  protected String getExtentElement(String url, int zoom, Bounds bounds, String projection) {
    
    String userXmin = bounds != null  ? " value=\"" + bounds.getMin().x  + "\"" :"";
    String userYmin = bounds != null  ? " value=\"" + bounds.getMin().y + "\"" :"";
    String userXmax = bounds != null  ? " value=\"" + bounds.getMax().x  + "\"" :"";
    String userYmax = bounds != null  ? " value=\"" + bounds.getMax().y + "\"" :"";
    int z = zoom != -1 ? zoom : serviceBounds.getMinZoom();
    String userZoom = " value=\"" + z + "\"";
    
    String minX = " min=\"" + serviceBounds.getPixelBounds(z).getMin().floor().x +  "\"" ;
    String minY = " min=\"" + serviceBounds.getPixelBounds(z).getMin().floor().y +  "\"" ;
    String maxX = " max=\"" + serviceBounds.getPixelBounds(z).getMax().floor().x +  "\"" ;
    String maxY = " max=\"" + serviceBounds.getPixelBounds(z).getMax().floor().y +  "\"" ;
    
    String minZm = " min=\""  + serviceBounds.getMinZoom()  + "\"";
    String maxZm = " max=\""  + serviceBounds.getMaxZoom()  + "\"";
    // TODO DO NOT RETURN THE DEFAULT PROJECTION OSMTILE HERE IF IT IS NOT AVAILABLE
    // FROM THE PRINTER
    String extent = 
      "<extent units=\""+projection+"\" "
      +"action=\""+url+"\" "
      +"method=\"get\" enctype=\"application/x-www-form-urlencoded\">"
        +"<input name=\"xmin\" type=\"xmin\" " + userXmin  + minX  + maxX  + "/>"
        +"<input name=\"ymin\" type=\"ymin\" " + userYmin + minY  + maxY  + "/>"
        +"<input name=\"xmax\" type=\"xmax\" " + userXmax  + minX  + maxX  + "/>"
        +"<input name=\"ymax\" type=\"ymax\" " + userYmax + minY  + maxY  + "/>"
        +"<input name=\"zoom\" type=\"zoom\" " + userZoom  + minZm   + maxZm   + "/>"
        +"<input name=\"projection\" type=\"projection\" value=\""+ projection +"\"/>"
      +"</extent>";
    return extent;
  }
  /**
   * Generate <tile> elements for the requested parameters, using the tileServers
   * and tileUrlTemplates with which the class is configured.
   * 
   * @param zoom the zoom of the request
   * @param bounds the extent of the request in projected, *scaled* units
   * @param start offset 
   * @return the String value of the set of <tile> elements
   */
  protected String getTileElements(String scheme, int zoom, Bounds bounds, long start) {
    StringBuilder tes = new StringBuilder();
    Iterator<String> i = tileServers.iterator();
    String s = "";
    List<TileCoordinates> tiles = this.tiledCRS.getTilesForExtent(bounds, zoom, start);
    
    for (TileCoordinates t : tiles) {
       if (i.hasNext()) {
          s = i.next();
          if (!i.hasNext())
            i = tileServers.iterator();
       }
       for (String template : this.tileUrlTemplates) {
          String src = template
             .replaceFirst("\\{scheme\\}", scheme+"")
             .replaceFirst("\\{s\\}", s+"").replaceFirst("\\{z\\}", t.z+"")
             .replaceFirst("\\{y\\}", t.y+"").replaceFirst("\\{x\\}", t.x+"")
             .replaceAll("&", "&amp;");
          tes.append("<tile ")
           .append("col=\"").append(t.x).append("\" row=\"").append(t.y).append("\" ")
           // .append("distance=\"").append(c.distanceTo(t)).append("\" ")
           .append("src=\"").append(src).append("\"/>");
       }
    }
    return tes.toString();
  }
  /**
   * Compares two tile coordinates and ranks them by distance from the constructed
   * center point.
   */
  protected class TileComparator implements Comparator<TileCoordinates> {
      private final TileDecimalCoordinates centre;
      TileComparator(TileDecimalCoordinates centre) {
        this.centre = centre;
      }
      @Override
      public int compare(TileCoordinates t1, TileCoordinates t2) {
          Double d1 = this.centre.distanceTo(t1);
          Double d2 = this.centre.distanceTo(t2);
          return d1.compareTo(d2);
      }
  }
  /**
   * Represents a position as an exact decimal in terms of the tile
   * units at the specified zoom level.  Each zoom level has different numerical
   * bounds, for instance zoom level 0 has a maximum x / y of 1.0D, whereas 
   * zoom level 18 has greater extents.  Instances of this class should be
   * created with the getTileDecimalCoordinates(zoom, lat, lon) method, so 
   * as to avoid confusion over lat mapsto y, lon mapsto x business.
   */
  protected class TileDecimalCoordinates {
      int zoom;
      double x;
      double y;
      /**
       * Generate a position in  the numerical domain of the coordinate system
       * at a particular zoom level
       * @param zoom
       * @param x
       * @param y
       */
      public TileDecimalCoordinates(int zoom, double x, double y) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
      }
      public double distanceTo(TileCoordinates tile) {
        double dx = (tile.x+0.5) - this.x;
        double dy = (tile.y+0.5) - this.y;
        return Math.sqrt(dx*dx + dy*dy);
      }
  }
}
