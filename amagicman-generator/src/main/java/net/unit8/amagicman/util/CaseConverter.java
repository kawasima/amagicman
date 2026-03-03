package net.unit8.amagicman.util;

import java.util.*;
import java.util.function.Function;

import static net.unit8.amagicman.util.CaseConverter.CharType.*;

/**
 * Utility class for converting between common identifier naming conventions.
 *
 * <p>Supports conversion among {@code camelCase}, {@code PascalCase},
 * {@code snake_case}, and {@code SCREAMING_SNAKE_CASE}. The implementation
 * is inspired by <a href="https://github.com/qerub/camel-snake-kebab">camel-snake-kebab</a>.</p>
 *
 * <p>Token boundaries are detected at:</p>
 * <ul>
 *   <li>whitespace characters ({@code -}, {@code _}, space, tab, newline)</li>
 *   <li>transitions from a non-uppercase letter to an uppercase letter
 *       (e.g. {@code fooBar} → {@code ["foo", "Bar"]})</li>
 *   <li>transitions from a non-digit to a digit
 *       (e.g. {@code foo2Bar} → {@code ["foo", "2", "Bar"]})</li>
 *   <li>two consecutive uppercase letters followed by a lowercase letter
 *       (e.g. {@code XMLParser} → {@code ["XML", "Parser"]})</li>
 * </ul>
 *
 * @author kawasima
 */
public class CaseConverter {

    /**
     * Character category used by the tokeniser.
     */
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

        /**
         * Returns {@code true} if this category contains the given character.
         *
         * @param c the character to test
         * @return {@code true} if {@code c} belongs to this category
         */
        public boolean contains(char c) {
            return chars.contains(c);
        }
    }

    /** Capitalises a token: first character upper-cased, remainder lower-cased. */
    protected static final Function<String, String> capitalize = s -> {
        if (s.length() < 2) {
            return s.toUpperCase(Locale.US);
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(Locale.US);
        }
    };

    /** Converts a token to lower-case. */
    protected static final Function<String, String> lowerCase = s -> s.toLowerCase(Locale.US);

    /** Converts a token to upper-case. */
    protected static final Function<String, String> upperCase = s -> s.toUpperCase(Locale.US);

    private static boolean charEquals(String s, int index, CharType type) {
        return index >= 0 && index < s.length() && type.contains(s.charAt(index));
    }

    private static void tokensPlusNew(List<String> tokens, String ss, int start, int end) {
        if (end > start) {
            tokens.add(ss.substring(start, end));
        }
    }

    /**
     * Splits an identifier string into its constituent tokens.
     *
     * <p>Splitting occurs at whitespace characters and at character-type boundaries
     * as described in the class-level documentation.</p>
     *
     * @param ss the identifier string to split
     * @return ordered list of tokens; never {@code null}
     */
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

    /**
     * Core conversion routine that applies different case functions to the first and
     * remaining tokens, then joins them with a separator.
     *
     * @param firstFn function applied to the first token
     * @param restFn  function applied to all subsequent tokens
     * @param sep     separator inserted between tokens
     * @param s       input identifier string
     * @return converted string, or {@code null} if {@code s} is {@code null}
     */
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

    /**
     * Converts an identifier to {@code camelCase}.
     *
     * <p>Example: {@code "USER_NAME"} → {@code "userName"}</p>
     *
     * @param s the input identifier
     * @return the camelCase form, or {@code null} if {@code s} is {@code null}
     */
    public static String camelCase(String s) {
        return convertCase(lowerCase, capitalize, "", s);
    }

    /**
     * Converts an identifier to {@code PascalCase} (also known as UpperCamelCase).
     *
     * <p>Example: {@code "user_name"} → {@code "UserName"}</p>
     *
     * @param s the input identifier
     * @return the PascalCase form, or {@code null} if {@code s} is {@code null}
     */
    public static String pascalCase(String s) {
        return convertCase(capitalize, capitalize, "", s);
    }

    /**
     * Converts an identifier to {@code SCREAMING_SNAKE_CASE}.
     *
     * <p>Example: {@code "userName"} → {@code "USER_NAME"}</p>
     *
     * @param s the input identifier
     * @return the SCREAMING_SNAKE_CASE form, or {@code null} if {@code s} is {@code null}
     */
    public static String screamingSnakeCase(String s) {
        return convertCase(upperCase, upperCase, "_", s);
    }

    /**
     * Converts an identifier to {@code snake_case}.
     *
     * <p>Example: {@code "UserName"} → {@code "user_name"}</p>
     *
     * @param s the input identifier
     * @return the snake_case form, or {@code null} if {@code s} is {@code null}
     */
    public static String snakeCase(String s) {
        return convertCase(lowerCase, lowerCase, "_", s);
    }
}
