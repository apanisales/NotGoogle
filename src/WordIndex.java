import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data structure to store strings and their positions.
 */
public class WordIndex {

	/**
	 * Stores a mapping of words to the positions the words were found.
	 */
	private Map<String, Set<Integer>> index;

	/**
	 * Initializes the index.
	 */
	public WordIndex() {
		index = new HashMap<String, Set<Integer>>(); 
	}

	/**
	 * Adds the word and the position it was found to the index.
	 *
	 * @param word
	 *            word to clean and add to index
	 * @param position
	 *            position word was found
	 */
	public void add(String word, int position) {
		if (!contains(word)) {
			index.put(word, new HashSet<Integer>());
		}
		index.get(word).add(position);
	}

	/**
	 * Adds the array of words at once, assuming the first word in the array is
	 * at position 1.
	 *
	 * @param words
	 *            array of words to add
	 *
	 * @see #addAll(String[], int)
	 */
	public void addAll(String[] words) {
		addAll(words, 1);
	}

	/**
	 * Adds the array of words at once, assuming the first word in the array is
	 * at the provided starting position
	 *
	 * @param words
	 *            array of words to add
	 * @param start
	 *            starting position
	 */
	public void addAll(String[] words, int start) {
		for (int i = 0; i < words.length; i++) {
			add(words[i], start++);
		}
	}

	/**
	 * Returns the number of times a word was found (i.e. the number of
	 * positions associated with a word in the index).
	 *
	 * @param word
	 *            word to look for
	 * @return number of times the word was found
	 */
	public int count(String word) {
		if (contains(word)) {
			return index.get(word).size();
		}
		return 0;
	}

	/**
	 * Returns the number of words stored in the index.
	 *
	 * @return number of words
	 */
	public int words() {
		return index.size();
	}

	/**
	 * Tests whether the index contains the specified word.
	 *
	 * @param word
	 *            word to look for
	 * @return true if the word is stored in the index
	 */
	public boolean contains(String word) {
		return index.containsKey(word);
	}

	/**
	 * Returns a copy of the words in this index as a sorted list.
	 *
	 * @return sorted list of words
	 *
	 * @see ArrayList#ArrayList(java.util.Collection)
	 * @see Collections#sort(List)
	 */
	public List<String> copyWords() {
		List<String> l = new ArrayList<String>(index.keySet());
		Collections.sort(l);
		return l;
	}

	/**
	 * Returns a copy of the positions for a specific word.
	 *
	 * @param word
	 *            to find in index
	 * @return sorted list of positions for that word
	 *
	 * @see ArrayList#ArrayList(java.util.Collection)
	 * @see Collections#sort(List)
	 */
	public List<Integer> copyPositions(String word) {
		List<Integer> l = new ArrayList<Integer>();
		if (contains(word)) {
			l.addAll(index.get(word));
			Collections.sort(l);
		}
		return l;
	}

	/**
	 * Returns a string representation of this index.
	 */
	@Override
	public String toString() {
		return index.toString();
	}
}