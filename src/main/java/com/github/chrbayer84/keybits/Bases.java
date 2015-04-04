/**
 * Copyright (C) 2014 keybits@gmx.de This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.github.chrbayer84.keybits;

public class Bases
{
    private static char[] base16_alphabet = "0123456789ABCDEF".toCharArray();

    public static byte[] base16StringToByteArray( String s )
    {
        int l = s.length();
        byte[] ret = new byte[l / 2];
        for ( int i = 0; i < l; i += 2 )
            // get first character number in hex, divide by 16 and add second character number in hex
            ret[i / 2] =
                (byte) ( ( Character.digit( s.charAt( i ), 16 ) << 4 ) + Character.digit( s.charAt( i + 1 ), 16 ) );

        return ret;
    }

    public static String byteArrayToBase16String( byte[] bytes )
    {
        char[] hex_chars = new char[2 * bytes.length];
        for ( int j = 0; j < bytes.length; j++ )
        {
            int value = bytes[j] & 0xFF; // get both entries for hex-number, also necessary if byte has value 256 which
                                         // is zero
            hex_chars[2 * j] = Bases.base16_alphabet[value >>> 4]; // divide value by 16 to get first character, shift
                                                                   // bits four positions
            hex_chars[2 * j + 1] = Bases.base16_alphabet[value & 0x0F]; // get second entry for hex-number
        }
        return new String( hex_chars );
    }
}
