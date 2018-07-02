import java.text.Normalizer;
import java.util.Collections;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Cleans and parses text into words.
 */
public class WordParser {

	/**
	 * Regular expression for splitting text into words by one or more
	 * whitespace.
	 */
	private static final Pattern SPLIT_REGEX = Pattern.compile("(?U)\\p{Space}+");

	/**
	 * Regular expression for removing all non-alpha and non-whitespace
	 * characters.
	 */
	private static final Pattern CLEAN_REGEX = Pattern.compile("(?U)[^\\p{Alpha}\\p{Space}]+");

	/**
	 * Cleans the text by removing all non-alphabetic and non-whitespace
	 * characters, and converting the remaining text to lowercase. The remaining
	 * text will also be converted to a "normalized" form (useful for accented
	 * letters, for example).
	 *
	 * @param text
	 *            text to be cleaned
	 * @return cleaned and normalized text
	 *
	 * @see #CLEAN_REGEX
	 */
	public static String clean(String text) {
		text = Normalizer.normalize(text, Normalizer.Form.NFC);
		text = CLEAN_REGEX.matcher(text).replaceAll(" ");
		return text.toLowerCase().trim();
	}

	/**
	 * Splits the text into words by one or more whitespace.
	 *
	 * @param text
	 *            text to be split into words
	 * @return array of words
	 *
	 * @see #SPLIT_REGEX
	 */
	public static String[] split(String text) {
		text = text.trim();
		return text.isEmpty() ? new String[0] : SPLIT_REGEX.split(text);
	}

	/**
	 * Convenience method for cleaning and splitting text.
	 *
	 * @param text
	 *            to clean and split
	 * @return cleaned array of words
	 *
	 * @see #split(String)
	 * @see #clean(String)
	 */
	public static String[] parseWords(String text) {
		return split(clean(text));
	}

	/**
	 * Convenience method for cleaning and splitting text, and returning only
	 * only the unique words found as a sorted set.
	 *
	 * @param text
	 *            to clean, split, and find unique words
	 * @return sorted set of unique words
	 *
	 * @see #parseWords(String)
	 */
	public static TreeSet<String> uniqueWords(String text) {
		TreeSet<String> words = new TreeSet<>();
		Collections.addAll(words, parseWords(text));
		return words;
	}
}
