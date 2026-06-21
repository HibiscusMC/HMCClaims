package com.hibiscusmc.hmcclaims.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility for string manipulation and fuzzy comparison.
 */
public class StringUtil {

    private final static Pattern NFD_PATTERN
            = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Checks if a string contains another string, ignoring case and accents.
     *
     * @param input     The source string to check.
     * @param toCompare The sequence to search for.
     * @return {@code true} if the sanitized input contains the sanitized comparison string.
     */
    @Contract(pure = true)
    public static boolean has(@NotNull String input, @NotNull String toCompare) {
        return sanitize(input).contains(sanitize(toCompare));
    }

    /**
     * Checks if any string within a list contains the specified string, ignoring case and accents.
     *
     * @param input     The list of strings to search through.
     * @param toCompare The sequence to search for.
     * @return {@code true} if any sanitized element in the list contains the sanitized comparison string.
     */
    @Contract(pure = true)
    public static boolean listHas(@NotNull List<String> input, @NotNull String toCompare) {
        return input.stream().anyMatch(s -> sanitize(s).contains(sanitize(toCompare)));
    }

    /**
     * Sanitizes a string by normalizing it to NFD form, stripping accents,
     * and converting it to lowercase.
     * <p>
     * Example: {@code "Jóshüá"} becomes {@code "joshua"}.
     *
     * @param string The string to sanitize.
     * @return A normalized, lowercase, accent-free version of the input.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static String sanitize(@NotNull String string) {
        return NFD_PATTERN.matcher(Normalizer.normalize(string, Normalizer.Form.NFD))
                .replaceAll("")
                .toLowerCase();
    }
}