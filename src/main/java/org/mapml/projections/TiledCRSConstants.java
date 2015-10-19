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

import java.util.HashMap;

public class TiledCRSConstants {
    public static final HashMap<String, TiledCRSParams> tiledCRSDefinitions = new HashMap<>();

    static {
        final String OSMTILE_NAME = "OSMTILE";
        final String OSMTILE_CODE = "EPSG:3857";
        Projection proj = new Projection(OSMTILE_CODE);
        final Bounds OSMTILE_BOUNDS = 
                new Bounds(proj.project(new LatLng(-85.0511287798,-180)), proj.project(new LatLng(85.0511287798,180)));
        final int OSMTILE_TILE_SIZE = 256;
        final double[] OSMTILE_SCALES = {
                  /* "scale" is the reciprocal of "resolution", per Proj4Leaflet.js */
                     1 / 156543.0339D,
                     1 / 78271.51695D,
                     1 / 39135.758475D,
                     1 / 19567.8792375D,
                     1 / 9783.93961875D,
                     1 / 4891.969809375D,
                     1 / 2445.9849046875D,
                     1 / 1222.9924523438D,
                     1 / 611.49622617188D,
                     1 / 305.74811308594D,
                     1 / 152.87405654297D,
                     1 / 76.437028271484D,
                     1 / 38.218514135742D,
                     1 / 19.109257067871D,
                     1 / 9.5546285339355D,
                     1 / 4.7773142669678D,
                     1 / 2.3886571334839D,
                     1 / 1.1943285667419D,
                     1 / 0.59716428337097D   
        };
        final Point OSMTILE_TILE_ORIGIN = new Point(-20037508.342787D,20037508.342787D);
        tiledCRSDefinitions.put(OSMTILE_NAME, new TiledCRSParams(OSMTILE_NAME, OSMTILE_CODE, OSMTILE_BOUNDS, OSMTILE_TILE_SIZE, OSMTILE_TILE_ORIGIN, OSMTILE_SCALES));
        
        final String CBMTILE_NAME = "CBMTILE";
        final String CBMTILE_CODE = "EPSG:3978";
        final Bounds CBMTILE_BOUNDS = 
                new Bounds(new Point(-7786476.885838887,-5153821.09213678),new Point(7148753.233541353,7928343.534071138));
//                new Bounds(new Point(-4282638.06150141,-5153821.09213678),new Point(4852210.1755664,4659267.000000001));
        final int CBMTILE_TILE_SIZE = 256;
        final double[] CBMTILE_SCALES = {
                 1 / 38364.660062653464D, 
                 1 / 22489.62831258996D, 
                 1 / 13229.193125052918D, 
                 1 / 7937.5158750317505D, 
                 1 / 4630.2175937685215D, 
                 1 / 2645.8386250105837D,
                 1 / 1587.5031750063501D,
                 1 / 926.0435187537042D, 
                 1 / 529.1677250021168D, 
                 1 / 317.50063500127004D, 
                 1 / 185.20870375074085D, 
                 1 / 111.12522225044451D, 
                 1 / 66.1459656252646D, 
                 1 / 38.36466006265346D, 
                 1 / 22.48962831258996D,
                 1 / 13.229193125052918D,
                 1 / 7.9375158750317505D, 
                 1 / 4.6302175937685215D    
        };
        final Point CBMTILE_TILE_ORIGIN = new Point(-34655800D, 39310000D);
        tiledCRSDefinitions.put(CBMTILE_NAME, new TiledCRSParams(CBMTILE_NAME, CBMTILE_CODE, CBMTILE_BOUNDS, CBMTILE_TILE_SIZE, CBMTILE_TILE_ORIGIN, CBMTILE_SCALES));
        
        /* Arctic Polar Stereographic, origin and scales defined by map service at http://maps8.arcgisonline.com/arcgis/rest/services/Arctic_Polar_Ocean_Base/MapServer */
        final String APSTILE_NAME = "APSTILE";
        final String APSTILE_CODE = "EPSG:5936";
        final Bounds APSTILE_BOUNDS = new Bounds(new Point(-28567784.109254867D, -28567784.109254755D),new Point( 32567784.109255023D,32567784.10925506D));
        final int    APSTILE_TILE_SIZE = 256;
        final double[] APSTILE_SCALES = {
                  /* "scale" is the reciprocal of "resolution", per Proj4Leaflet.js */
                  1 / 238810.813354D,
                  1 / 119405.406677D,
                  1 / 59702.7033384999D,
                  1 / 29851.3516692501D,
                  1 / 14925.675834625D,
                  1 / 7462.83791731252D,
                  1 / 3731.41895865639D,
                  1 / 1865.70947932806D,
                  1 / 932.854739664032D,
                  1 / 466.427369832148D,
                  1 / 233.213684916074D,
                  1 / 116.606842458037D,
                  1 / 58.3034212288862D,
                  1 / 29.1517106145754D,
                  1 / 14.5758553072877D,
                  1 / 7.28792765351156D,
                  1 / 3.64396382688807D,
                  1 / 1.82198191331174D,
                  1 / 0.910990956788164D,
                  1 / 0.45549547826179D
        };
        final Point APSTILE_TILE_ORIGIN = new Point(-28567784.109255D,32567784.109255D);
        tiledCRSDefinitions.put(APSTILE_NAME, new TiledCRSParams(APSTILE_NAME, APSTILE_CODE, APSTILE_BOUNDS, APSTILE_TILE_SIZE, APSTILE_TILE_ORIGIN, APSTILE_SCALES));
    }
}
