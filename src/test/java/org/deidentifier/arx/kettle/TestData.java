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
package org.deidentifier.arx.kettle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestData {

    static List<String> fields1 = Arrays.asList("sex", "zip");
    static Set<String> qis1 = new HashSet<>(Arrays.asList("sex", "zip"));
    static String[][] ds1 = new String[][] {
        {  "sex", "zip"  },  // f (oc) | f (wc)
        {  "M",   "4711" },  // 3,       6
        {  "M",   "4711" },  // 3,       6
        {  "M",   "4711" },  // 3,       6
        { null,   "4711" },  // 2,       7
        { null,   "4711" },  // 2,       7
        {  "F",   "4712"  }, // 1,       2
        {  "F",    null  },  // 1,       4
        {  "M",    null  }   // 1,       6
    };
}
