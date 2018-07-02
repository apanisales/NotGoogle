import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.io.BufferedWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map.Entry;

/**
 * Data structure that stores the mapping from words to the
 * documents and positions within those documents where those
 * words were found.
 * 
 * @author Anthony Panisales
 *
 */
public class InvertedIndex {
	
	/**
	 * Writes the contents of an inverted index in alphabetically sorted order
	 * as a nested JSON object using a "pretty" format with tab characters for 
	 * indentation to a file.
	 */
	private class IndexJSONWriter {

		/**
		 * Returns a String with the specified number of tab characters.
		 *
		 * @param times
		 *            number of tab characters to include
		 * @return tab characters repeated the specified number of times
		 */
		private String indent(int times) {
			char[] tabs = new char[times];
			Arrays.fill(tabs, '\t');
			return String.valueOf(tabs);
		}

		/**
		 * Returns a quoted version of the provided text.
		 *
		 * @param text
		 *            text to surround in quotes
		 * @return text surrounded by quotes
		 */
		private String quote(String text) {
			return String.format("\"%s\"", text);
		}

		/**
		 * Writes the list of positions as a JSON array at the specified indent level.
		 *
		 * @param writer
		 *            writer to use for output
		 * @param positions
		 *            list of positions to write as JSON array
		 * @param level
		 *            number of times to indent the array itself
		 * @throws IOException
		 */
		private void asArray(Writer writer, List<Integer> positions, int level) throws IOException {
			writer.write("[\n");
			int index = 0;
			for (int position : positions) {
				if (index != positions.size()-1) {
					writer.write(indent(level+1) + position + ",\n");
				} else {
					writer.write(indent(level+1) + position + "\n");
				}
				index++;
			}
			writer.write(indent(level) + "]");
		}

