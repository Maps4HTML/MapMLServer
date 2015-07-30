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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.mapml.util.Bbox;

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
  private static int pageSize = 100;
  private MapMLServiceBounds serviceBounds;
  private String[] tileUrlTemplates;
  private final HashSet<String> tileServers = new HashSet<>();
  private final HashMap<Integer, TileCoordinates> maxima = new HashMap<>();
  private String licenseUrl;
  private String licenseText;
  private String licenseTitle;


  public MapMLPrinter() {
    init();
  }
  /**
   * Set up the maximum tile coordinates on a per-zoom-level basis
   */
  private void init() {
    // establish the maximum tile coordinates for each zoom value
    // we know that the minimum x and y are 0 at 85.0511D North and 180.0 West
    for (int zoom = 0; zoom < 21 ;zoom++) {
      maxima.put(zoom, getTileCoordinates(zoom, -85.0511D, 180.0D));
    }
  }
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
  /**
   * For testing purposes need to be able to set the pagesize. 
   * @param size 
   */
  protected void setPageSize(int size) {
      pageSize = size;
  }
  /**
   * The text that will be reflected in the link[@rel=license].
   * @param text 
   */
  public void setLicenseText(String text) {
      this.licenseText = text;
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
   * Print a mapml document on the output, given the parameters
   * @param responseType the mime type to reflect in the &lt;meta&gt element
   * @param start offset, given tiles are row,col ordered
   * @param base base URI to serialize as base element
   * @param zoom zoom level at which the tile references are generated
   * @param bbox the map extent to use for generating tile references in/touching
   * @param out the PrintWriter on which to print.
   */
  public void printMapMLDoc(String responseType, long start, String base, int zoom, Bbox bbox, PrintWriter out) {
      long tileCount = bbox == null?0:tileCount(zoom,bbox);
      // check that start is an integral multiple of pageSize
      // check that start is less than tileCount
      // check that next is less than tileCount
      long next = 0L;
      if (tileCount > pageSize) {
          next = start + pageSize;
          if (next > tileCount) 
              next = 0L;
      }
    
      out.print("<mapml><head><title>Tile references for extent@value, sorted by distance from centre</title>");
      out.print("<meta http-equiv=\"Content-Type\" content=\""+responseType+"\"/>");
      out.print("<meta charset=\"utf-8\"/>");
      out.print("<meta name=\"count\" content=\""+tileCount+"\"/>");
      out.print("<base href=\""+base+"\"/>");
      out.print("<link rel=\"license\" href=\""+licenseUrl+"\" text=\""+licenseText+"\" title=\""+licenseTitle+"\"/>");
      out.print("</head><body>");
      if (bbox == null || !serviceBounds.intersects(zoom, bbox)) {
          out.print(getExtentElement(base, -1, null));
      } else {
          out.print(getExtentElement(base, zoom, bbox));
          if (next > 0 && next != tileCount) {
              out.print("<link rel=\"next\" href=\""+base+"?bbox="+bbox+"&amp;projection=OSMTILE&amp;zoom="+zoom+"&amp;start="+next+"\" type=\"text/mapml\"/> ");
          }
          out.print(getTileElements(zoom, bbox, start));
      }
      out.print("</body></mapml>");
  }
  /**
   * Gets the string representing the extent of the service, reflecting the
   * values of the zoom and bbox for the request in the value="" attributes.
   * 
   * @param url the URI at which the service is available
   * @param zoom the zoom for which the extent is to be generated
   * @param bbox the value to use for the bbox 
   * @return a String <extent> element for the service
   */
  protected String getExtentElement(String url, int zoom, Bbox bbox) {
    
    String userWest = bbox != null  ? " value=\"" + bbox.getWest()  + "\"" :"";
    String userSouth = bbox != null ? " value=\"" + bbox.getSouth() + "\"" :"";
    String userEast = bbox != null  ? " value=\"" + bbox.getEast()  + "\"" :"";
    String userNorth = bbox != null ? " value=\"" + bbox.getNorth() + "\"" :"";
    String userZoom = zoom != -1    ? " value=\"" + zoom            + "\"" :"";
    
    String minLon = " min=\"" + serviceBounds.getWest() + "\"";
    String minLat = " min=\"" + serviceBounds.getSouth()+ "\"";
    String maxLon = " max=\"" + serviceBounds.getEast() + "\"";
    String maxLat = " max=\"" + serviceBounds.getNorth()+ "\"";
    
    String minZm = " min=\""  + serviceBounds.getMinZoom()  + "\"";
    String maxZm = " max=\""  + serviceBounds.getMaxZoom()  + "\"";
    
    String extent = 
      "<extent units=\"WGS84\" "
      +"action=\""+url+"\" "
      +"method=\"get\" enctype=\"application/x-www-form-urlencoded\">"
        +"<input name=\"xmin\" type=\"xmin\" " + userWest  + minLon  + maxLon  + "/>"
        +"<input name=\"ymin\" type=\"ymin\" " + userSouth + minLat  + maxLat  + "/>"
        +"<input name=\"xmax\" type=\"xmax\" " + userEast  + minLon  + maxLon  + "/>"
        +"<input name=\"ymax\" type=\"ymax\" " + userNorth + minLat  + maxLat  + "/>"
        +"<input name=\"zoom\" type=\"zoom\" " + userZoom  + minZm   + maxZm   + "/>"
        +"<input name=\"projection\" type=\"projection\" value=\"OSMTILE\"/>"
      +"</extent>";
    return extent;
  }
  /**
   * Generate <tile> elements for the requested parameters, using the tileServers
   * and tileUrlTemplates with which the class is configured.
   * 
   * @param zoom the zoom of the request
   * @param bbox the extent of the request
   * @param start offset 
   * @return the String value of the set of <tile> elements
   */
  protected String getTileElements(int zoom, Bbox bbox, long start) {
    StringBuilder tes = new StringBuilder();
    Iterator<String> i = tileServers.iterator();
    String s = "";
    List<TileCoordinates> tiles = getTilesInBbox(zoom, bbox,start);
    
    for (TileCoordinates t : tiles) {
       if (i.hasNext()) {
          s = i.next();
          if (!i.hasNext())
            i = tileServers.iterator();
       }
       for (String template : this.tileUrlTemplates) {
          String src = template
             .replaceFirst("\\{s\\}", s+"").replaceFirst("\\{z\\}", t.z+"")
             .replaceFirst("\\{y\\}", t.y+"").replaceFirst("\\{x\\}", t.x+"")
             .replaceAll("&", "&amp;");
          tes.append("<tile ")
           .append("x=\"").append(t.x).append("\" y=\"").append(t.y).append("\" ")
           // .append("distance=\"").append(c.distanceTo(t)).append("\" ")
           .append("src=\"").append(src).append("\"/>");
       }
    }
    return tes.toString();
  }
  /**
   * Get the integer TileCoordinates of the requested zoom,lat,long location.
   * 
   * @param zoom level at which to do the conversion
   * @param latitude double latitude of the point to convert
   * @param longitude double longitude of the point to convert
   * @return A point, converted to TileCoordinate (integral) space
   */
  protected  TileCoordinates getTileCoordinates(int zoom, double latitude, double longitude) {
    // see http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Lon..2Flat._to_tile_numbers
    double lat = Math.toRadians(latitude);
    double lon = Math.toRadians(longitude);
    
    double x = lon;
    double y = Math.log( Math.tan(lat)+(1.0/Math.cos(lat)) );
    x = (1.0 + ( x / Math.PI)) / 2.0;
    y = (1.0 - ( y / Math.PI)) / 2.0;
    long tilex = Math.round(Math.floor(Math.pow(2, zoom) * x));
    long tiley = Math.round(Math.floor(Math.pow(2, zoom) * y));
    return new TileCoordinates(zoom, tilex, tiley);
  }
  /**
   * Get the coordinates, in terms of the coorinate reference system used by tiles
   * i.e. convert the WGS84 coordinates to the integral coordinate system 
   * for the given zoom level, returned as a point in double precision zoom,x,y 
   * format.  This allows for calculation of distance from a non-integer central 
   * point to integer tile reference locations, such that collections may be 
   * ordered by distance from map extent's center.
   * 
   * @param zoom integer zoom level at which to do the conversion
   * @param latitude double latitude of the point to convert
   * @param longitude double longitude of the point to convert
   * @return a point in TileDecimalCoordinate space
   */
  protected TileDecimalCoordinates getTileDecimalCoordinates(int zoom, double latitude, double longitude) {
    // see http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Lon..2Flat._to_tile_numbers
    double lat = Math.toRadians(latitude);
    double lon = Math.toRadians(longitude);
    
    double x = lon;
    double y = Math.log( Math.tan(lat)+(1.0/Math.cos(lat)) );
    x = (1.0 + ( x / Math.PI)) / 2.0;
    y = (1.0 - ( y / Math.PI)) / 2.0;
    double xd = Math.pow(2, zoom) * x;
    double yd  = Math.pow(2, zoom) * y;
    return new TileDecimalCoordinates(zoom, xd, yd);
  }
  /**
   * Counts the number of tiles at the specified zoom level which intersect the
   * given bbox.
   * 
   * @param zoom integer zoom level at which count tiles
   * @param bbox Bbox bbox the extent within which to count tiles
   * @return long number of tiles
   */
  protected long tileCount(int zoom, Bbox bbox) {
    if (zoom == -1 || bbox == null) return 0;
    // the tile coordinate system has +y downwards, so that is why min/max are defined this way
    TileCoordinates min = getTileCoordinates(zoom,bbox.getNorth(),bbox.getWest());
    TileCoordinates max = getTileCoordinates(zoom,bbox.getSouth(), bbox.getEast());
    // integer coordinate system, bump max values up to next increment
    long width = max.x+1 - min.x;
    long height = max.y+1 - min.y;
    return width * height;
  }
  /**
   * Count the width of the bbox at the given zoom level in integral tile units.
   * @param zoom integer zoom level at which to calculate the width
   * @param bbox the bbox for which the calculation/conversion should be done
   * @return long the number of tiles wide the bbox is at the zoom level
   */
  protected long tileWidth(int zoom, Bbox bbox) {
      if (zoom == -1 || bbox == null) return 0;
      TileCoordinates min = getTileCoordinates(zoom,bbox.getNorth(),bbox.getWest());
      TileCoordinates max = getTileCoordinates(zoom,bbox.getSouth(), bbox.getEast());
      return max.x+1 - min.x;
  }
  /**
   * Get a paged List of tiles in terms of their integral x,y coordinates for a given
   * bbox at the given zoom level, optionally offset by some number of tiles.
   * @param zoom
   * @param bbox
   * @param start
   * @return a "page" / List of integer tile coordinates in the requested extent
   */
  protected List<TileCoordinates> getTilesInBbox(int zoom, Bbox bbox, long start) {
    TileCoordinates min = getTileCoordinates(zoom,bbox.getNorth(),bbox.getWest());
    TileCoordinates max = getTileCoordinates(zoom,bbox.getSouth(), bbox.getEast());
    TileDecimalCoordinates centre = getTileDecimalCoordinates(zoom,bbox.getEnvelope().centre().y,bbox.getEnvelope().centre().x);
    long width = tileWidth(zoom, bbox);
    List<TileCoordinates> tiles = new ArrayList<>();
    for (long i=(start > 0?min.y+start/width:min.y); i <= max.y; i++) {
      for (long j = start > 0?min.x+(start % width):min.x;j <=max.x; j++) {
        if (tiles.size() < pageSize) {
          // constructor args: zoom,x,y 
          if (i >= 0 && i <= maxima.get(zoom).y && j >= 0 && j < maxima.get(zoom).x) {
            tiles.add(new TileCoordinates(zoom,j, i));
          }
        } else {
          break;
        }
      }
    }
    // note that where > 1 url templates have been provided, there will be ties
    // there is no way at present to force the order of ties.  Observed is that
    // the second url template generates a tile reference that places second
    // in the sorting.  Presumably this fortuitous bounce applies to two or more
    // templates as well.  If not, will have to figure out how to weight / rank
    // ties in this sort.
    Collections.sort(tiles, new TileComparator(centre));
    return tiles;
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
  /**
   * Represents a 'coordinate' in a tiled coordinate system, where the origin
   * is at the upper left, and x is postive to the right, y is positive down.
   * The left/top edge is the coordinate value.
   * 
   */
  protected class TileCoordinates {
      public int z;
      public long x;
      public long y;
      public TileCoordinates(int zoom, long x, long y) {
        this.z = zoom;
        this.x = x;
        this.y = y;
      }
  }

}
