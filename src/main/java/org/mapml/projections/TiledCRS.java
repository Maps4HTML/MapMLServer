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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.mapml.MapMLConstants;


public class TiledCRS {
  
    private final Transformation transformation;
    private final Projection projection;
    private final double[] scales;
    private final String name;
    private final int TILESIZE = 256;
    private final Point TILE_ORIGIN;
    private int pageSize = MapMLConstants.PAGESIZE;
    
    // the max.x,max.y of the Bounds member are integer numbers (though the defn of Bounds.* are double)
    // representing the maximum tile ordinate for the zoom level (the key).  min.x,
    // min.y (should/will) always be 0.
    private final HashMap<Integer, Bounds> tileBounds = new HashMap<>();
    
    private final Bounds bounds;
    
    public TiledCRS(String name) {
        TiledCRSParams parameters = TiledCRSConstants.tiledCRSDefinitions.get(name);
        if (parameters == null) {
          throw new RuntimeException("Definition for Tiled CRS not found: "+name);
        }
        // the bounds are in projected, but not transformed units.
        this.bounds = parameters.getBounds();
        // the tile origin is in projected, but not transformed units.
        this.TILE_ORIGIN = parameters.getOrigin();
        this.transformation = new Transformation(1, (-1 * parameters.getOrigin().x), -1, parameters.getOrigin().y);
        // the projection below is the proj4 / EPSG definition
        this.projection = new Projection(parameters.getCode());
        // the 'scales' are the reciprocal of the resolution of the zoom level
        // the resolution is in projected units, thus the scales are pixels per 
        // projected unit.
        this.scales = parameters.getScales();
        // the name is the name of the TiledCRS, which is equal to the name of 
        // the 'projection' in the MapML projection registry*.
        this.name = name;
        // calculate the maximum tile coordinates on a per-zoom-level basis.
        init();
    }
    private void init() {
      // establish the maximum tile coordinates for each zoom value
      // we know that the minimum x and y are 0 at 85.0511D North and 180.0 West

      for (int zoom = 0; zoom < this.scales.length ;zoom++) {
        Bounds pb = new Bounds(
            this.transformation.transform(this.bounds.min, this.scales[zoom]), 
            this.transformation.transform(this.bounds.max, this.scales[zoom])
        ) ;
        // consider if the min should be automatically bumped up to 0,0 if 
        // it is below that.  If it is below that, it might indicate an
        // error in the TiledCRSConstants parameter for BOUNDS, possibly
        // due to converting from a theoretical boundary in lat long to projected
        // coordinates e.g. -90,-180 90,180 instead of -85.011,-180.0 85.011,180
        
        // in any case at the current time the tileBounds values are only used
        // as an upper limit (i.e. only the tb.max is used (0 is taken to be min). 
        Bounds tb = new Bounds(
                pb.min.divideBy(TILESIZE).floor(),
                pb.max.divideBy(TILESIZE).floor());
        this.tileBounds.put(zoom, tb);
      }
    }
    
    /**
     * For testing purposes need to be able to set the pagesize. 
     * @param size 
     */
    public void setPageSize(int size) {
        pageSize = size;
    }
    
    public int getPageSize() {
        return pageSize;
    }

    public String getName() {
      return name;
    }
    
    public int getTileSize() {
        return this.TILESIZE;
    }
    
    public Point latLngToPoint(LatLng latlng, int zoom) {
        Point p = this.projection.project(latlng);
        return this.transformation.transform(p, this.scales[zoom]);
    }
    
