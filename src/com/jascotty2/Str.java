/**
 * Programmer: Jacob Scott
 * Program Name: Str
 * Description:
 * Date: Mar 31, 2011
 */
package com.jascotty2;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jacob
 */
public class Str extends OutputStream {

    public String text = "";

    public static String argStr(String[] s) {
        return argStr(s, " ");
    }

    public static String argStr(String[] s, String sep) {
        String ret = "";
        if (s != null) {
            for (int i = 0; i < s.length; ++i) {
                ret += s[i];
                if (i + 1 < s.length) {
                    ret += sep;
                }
            }
        }
        return ret;
    }

    public static boolean isIn(String input, String[] check) {
        input = input.trim();
        for (String c : check) {
            if (input.equalsIgnoreCase(c.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIn(String input, String check) {
        String comms[] = check.split(",");
        input = input.trim();
        for (String c : comms) {
            if (input.equalsIgnoreCase(c.trim())) {
                return true;
            }
        }
        return false;
    }

    public static int count(String str, String find) {
        int c = 0;
        for (int i = 0; i < str.length() - find.length(); ++i) {
            if (str.substring(i, i + find.length()).equals(find)) {
                ++c;
            }
        }
        return c;
    }

    public static int count(String str, char find) {
        int c = 0;
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == find) {
                ++c;
            }
        }
        return c;
    }

    public static int countIgnoreCase(String str, String find) {
        int c = 0;
        for (int i = 0; i < str.length() - find.length(); ++i) {
            if (str.substring(i, i + find.length()).equalsIgnoreCase(find)) {
                ++c;
            }
        }
        return c;
    }

    @Override
    public void write(int b) throws IOException {
        text += (char) b;
    }
} // end class Str