		/**
		 * Writes the inverted index as a JSON object to the path using UTF8.
		 *
		 * @param invertedIndex
		 *            inverted index data structure to write as a JSON object
		 * @param path
		 *            path to write the file to
		 * @throws IOException
		 * @throws InterruptedException 
		 */
		public void asObject(InvertedIndex invertedIndex, Path path) throws IOException, InterruptedException {
			try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);) {
				writer.write("{\n");
				int index = 0;
				for (Entry<String, HashMap<String, List<Integer>>> e : invertedIndex.toTreeMap().entrySet()) {
					writer.write(indent(1) + quote(e.getKey()) + ": {\n");
					asNestedObject(writer, new TreeMap<String, List<Integer>>(e.getValue()), path);
					if (index != invertedIndex.size()-1) {
						writer.write(indent(1) + "},\n");
					} else {
						writer.write(indent(1) + "}\n");
					}
					index++;
				}
				writer.write("}");
				writer.flush();
			}
		}
		
		/**
		 * Writes the map of elements as a JSON object with a nested array to the
		 * path using UTF8.
		 *
		 * @param writer
		 *            writer to use for output
		 * @param elements
		 *            elements to write as a JSON object with a nested array
		 * @param path
		 *            path to write file
		 * @throws IOException
		 */
		public void asNestedObject(Writer writer, TreeMap<String, List<Integer>> elements, Path path) throws IOException {
			int filenameIndex = 0;
			for (String filename : elements.keySet()) {
				writer.write(indent(2) + quote(filename) + ": ");
				asArray(writer, elements.get(filename), 2);
				if (filenameIndex != elements.size()-1) {
					writer.write(",\n");
				} else {
					writer.write("\n");
				}
				filenameIndex++;
			}
			writer.flush();
		}
	}
	
	private HashMap<String,HashMap<String,List<Integer>>> invertedIndex;
	private final ReadWriteLock lock;
	
	/**
	 * Initializes the inverted index.
	 */
	public InvertedIndex() {
		invertedIndex = new HashMap<String,HashMap<String,List<Integer>>>();
		lock = new ReadWriteLock();
	}
	
	/**
	 * Merges another inverted index with this index.
	 * 
	 * @param indexToAdd
	 *              index to merge with
	 */
	public void addIndextoIndex(InvertedIndex indexToAdd) throws InterruptedException {
		lock.lockReadWrite();
		for (Entry<String, HashMap<String, List<Integer>>> indexToAddEntry : indexToAdd.toTreeMap().entrySet()) {
			
			// If invertedIndex has already has the word
			if (invertedIndex.containsKey(indexToAddEntry.getKey())) {
				HashMap<String, List<Integer>> invertedIndexInnerMap = invertedIndex.get(indexToAddEntry.getKey());
				HashMap<String, List<Integer>> indexToAddEntryInnerMap = indexToAddEntry.getValue();
				for (String path : indexToAddEntryInnerMap.keySet()) {
					invertedIndexInnerMap.put(path, indexToAddEntryInnerMap.get(path));
				}
				invertedIndex.put(indexToAddEntry.getKey(), invertedIndexInnerMap);
			} else {
				invertedIndex.put(indexToAddEntry.getKey(), indexToAddEntry.getValue());
			}
			
		}
		lock.unlockReadWrite();
	}
	
	/**
	 * Adds an entry into the inverted index for every word in the given
	 * list of words.
	 * 
	 * @param pathString
	 *              name of the path where the list of words was found      
	 * @param wi
	 *              word index containing the list of words
	 * @param words
	 *              list of words to be put into the inverted index
	 */
	public void addAll(String pathString, WordIndex wi, List<String> words) {
		lock.lockReadWrite();
		for (String word : words) {
			if (!invertedIndex.containsKey(word)) {
				invertedIndex.put(word, new HashMap<String,List<Integer>>());
			}
			invertedIndex.get(word).put(pathString, wi.copyPositions(word));
		}
		lock.unlockReadWrite();
	}
	
	/**
	 * Checks if a path is a valid HTML file, and if it is, 
	 * then new mappings that consist of words and HashMaps
	 * that contain mappings of filenames and lists of positions,
	 * are placed inside the inverted index
	 * 
	 * @param filepath
	 *               path to the file to check
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void checkAndParseFile(Path filepath) throws IOException, InterruptedException {
		String fileString = filepath.toString();
		
		if (Files.isReadable(filepath) && (fileString.endsWith(".html") || fileString.endsWith(".htm")) || fileString.endsWith(".HTML")) {
			WordIndex wi = WordIndexBuilder.buildIndex(filepath);
			addAll(fileString, wi, wi.copyWords());
		}
	}
	
	/**
	 * Recursively passes through a directory and its contents. If one
	 * entry in the directory is another directory, then checkDirectory
	 * is called again with the entry as an argument. If an entry is
	 * not another directory, then checkFile is called, using that 
	 * entry as an argument.
	 * 
	 * @param directory
	 *                path to the directory to pass through
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public synchronized void checkDirectory(Path directory) throws IOException, InterruptedException {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory)) {
		    for (Path dspath : ds) {
		    	if (Files.isDirectory(dspath)) {
		    		checkDirectory(dspath);
		    	} else {
		    		checkAndParseFile(dspath);
		    	}
		    }
		}
	}
	
	/**
	 * Makes sure that filenames only correspond to one search result per 
	 * query and updates those search results as needed.
	 * 
	 * @param usedFilenames
	 *           map containing mappings of a filename and its search result
	 * @param word
	 *           a word in the inverted index that is valid for the query and
	 *           the type of search being performed
	 */
	private void search(HashMap<String,QueryHelper.SearchResult> usedFilenames, String word) {
		HashMap<String,List<Integer>> invIdxInnerMap = invertedIndex.get(word);
		for (String filename : invIdxInnerMap.keySet()) {
			QueryHelper.SearchResult sr;
			if (!usedFilenames.containsKey(filename)) {
				sr = new QueryHelper().new SearchResult(filename);
			} else {
				sr = usedFilenames.get(filename);
			}
			List<Integer> listOfPos = invIdxInnerMap.get(filename);
			sr.setCount(sr.getCount() + listOfPos.size());
			if (listOfPos.get(0) < sr.getFirstPos()) {
				sr.setFirstPos(listOfPos.get(0));
			}
			usedFilenames.put(filename, sr);
		}
	}

	/**
	 * Returns partial search results from the inverted index, such that any 
	 * word in the inverted index that starts with a query word is taken into
	 * account.
	 * 
	 * @param query
	 *            array of words to be searched for
	 * @return list of sorted search results
	 * @throws InterruptedException
	 */
	public List<QueryHelper.SearchResult> partialSearch(String[] query) throws InterruptedException {
		HashMap<String,QueryHelper.SearchResult> usedFilenames = new HashMap<String,QueryHelper.SearchResult>();
		HashSet<String> validInvIdxWords = new HashSet<String>();
		lock.lockReadOnly();
		for (String word : query) {
			for (String invIdxWord : invertedIndex.keySet()) {
				if (invIdxWord.startsWith(word)) {
					validInvIdxWords.add(invIdxWord);
				}
			}
		}
		for (String word : validInvIdxWords) {
			search(usedFilenames, word);
		}
		lock.unlockReadOnly();
		List<QueryHelper.SearchResult> searchResults = new ArrayList<QueryHelper.SearchResult>();
		searchResults.addAll(usedFilenames.values());
		Collections.sort(searchResults, new Comparator<QueryHelper.SearchResult>() {
			@Override
			public int compare(QueryHelper.SearchResult a, QueryHelper.SearchResult b) {
				return a.compareTo(b);
			}
		});
		return searchResults;
	}

	/**
	 * Returns exact search results from the inverted index, such that any 
	 * word in the inverted index that exactly matches a query word is taken
	 * into account.
	 * 
	 * @param query
	 *            array of words to be searched for
	 * @return list of sorted search results
	 * @throws InterruptedException
	 */
	public List<QueryHelper.SearchResult> exactSearch(String[] query) throws InterruptedException {
		HashMap<String,QueryHelper.SearchResult> usedFilenames = new HashMap<String,QueryHelper.SearchResult>();
		lock.lockReadOnly();
		for (String word : query) {
			if (invertedIndex.containsKey(word)) {
				search(usedFilenames, word);
			}
		}
		lock.unlockReadOnly();
		List<QueryHelper.SearchResult> searchResults = new ArrayList<QueryHelper.SearchResult>();
		searchResults.addAll(usedFilenames.values());
		Collections.sort(searchResults, new Comparator<QueryHelper.SearchResult>() {
			@Override
			public int compare(QueryHelper.SearchResult a, QueryHelper.SearchResult b) {
				return a.compareTo(b);
			}
		});
		return searchResults;
	}
	
	/**
	 * Writes the contents of an inverted index in alphabetically sorted order
	 * as a nested JSON object using a "pretty" format with tab characters for 
	 * indentation to a file.
	 * 
	 * @param outpath
	 *              path to write the file to
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public synchronized void asObject(Path outpath) throws IOException, InterruptedException {
		Files.createFile(outpath);
		new IndexJSONWriter().asObject(this, outpath);
	}
	
	/**
	 * Returns the number of mappings in the inverted index.
	 * 
	 * @return size of the inverted index
	 */
	public synchronized int size() {
		return invertedIndex.size();
	}
	
	/**
	 * Returns the inverted index sorted by the words.
	 * 
	 * @return inverted index as a TreeMap
	 * @throws InterruptedException
	 */
	public synchronized TreeMap<String,HashMap<String,List<Integer>>> toTreeMap() throws InterruptedException {
		return new TreeMap<String,HashMap<String,List<Integer>>>(invertedIndex);
	}
	
	/**
	 * Uses a work queue to build an inverted index from a directory of files using multiple
	 * worker threads. Each worker thread parses a single HTML file.
	 * 
	 * @param indexInput
	 *            the first file to parse to create the inverted index
	 * @param index
	 *            the inverted index that is going to be built
	 * @param queue
	 *            work queue with multiple worker threads
	 */
	public static void threadsBuildIndex(Path indexInput, InvertedIndex index, WorkQueue queue) {
		queue.execute(new IndexTask(indexInput, index, queue));
	}
	
	/**
	 * Runnable task that determines if a path leads to a valid HTML file, and if it
	 * does, then it, along with its contents will be added to the inverted index. If it
	 * leads to a directory, then new tasks will be created for each file in the directory.
	 */
	private static class IndexTask implements Runnable {
		
		private final Path file;
		private final InvertedIndex index;
		private final WorkQueue queue;
		
		public IndexTask(Path file, InvertedIndex index, WorkQueue queue) {
			this.file = file;
			this.index = index;
			this.queue = queue;
		}

		@Override
		public void run() {
			try {
				if (Files.isDirectory(file)) {
					try (DirectoryStream<Path> ds = Files.newDirectoryStream(file)) {
					    for (Path dspath : ds) {
					    	queue.execute(new IndexTask(dspath, index, queue));
					    }
					} 
		    	} else {
	    			index.checkAndParseFile(file);
		    	}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
