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
package org.mapml.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mapml.MapMLServiceBounds;
import org.mapml.MapMLPrinter;
import org.mapml.uri.QueryParam;
import org.mapml.util.Bbox;

@WebServlet(name = "MapMLServlet", urlPatterns = {"/MapMLServlet"})
public class MapMLServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final MapMLPrinter printer = new MapMLPrinter();

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public void init() {
        ServletConfig config = getServletConfig();
        Bbox extent = new Bbox(config.getInitParameter("extent"));
        String[] range = config.getInitParameter("zoomRange").split(",");
        printer.setServiceBounds(new MapMLServiceBounds(Integer.parseInt(range[0]),Integer.parseInt(range[1]),extent));
        printer.setTileUrlTemplates(config.getInitParameter("tileUrlTemplate").split(","));
        printer.setTileServers(config.getInitParameter("tileServers"));
        printer.setLicenseText(config.getInitParameter("licenseText"));
        printer.setLicenseUrl(config.getInitParameter("licenseUrl"));
    }
    /**
     * Processes <code>GET</code> requests.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int    zoom = (Integer)QueryParam.zoom.parse(request);
            
            double xmin = (double)QueryParam.xmin.parse(request);
            double ymin = (double)QueryParam.ymin.parse(request);
            double xmax = (double)QueryParam.xmax.parse(request);
            double ymax = (double)QueryParam.ymax.parse(request);

            Bbox bbox = new Bbox(Arrays.asList(xmin,ymin,xmax,ymax));

            // could validate that the requested projection is available
            String projection = (String)QueryParam.projection.parse(request);

            // alt is a stealth parameter, not part of the contract
            String alt = (String)QueryParam.alt.parse(request);
            String responseType;
            if (alt != null && alt.equalsIgnoreCase("xml")) {
                responseType = "application/xml";
            } else {
                responseType = "text/mapml;projection="+projection+";zoom="+zoom;
            }
            response.setContentType(responseType);
            
            // start should be set if this is not the first page of large request
            long start = (Long)QueryParam.start.parse(request);

            // getRequestURL() omits the query part
            String base = request.getRequestURL().toString();

            try (PrintWriter out = response.getWriter()) {
                printer.printMapMLDoc(responseType, start, base,  zoom, bbox, out);
            } catch (Exception e) {
                response.sendError(500, e.getMessage());
            }
        } catch (RuntimeException e) {
            response.sendError(400, e.getMessage());
        }
    }
}
