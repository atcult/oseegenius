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
package com.atc.osee.z3950;

/**
 * OseeGenius -Z- operators.
 * 
 * @author agazzarini
 * @since 1.0
 */
interface Operator 
{
	int AND = 1;
	int OR = 2;
	int NOT = 3;
	int PROXIMITY = 4;
}