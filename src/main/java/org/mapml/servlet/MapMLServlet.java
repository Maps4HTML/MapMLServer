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
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mapml.MapMLServiceBounds;
import org.mapml.MapMLPrinter;
import org.mapml.exceptions.BadRequestException;
import org.mapml.projections.Bounds;
import org.mapml.projections.Point;
import org.mapml.uri.QueryParam;

@WebServlet(name = "MapMLServlet", urlPatterns = {"/MapMLServlet"})
public class MapMLServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MapMLPrinter printer;

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
        response.addHeader("Access-Control-Allow-Origin", "*");
        processRequest(request, response);
    }

    @Override
    public void init() throws ServletException {
        ServletConfig config = getServletConfig();
        Bounds extent = new Bounds(config.getInitParameter("extent"));
        String[] range = config.getInitParameter("zoomRange").split(",");
        
        String projection = config.getInitParameter("projection");
        printer = new MapMLPrinter(projection);
        
        printer.setServiceBounds(new MapMLServiceBounds(Integer.parseInt(range[0]),Integer.parseInt(range[1]),extent, printer.getTiledCRS()));
        
        String tileUrlTemplates = config.getInitParameter("tileUrlTemplate");
        String wmsUrlTemplates = config.getInitParameter("wmsUrlTemplate");
        
        if (tileUrlTemplates == null && wmsUrlTemplates == null) {
            throw new ServletException("Error reading tileUrlTemplate and wmsUrlTemplate config parameters from web.xml\n");
        }
        if (tileUrlTemplates != null) {
            printer.setTileUrlTemplates(tileUrlTemplates.split("\\n"));
        printer.setTileServers(config.getInitParameter("tileServers"));
        }
        if (wmsUrlTemplates != null) {
            printer.setWmsUrlTemplates(wmsUrlTemplates.split("\\n"));
        }
        printer.setLicenseUrl(config.getInitParameter("licenseUrl"));
        printer.setLicenseTitle(config.getInitParameter("licenseTitle"));
        printer.setTitle(config.getInitParameter("title"));
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
            
            Bounds bounds = null;
            if (!(xmin == 0D && ymin == 0D && xmax == 0D && ymax == 0D)) {
                bounds = new Bounds(new Point(xmin,ymin), new Point(xmax,ymax));
            }

            String projection = (String)QueryParam.projection.parse(request);
            String availableProjection = printer.getTiledCRS().getName();
            // validate that the requested projection is available
            if (!availableProjection.equalsIgnoreCase(projection) && !projection.equalsIgnoreCase("OSMTILE")) {
                // this could probably be a 416, I think.
                throw new BadRequestException("Invalid projection requested: "+ projection);
            }
            projection = availableProjection;

            // alt is a stealth parameter, not part of the contract good for debugging responses though
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
                printer.printMapMLDoc(responseType, start, base,  zoom, bounds, projection, out);
            } catch (Exception e) {
                response.sendError(500, e.getMessage());
            }
        } catch (RuntimeException e) {
            response.sendError(400, e.getMessage());
        }
    }
}
