/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Affero General Public License version 3 as published by the Free 
 * Software Foundation with the addition of the following permission added to Section 
 * 15 as permitted in Section 7(a). 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. 
 * You should have received a copy of the GNU Affero General Public License along with this program;
 */
package org.jzkit.z3950.server;

import java.math.BigInteger;

/**
 * The result of the scan operation.
 * From <a href="http://www.loc.gov/z3950/agency/markup/05.html#Scan-status7>Scan Status</a><br/>
 * The target indicates the result of the operation. The defined values are:
 * 
 * <ul>
 * 		<li>success - The response contains the number of entries (term-list-entries or surrogate diagnostics) requested.</li>
 * 		<li>partial-1 - Not all of the expected entries can be returned because the operation was terminated by access-control.</li>
 * 		<li>partial-2 - Not all of the expected entries will fit in the response message.</li>
 * 		<li>partial-3 - Not all of the expected entries can be returned because the operation was terminated by resource-control, at origin request.</li>
 * 		<li>partial-4 - Not all of the expected entries can be returned because the operation was terminated by resource-control, by target.</li>
 * 		<li>partial-5 - Not all of the expected entries can be returned because the term-list contains fewer entries (from either the low end, high end, or both ends of the term-list) than the number of terms requested.</li>
 * 		<li>failure - None of the expected entries can be returned. One or more non-surrogate diagnostics is returned.</li>
 * </ul>
 * @author agazzarini
 * @since 1.0
 */
public interface ScanStatus 
{
	BigInteger SUCCESS = BigInteger.ZERO;
	BigInteger PARTIAL_1 = BigInteger.ONE;
	BigInteger PARTIAL_2 = BigInteger.valueOf(2);
	BigInteger PARTIAL_3 = BigInteger.valueOf(3);
	BigInteger PARTIAL_4 = BigInteger.valueOf(4);
	BigInteger PARTIAL_5 = BigInteger.valueOf(5);
	BigInteger FAILURE = BigInteger.valueOf(6);
}