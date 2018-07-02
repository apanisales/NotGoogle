# I Can't Believe It's Not Google

A search engine web application

* Created a web crawler that gathers words that could be used as search terms from the web and uses them and the websites they were found on to build an inverted index data structure.
* Implemented multithreading with Runnable tasks in order to improve the speed of performing searches on the inverted index.
* Utilized a MySQL database to allow for user accounts.
* The relevant websites found from performing a search are initially ranked in order of the number of appearances of the search terms in the HTML file. Locations where the search term(s) are more frequent are ranked above others. For locations that have the same frequency of search term(s), locations where the words appear in earlier positions are ranked above others. For locations that have the same frequency and position, the results are sorted by path in case-insensitive order.

Usage for web application:
```
javac Driver.java

java Driver [-url seed] [-limit total] [-port num]
```
* **-url seed**: "seed" is the seed URL the web crawler initially crawl to build the inverted index.
* **-limit total**: "-limit" indicates the next argument "total" is the total number of URLs to crawl (including the seed URL) when building the index. 50 is the default value if this flag is not properly provided.
* **-port num**: "num" is the port the web server should use to accept socket connections. 8080 is the default port used if "num" is not provided.

* *Note: Flags may be provided in any order*
* *Note: localhost is used as the server*
