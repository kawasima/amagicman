package net.unit8.amagicman.util;

import java.util.*;
import java.util.function.Function;

import static net.unit8.amagicman.util.CaseConverter.CharType.*;

/**
 * Convert case.
 *
 * https://github.com/qerub/camel-snake-kebab
 *
 * @author kawasima
 */
public class CaseConverter {
    public enum CharType {
        NUMBER('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'),
        WHITESPACE('-', '_', ' ', '\t', '\n', '\r'),
        UPPER('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'),
        LOWER('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');

        private final Set<Character> chars;
        CharType(Character... cs) {
            chars = new HashSet<>(Arrays.asList(cs));
        }

        public boolean contains(char c) {
            return chars.contains(c);
        }
    }

    protected static final Function<String, String> capitalize = s -> {
        if (s.length() < 2) {
            return s.toUpperCase(Locale.US);
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(Locale.US);
        }
    };

    protected static final Function<String, String> lowerCase = s -> s.toLowerCase(Locale.US);
    protected static final Function<String, String> upperCase = s -> s.toUpperCase(Locale.US);

    private static boolean charEquals(String s, int index, CharType type) {
        return index >= 0 && index < s.length() && type.contains(s.charAt(index));
    }

    private static void tokensPlusNew(List<String> tokens, String ss, int start, int end) {
        if (end > start) {
            tokens.add(ss.substring(start, end));
        }
    }

    protected static List<String> genericSplit(String ss) {
        List<String> tokens = new ArrayList<>();
        int start = 0, current = 0;
        while (true) {
            int next = current + 1;
            if (current >= ss.length()) {
                tokensPlusNew(tokens, ss, start, current);
                return tokens;
            } else if (charEquals(ss, current, WHITESPACE)) {
                tokensPlusNew(tokens, ss, start, current);
                start = current = next;
            } else if ((!charEquals(ss, current, UPPER) && charEquals(ss, current + 1, UPPER))
                    || (!charEquals(ss, current, NUMBER) && charEquals(ss, current + 1, NUMBER))
                    || (charEquals(ss, current, UPPER) && charEquals(ss, current + 1, UPPER) && charEquals(ss, current + 2, LOWER))) {
                tokensPlusNew(tokens, ss, start, next);
                start = current = next;
            } else {
                current = next;
            }
        }
    }

    private static String convertCase(Function<String, String> firstFn, Function<String, String> restFn, String sep, String s) {
        if (s == null) return null;

        List<String> ss = genericSplit(s);
        if (!ss.isEmpty()) {
            ss.set(0, firstFn.apply(ss.get(0)));
        }
        if (ss.size() > 1) {
            for (int i = 1; i < ss.size(); i++) {
                ss.set(i, restFn.apply(ss.get(i)));
            }
        }
        return String.join(sep, ss);
    }


    public static String camelCase(String s) {
        return convertCase(lowerCase, capitalize, "", s);
    }

    public static String pascalCase(String s) {
        return convertCase(capitalize, capitalize, "", s);
    }

    public static String screamingSnakeCase(String s) {
        return convertCase(upperCase, upperCase, "_", s);
    }

    public static String snakeCase(String s) {
        return convertCase(lowerCase, lowerCase, "_", s);
    }
}
