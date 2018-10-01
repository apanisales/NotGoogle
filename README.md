# I Can't Believe It's Not Google

A search engine web application

* Created a web crawler that gathers words that could be used as search terms from the web and uses them and the websites they were found on to build an inverted index data structure.
* Implemented multithreading using a work queue and Runnable tasks which improved the speed of the web crawler as well as the speed of building and performing searches on the inverted index.
* Utilized a MySQL database to allow for user accounts.
* The relevant locations found from performing a search are initially ranked in order of the number of appearances of the search terms in the HTML file. Locations where the search term(s) are more frequent are ranked above others. For locations that have the same frequency of search term(s), locations where the words appear in earlier positions are ranked above others. For locations that have the same frequency and position, the results are sorted by URL in case-insensitive order.

Usage for web application:
```
javac Driver.java

java Driver [-url seed] [-limit total] [-port num] [-threads num]
```
* **-url seed**: "seed" is the seed URL the web crawler initially crawls to build the inverted index.
* **-limit total**: the flag -limit indicates the next argument "total" is the total number of URLs to crawl (including the seed URL) when building the index. 50 is the default value if this flag is not properly provided.
* **-port num**: "num" is the port the web server should use to accept socket connections. 8080 is the default port used if "num" is not provided.
* **-threads num**: the flag -threads indicates the next argument "num" is the number of threads to use. If an invalid number of threads are provided, 5 threads are used as default. If the -threads flag is not provided, then the program will be single-threaded.

* *Note: Flags may be provided in any order*
* *Note: localhost is used as the server*

## Other Features

* Can build an inverted index by processing all of the HTML files in a directory and its subdirectories.
* The inverted index can be written to a JSON file.
* Able to parse a query file, generate a sorted list of search results from the inverted index, and support writing those results to a JSON file. 

Usage for other features:
```
javac Driver.java

java Driver [-path path] [-index path] [-query filepath] [-exact] [-results filepath]
```
* **-path path**: the flag -path indicates the next argument is a path to either a single HTML file or a directory of HTML files that must be processed and added to the inverted index.
* **-index path**: the flag -index indicates the next argument is the path to use for the inverted index output file. If the "path" argument is not provided, "index.json" is the default output path. If the -index flag is not provided, an output file is not produced.
* **-query filepath**: the flag -query indicates the next argument is a path to a text file of queries to be used for search. If this flag is not provided, then no search is performed.
* **-exact**: the flag -exact indicates all search operations performed are exact search. If the flag is NOT present, all search operations are partial search instead.
    * *partial search*: any word in the inverted index that starts with a query word is taken into account.
    * *exact search*: any word in the inverted index that exactly matches a query word is taken into account.
* **-results filepath**: the flag -results indicates the next argument is a file path, and "filepath" is the path to the file to use for the search results output file. If the "filepath" argument is not provided, "results.json" is the default output filename. If the -results flag is not provided, an output file of search results is not produced but the search operation is still performed.
* *Note: Flags may be provided in any order*
