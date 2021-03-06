/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.shared.util.parser;

/**
 * This class implements an exception which can wrap a lower-level exception.
 * 
 * <p>Notice that it is now a RuntimeException which should often be caught
 * at some level.
 * 
 * @author J. Stowisek
 * @version $Revision: 1.1 $ ($Date: 2004/12/06 13:25:51 $ - $State: Exp $)
 */
public class ParserException extends RuntimeException {
  private Exception exception;

  /**
   * Creates a new ParserException wrapping another exception, 
   * and with a detailed message.
   * @param message the detailed message.
   * @param exception the wrapped exception.
   */
  public ParserException(String message, Exception exception) {
    super(message);
    this.exception = exception;
    return;
  }

  /**
   * Creates a ParserException with the specified detail message.
   * @param message the detail message.
   */
  public ParserException(String message) {
    this(message, null);
    return;
  }

  /**
   * Creates a new ParserException wrapping another exception, and with no detail message.
   * @param exception the wrapped exception.
   */
  public ParserException(Exception exception) {
    this(null, exception);
    return;
  }

  /**
   * Gets the wrapped exception.
   *
   * @return the wrapped exception.
   */
  public Exception getException() {
    return exception;
  }

  /**
   * Retrieves (recursively) the root cause exception.
   *
   * @return the root cause exception.
   */
  public Exception getRootCause() {
    if (exception instanceof ParserException) {
      return ((ParserException) exception).getRootCause();
    }
    return exception == null ? this : exception;
  }

  public String toString() {
    if (exception instanceof ParserException) {
      return ((ParserException) exception).toString();
    }
    return exception == null ? super.toString() : exception.toString();
  }
}
