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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.servlet.http.HttpServletRequest;

import java.text.MessageFormat;

/**
 * QueryParam - Enumeration to validate the MapMLServlet URL query parameters.
 */
public enum QueryParam {
    /* constructor("name", defaultValue) */
    xmin("xmin", 0D) {
        Object parseValue(String stringValue) {
          double xmin = Double.parseDouble(stringValue);
          return xmin;
        }},
    ymin("ymin", 0D) {
        Object parseValue(String stringValue) {
          double ymin = Double.parseDouble(stringValue);
          return ymin;
        }},
    xmax("xmax", 0D) {
        Object parseValue(String stringValue) {
          double xmax = Double.parseDouble(stringValue);
          return xmax;
        }},
    ymax("ymax", 0D) {
        Object parseValue(String stringValue) {
          double ymax = Double.parseDouble(stringValue);
          return ymax;
        }},
    alt("alt", null) {
        Object parseValue(String stringValue) {
            return stringValue;
        }},
    zoom("zoom", -1) {
        Object parseValue(String stringValue) {
          int zoom = Integer.parseInt(stringValue);
          if (zoom < -1 || zoom > 18) throw new RuntimeException("Valid zoom values are between 0 and 18");
          return zoom;
        }},
    projection("projection", "OSMTILE") {
      Object parseValue(String stringValue) {
          return stringValue;
      }},
    start("start", 0L) {
      Object parseValue(String stringValue) {
          Long val = (Long)Long.parseLong(stringValue);
          if (val < 0L) throw new RuntimeException("start parameter must be a positive integer");
          if (val > 68719476736L) throw new RuntimeException("Invalid start value");
          return val;
      }};

    private static final Logger log = LogManager.getLogger();

    private final String paramName;
    private final Object defaultValue;
    public String getParamName() { return paramName; }

    QueryParam(String paramName, Object defaultValue) {
        this.paramName = paramName;
        this.defaultValue = defaultValue;
    }
    /**
     * Accepts the string value as captured from the URL parameter and attempts
     * to instantiate the defined type of the object using the passed string.
     * If an error occurs, the constructor of the defined type should throw a
     * (subclass of) RunTimeException.  If no error occurs, the value is returned
     * as an Object.
     * @param stringValue
     * @return The value as an Object
     */
    abstract Object parseValue(String stringValue);
    public Object parse(HttpServletRequest request) {
        log.debug("parsing " + this + " (" + this.paramName + ")");
        Object value = this.defaultValue;
        String stringValue = request.getParameter(this.paramName);
        log.debug("value is " + stringValue);
        if (stringValue != null && !stringValue.trim().isEmpty()) {
            try {
            	value = parseValue(stringValue);
            } catch (RuntimeException e) {
                String msg = MessageFormat.format(
                        "Invalid value {1} for parameter {0}", this.paramName, stringValue);
                log.error(msg);
                throw new RuntimeException(msg,e);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace(MessageFormat.format("parsed request parameter {0} as value {1}",this.paramName,value));
        }
        return value;
    }
}
