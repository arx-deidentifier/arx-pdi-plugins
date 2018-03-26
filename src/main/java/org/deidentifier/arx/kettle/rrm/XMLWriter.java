/*
 * Kettle re-identification risk management step
 * Copyright (C) 2018 TUM/MRI
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.deidentifier.arx.kettle.rrm;

import java.util.Locale;
import java.util.Stack;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * A writer for XML documents that can handle common objects. *
 * @author Fabian Prasser
 * @Helmut Spengler
 */
public abstract class XMLWriter<T> {
    
    /** The current prefix for indentation. */
    private StringBuilder prefix   = new StringBuilder();

    /** A backing string builder. */
    private StringBuilder builder = null;

    /** The current stack of nested elements. */
    private Stack<String> elements = new Stack<String>();

    /** Compact builder */
    private boolean       compact  = false;
    
    public abstract String write(T t);

    /**
     * Creates a new writer backed by a StringBuilder.
     */
    public XMLWriter() {
        this(false);
    }
    
	/**
     * Creates a new writer backed by a StringBuilder.
     *
     * @param compact
     */
	public XMLWriter(boolean compact) {
		this.builder = new StringBuilder();
		this.compact = compact;
	}
	
	/**
     * Intend the document.
     *
     * @param element
     * @
     */
	public void indent(String element) {
		elements.push(element);
		if (!compact) {
		    this.append(prefix.toString());
		}
		this.append("<"); //$NON-NLS-1$
		this.append(element);
		this.append(">"); //$NON-NLS-1$
		if (!compact) {
		    this.append("\n"); //$NON-NLS-1$
		    this.prefix.append("\t"); //$NON-NLS-1$
		}
	}

	/**
     * Intend the document.
     *
     * @param element
     * @param attribute
     * @param value
     * @
     */
	public void indent(String element, String attribute, int value) {
		elements.push(element);
		if (!compact) {
		    this.append(prefix.toString());
		}
		this.append("<"); //$NON-NLS-1$
		this.append(element);
		this.append(" "); //$NON-NLS-1$
		this.append(attribute);
		this.append("=\""); //$NON-NLS-1$
		this.append(String.valueOf(value));
		this.append("\""); //$NON-NLS-1$
		this.append(">"); //$NON-NLS-1$
        if (!compact) {
            this.append("\n"); //$NON-NLS-1$
            this.prefix.append("\t"); //$NON-NLS-1$
        }
	}

	/**
     * Returns a string representation.
     *
     * @return
     */
	public String toString(){
		return builder.toString();
	}

	/**
     * Unintend.
     */
	public void unindent() {
		String element = elements.pop();
		if (!compact) {
		    this.prefix.setLength(this.prefix.length()-1);
		    this.append(prefix.toString());
		}
		this.append("</"); //$NON-NLS-1$
		this.append(element);
		this.append(">"); //$NON-NLS-1$
		if (!compact) {
		    this.append("\n"); //$NON-NLS-1$
		}
	}

	/**
     * Appends the string.
     *
     * @param string
     */
	public void write(String string)  {
		this.append(string);
	}
	
	/**
     * Create an element.
     *
     * @param attribute
     * @param value
     */
	public void write(String attribute, boolean value) {
		write(attribute, String.valueOf(value));
	}
	
	/**
     * Create an element.
     *
     * @param attribute
     * @param value
     */
	public void write(String attribute, char value) {
		write(attribute, String.valueOf(value));
	}
	
	/**
     * Create an element.
     *
     * @param attribute
     * @param value
     */
	public void write(String attribute, double value) {
		write(attribute, String.format(Locale.ENGLISH, "%f", value));
	}

	/**
     * Create an element.
     *
     * @param attribute
     * @param value
     */
	public void write(String attribute, long value) {
		write(attribute, String.valueOf(value));
	}

	/**
     * Create an element.
     *
     * @param attribute
     * @param value
     */
	public void write(String attribute, String value) {
	    if (!compact) {
	        this.append(prefix.toString());
	    }
		this.append("<"); //$NON-NLS-1$
		this.append(attribute);
		this.append(">"); //$NON-NLS-1$
		this.append(StringEscapeUtils.escapeXml(value));
		this.append("</"); //$NON-NLS-1$
		this.append(attribute);
        this.append(">"); //$NON-NLS-1$
		if (!compact) {
		    this.append("\n"); //$NON-NLS-1$
		}
	}

	/**
     * Append stuff to the backing builder.
     *
     * @param value
     * @
     */
	private void append(String value) {
		builder.append(value);
	}
}