    public LatLng pointToLatLng(Point p, int zoom) {
        Point untransformedPoint = this.transformation.untransform(p, this.scales[zoom]);
        return this.projection.unproject(untransformedPoint);
    }
    /**
     * 
     * @param bounds the LatLngBounds that should be transformed to projected, scaled bounds
     * @param zoom the zoom scale at which to transform the bounds
     * @return  the projected and transformed LatLngBounds into pixel coordinates.
     */
    public Bounds getPixelBounds(LatLngBounds bounds,int zoom) {
        LatLng sw = bounds.southWest;
        LatLng ne = bounds.northEast;
        return new Bounds(latLngToPoint(sw,zoom),latLngToPoint(ne,zoom));
    }
    /**
     * 
     * @param bounds - projected, but not scaled bounds
     * @param zoom - the scale at which to transform the bounds
     * @return pixel bounds transformation of the given bounds
     */
    public Bounds getPixelBounds(Bounds bounds, int zoom) {
      Point min = this.transformation.transform(bounds.min, this.scales[zoom]).round();
      Point max = this.transformation.transform(bounds.max, this.scales[zoom]).round();
      return new Bounds(min, max);
    }
    // convenience methods
    public Point project(LatLng latLng) {
      return this.projection.project(latLng);
    }
    public LatLng unproject(Point point) {
      return this.projection.unproject(point);
    }
    /**
     * Count the width of the *tile* bounds at the given zoom level in integral tile units.
     * @param zoom integer zoom level at which to calculate the width
     * @param bounds the bounds for which the calculation/conversion should be done
     * @return long the number of tiles wide the bounds is at the zoom level
     */
    protected long tileWidth(int zoom, Bounds bounds) {
        if (zoom == -1 || bounds == null) return 0;
        return (long)bounds.max.x+1 - (long)bounds.min.x;
    }
    
    public long tileCount(int zoom, Bounds bounds) {
        if (zoom == -1 || bounds == null) return 0;

        Point min = bounds.min.divideBy(TILESIZE).floor();
        Point max = bounds.max.divideBy(TILESIZE).floor();

        // integer coordinate system, bump max values up to next increment
        long width = (long) (max.x+1 - min.x);
        long height = (long) (max.y+1 - min.y);
        return width * height;
    }
    
    public List<TileCoordinates> getTilesForExtent(Bounds extent, int zoom, long start) {
        
        // the extent must be expressed in projected, scaled units
        Bounds pb = extent;
        // the min/max in decimal tiles truncated to the next lower integer tile ordinate
        Bounds tb = new Bounds(
                pb.min.divideBy(TILESIZE).floor(),
                pb.max.divideBy(TILESIZE).floor());
        long width = tileWidth(zoom, tb);
        List<TileCoordinates> tiles = new ArrayList<>();
        for (long i = (start > 0?(long)tb.min.y+start/width:(long)tb.min.y); i <= tb.max.y; i++) {
            for (long j = (start > 0?(long)tb.min.x+(start % width):(long)tb.min.x); j <= tb.max.x;j++) {
                if (tiles.size() < pageSize) {
                  if (i >= 0 && i <= tileBounds.get(zoom).max.y && j >= 0 && j < tileBounds.get(zoom).max.x) {
                    tiles.add(new TileCoordinates(j, i, zoom));
                  }
                } else {
                  break;
                }
            }
        }
        // the centre of the extent in decimal tiles... not truncated
        Point centre  = pb.getCentre().divideBy(TILESIZE);
        Collections.sort(tiles, new TileComparator(centre));
        return tiles;
    }
    public List<TileCoordinates> getTilesForExtent(LatLngBounds extent, int zoom, long start) {
      
        Bounds pb = getPixelBounds(extent, zoom);
        return getTilesForExtent(pb, zoom, start);
    }
  /**
   * Compares two tile coordinates and ranks them by distance from the constructed
   * center point.
   */
  protected class TileComparator implements Comparator<TileCoordinates> {
      private final Point centre;
      public TileComparator(Point centre) {
        this.centre = centre;
      }
      @Override
      public int compare(TileCoordinates t1, TileCoordinates t2) {
          // add 0.5 to ordinates to calculate distance to tile centres
          Double d1 = this.centre.distanceTo(new Point(t1.x+0.5,t1.y+0.5));
          Double d2 = this.centre.distanceTo(new Point(t2.x+0.5,t2.y+0.5));
          return d1.compareTo(d2);
      }
  }
}
