# Disk-search-algorithms

Simple exercise on searching data on disk using serial and binary search algorithms. The program creates a sorted file with values(keys) from zero to 10^7 using a buffer with size of 128 keys. Then, for each algorithm, 10000 searches are executed. Performance is defined as the average number of buffers that will be pulled from the disk for a search.

## Search algorithms

For all algorithms, search inside the buffer is done with binary search.

Searching on disk is executed with the following algorithms:
	
* Serial search.

* Binary search.

* Binary search with grouping questions.

* Binary search with cached buffers.
