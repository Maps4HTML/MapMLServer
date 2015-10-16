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

public class Point implements Cloneable {
  
  public double x;
  public double y;
  
  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  private Point _divideBy(double num) {
      this.x /= num;
      this.y /= num;
      return this;
  }
  public Point divideBy(double num) {
      return this.clone()._divideBy(num);
  }
  
  protected Point clone() {
      return new Point(this.x, this.y);
  }
  
  public Point floor() {
      return this.clone()._floor();
  }
  
  private Point _floor() {
      this.x = Math.floor(this.x);
      this.y = Math.floor(this.y);
      return this;
  }
  public Point ceil() {
      return this.clone()._ceil();
  }
  
  private Point _ceil() {
      this.x = Math.ceil(this.x);
      this.y = Math.ceil(this.y);
      return this;
  }
  public Point round() {
      return this.clone()._round();
  }
  
  private Point _round() {
      this.x = Math.round(this.x);
      this.y = Math.round(this.y);
      return this;
  }
  
  public double distanceTo(Point other) {
      double dx = Math.abs(other.x - this.x);
      double dy = Math.abs(other.y - this.y);
      return Math.sqrt(dx*dx + dy*dy);
  }
  
  public double getX() { 
    return this.x;
  }
  
  public double getY() {
    return this.y;
  }

}
