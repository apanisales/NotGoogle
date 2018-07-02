import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Convenience class to build a {@link WordIndex} from a text file.
 */
public class WordIndexBuilder {

	/**
	 * Creates and returns a new word index built from the file located at the
	 * path provided.
	 *
	 * @param path
	 *            path to file to parse
	 * @return word index containing words from the path
	 * @throws IOException
	 *
	 * @see {@link #buildIndex(Path, WordIndex)}
	 */
	public static WordIndex buildIndex(Path path) throws IOException {
		WordIndex index = new WordIndex();
		buildIndex(path, index);
		return index;
	}

	/**
	 * Opens the file located at the path provided, parses each line in the file
	 * into words, and stores those words in a word index.
	 *
	 * @param path
	 *            path to file to parse
	 * @param index
	 *            word index to add words
	 * @throws IOException
	 *
	 * @see WordParser#parseWords(String, int)
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 */
	public static void buildIndex(Path path, WordIndex index) throws IOException {
		String htmlString;
		File wholeHTMLFile = new File(path.toString());
		Scanner wholeFileScan = new Scanner(wholeHTMLFile);
		wholeFileScan.useDelimiter("\\A");
		
		if (wholeFileScan.hasNext()) {
			htmlString = HTMLCleaner.stripHTML(wholeFileScan.next());
			wholeFileScan.close();
		} else {
			wholeFileScan.close();
			return;
		}
		
		String[] words = WordParser.parseWords(htmlString);
		index.addAll(words);
	}
}