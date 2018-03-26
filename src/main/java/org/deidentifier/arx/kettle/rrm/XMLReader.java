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

import org.w3c.dom.Node;

/**
 * This class encapsulates methods for creating objects of type &lt;T&gt; from XML nodes. * 
 * @author Fabian Prasser
 * @author Helmut Spengler
 *
 * @param <T>
 */
public abstract class XMLReader<T> {

    /**
     * Create an object from an XML node.
     * @param node
     * @return
     */
    public abstract T read(Node node);
}
