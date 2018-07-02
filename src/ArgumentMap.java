
import java.util.HashMap;
import java.util.Map;

/**
 * Parses command-line arguments into flag/value pairs, and stores those pairs
 * in a map for easy access.
 */
public class ArgumentMap {

	private final Map<String, String> map;

	/**
	 * Initializes the argument map.
	 */
	public ArgumentMap() {
		map = new HashMap<>();
	}

	/**
	 * Initializes the argument map and parses the specified arguments into
	 * key/value pairs.
	 *
	 * @param args
	 *            command line arguments
	 *
	 * @see #parse(String[])
	 */
	public ArgumentMap(String[] args) {
		this();
		parse(args);
	}

	/**
	 * Parses the specified arguments into key/value pairs and adds them to the
	 * argument map.
	 *
	 * @param args
	 *            command line arguments
	 */
	public void parse(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (isFlag(args[i])) {
				map.put(args[i], null);
			} else if (isValue(args[i]) && i != 0 && isFlag(args[i-1])) {
				map.put(args[i-1], args[i]);
			}
		}
	}

	/**
	 * Returns whether a String can be a valid flag or not
	 *
	 * @param arg
	 *          String to test
	 * @return true if yes, false if no
	 */
	public static boolean isFlag(String arg) {
		if (arg == null || arg.length() == 0) {
			return false;
		}
		if (arg.charAt(0) == '-') {
			if (arg.length() == 1 || arg.charAt(1) == ' ') {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns whether a String can be a valid value or not
	 *
	 * @param arg
	 *          String to test
	 * @return true if yes, false if no
	 */
	public static boolean isValue(String arg) {
		if (arg == null || arg.length() == 0) {
			return false;
		}
		return Character.isLetterOrDigit(arg.charAt(0)) || arg.startsWith("/");
	}

	/**
	 * Returns the number of unique flags stored in the argument map.
	 *
	 * @return number of flags
	 */
	public int numFlags() {
		return map.size();
	}

	/**
	 * Determines whether the specified flag is stored in the argument map.
	 *
	 * @param flag
	 *            flag to test
	 *
	 * @return true if the flag is in the argument map
	 */
	public boolean hasFlag(String flag) {
		return map.containsKey(flag);
	}

	/**
	 * Determines whether the specified flag is stored in the argument map and
	 * has a non-null value stored with it.
	 *
	 * @param flag
	 *            flag to test
	 *
	 * @return true if the flag is in the argument map and has a non-null value
	 */
	public boolean hasValue(String flag) {
		return map.get(flag) != null;
	}

	/**
	 * Returns the value for the specified flag as a String object.
	 *
	 * @param flag
	 *            flag to get value for
	 *
	 * @return value as a String or null if flag or value was not found
	 */
	public String getString(String flag) {
		return map.get(flag);
	}

	/**
	 * Returns the value for the specified flag as a String object. If the flag
	 * is missing or the flag does not have a value, returns the specified
	 * default value instead.
	 *
	 * @param flag
	 *            flag to get value for
	 * @param defaultValue
	 *            value to return if flag or value is missing
	 * @return value of flag as a String, or the default value if the flag or
	 *         value is missing
	 */
	public String getString(String flag, String defaultValue) {
		if (hasValue(flag)) {
			return map.get(flag);
		}
		return defaultValue;
	}

	/**
	 * Returns the value for the specified flag as an int value. If the flag is
	 * missing or the flag does not have a value, returns the specified default
	 * value instead.
	 *
	 * @param flag
	 *            flag to get value for
	 * @param defaultValue
	 *            value to return if the flag or value is missing
	 * @return value of flag as an int, or the default value if the flag or
	 *         value is missing
	 */
	public int getInteger(String flag, int defaultValue) {
		if (hasValue(flag) && map.get(flag).matches("[0-9]+")) {
			return Integer.parseInt(map.get(flag));
		}
		return defaultValue;
	}

	/**
	 * Returns a string representation of this argument map.
	 * 
	 * @return argument map as string
	 */
	@Override
	public String toString() {
		return map.toString();
	}
}
