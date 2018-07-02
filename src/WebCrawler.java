import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Builds an inverted index from the web instead of a directory of text files. 
 * This web crawler is multithreaded, uses a work queue, and processes URLs in 
 * a breadth-first manner.
 * 
 * @author Anthony Panisales
 *
 */
public class WebCrawler {
	
	private InvertedIndex index;
	private int limit;
	private HashSet<URL> usedURLs;
	private ReadWriteLock lock;
	
	public WebCrawler(int limit) {
		this.index = new InvertedIndex();
		this.limit = limit;
		this.usedURLs = new HashSet<URL>();
		this.lock = new ReadWriteLock();
	}
	
	/**
	 * Uses a work queue to build an inverted index from a seed URL and a web crawler. 
	 * Each worker thread parses a single URL.
	 * 
	 * @param seed
	 *            the first URL to parse to create the inverted index
	 * @param index
	 *            the inverted index that is going to be built
	 * @param numOfThreads
	 *            the number of threads to have in the work queue
	 * @throws InterruptedException 
	 */
	public void threadsBuildIndex(URL seed, int numOfThreads) throws InterruptedException {
		WorkQueue queue = new WorkQueue(numOfThreads);
		queue.execute(new WebCrawlerTask(seed, queue));
		queue.finish();
		queue.shutdown();
	}
	
	/**
	 * Returns an inverted index built by the web crawler.
	 * 
	 * @return an inverted index
	 */
	public InvertedIndex getIndex() {
		return index;
	}
	
	/**
	 * Runnable task that crawls a single, cleaned, unique URL. Parses all of the URLs 
	 * on the HTML page from the URL, and adds them to the queue of URLs to process as 
	 * appropriate. Removes any style and script segments and remove all of the HTML tags 
	 * and entities. Cleans and parses the resulting text to populate the inverted index.
	 */
	private class WebCrawlerTask implements Runnable {
		
		private final URL url;
		private final WorkQueue queue;
		
		
		public WebCrawlerTask(URL url, WorkQueue queue) {
			this.url = url;
			this.queue = queue;
		}

		@Override
		public void run() {
			if (usedURLs.size() >= limit) {
				return;
			}
			
			String html = LinkParser.fetchHTML(url);
			
			if (html == null) {
				return;
			}
			
			if (usedURLs.size() >= limit) {
				return;
			}
			
			ArrayList<URL> urlList = LinkParser.listLinks(url, html);
			
			if (usedURLs.size() >= limit) {
				return;
			}
			
			for (URL newUrl : urlList) {
				lock.lockReadOnly();
				if (!usedURLs.contains(newUrl)) {
					queue.execute(new WebCrawlerTask(newUrl, queue));
				}
				lock.unlockReadOnly();
			}
			
			if (usedURLs.size() >= limit) {
				return;
			}
			
			html = HTMLCleaner.stripHTML(html);
			String[] words = WordParser.parseWords(html);
			WordIndex wordIndex = new WordIndex();
			wordIndex.addAll(words);
			index.addAll(url.toString(), wordIndex, wordIndex.copyWords());
			
			synchronized(usedURLs) {
				usedURLs.add(url);
			}
		}
	}
}
