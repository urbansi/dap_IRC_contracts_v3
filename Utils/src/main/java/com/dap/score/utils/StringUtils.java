/*
 * Copyright 2022 ICONation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dap.score.utils;

import java.math.BigInteger;
import score.Context;
import scorex.util.ArrayList;

public class StringUtils {

    public static BigInteger toBigInt (String input) {
        if (input.startsWith("0x")) {
            return new BigInteger(input.substring(2), 16);
        }

        if (input.startsWith("-0x")) {
            return new BigInteger(input.substring(3), 16).negate();
        }

        return new BigInteger(input, 10);
    }
    
    public static int indexOf (String str, String character) {
        return indexOf(str, character, 0);
    }

    public static int indexOf (String str, String character, int fromIndex) {
        int len = str.length();
        for (int i = fromIndex; i < len; i++) {
            char c = str.charAt(i);
            if (character.charAt(0) == c) {
                return i;
            }
        }

        return -1;
    }
    
    public static String[] split (String str, String character) {
        Context.require(character.length() == 1, 
            "StringUtils::split: character must be 1 length");

        ArrayList<String> list = new ArrayList<String>();
        int start = 0;
        int pos = StringUtils.indexOf(str, character);

        while (pos >= start) {
            if (pos >= start) {
                list.add(str.substring(start,pos));
            }
            start = pos + 1;
            pos = StringUtils.indexOf(str, character, start); 
        }

        if (start <= str.length()) {
            list.add(str.substring(start));
        }

        String[] result = new String[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }
        return result;
    }
    
    /**
     * Convert a hexstring with or without leading "0x" to byte array
     * @param hexstring a hexstring
     * @return a byte array
     */
    public static byte[] hexToByteArray(String hexstring) {
        if (hexstring.startsWith("0x")) {
            hexstring = hexstring.substring(2);
        }
        
        /* hexstring must be an even-length string. */
        Context.require(hexstring.length() % 2 == 0,
            "hexToByteArray: invalid hexstring length");

        int len = hexstring.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int c1 = Character.digit(hexstring.charAt(i), 16) << 4;
            int c2 = Character.digit(hexstring.charAt(i+1), 16);

            if (c1 == -1 || c2 == -1) {
                Context.revert("hexToByteArray: invalid hexstring character at pos " + i);
            }

            data[i / 2] = (byte) (c1 + c2);
        }
        return data;
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static String byteArrayToHex(byte[] data) {
        char[] hexChars = new char[data.length * 2];

        for (int j = 0; j < data.length; j++) {
            int v = data[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }
}
