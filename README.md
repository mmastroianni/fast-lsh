fast-lsh
========

Introduction
--------------------

Locality sensitive hashing (LSH) is a technique used in approximate nearest-neighbor search.  For large datasets, searching the entire dataset for candidate near neighbors for a query point is often prohibitively expensive.  LSH uses hash functions that have a tendency to hash similar points to similar buckets and dissimilar points to dissimilar buckets, thereby reducing the set of points to consider in the nearest-neighbor search (Indyk and Motwani).

We implement a variant of LSH for cosine similarity.  In particular, we follow the discussions in Charikar (2002) and Ravichandran, et al. (2005).

Our implementation is divided into two parts, and indexer and a searcher.


Indexing
----------------------



Searching
----------------------



Benchmarks
----------------------

The indexing benchmark we've been running so far is as follows:

* Generate 10M random vectors 50 columns wide, and dump them into a
  csv file.
* Run the multi threaded indexer with 128 hashes, a batch size of 25k,
  and 16 threads (on this particular box, that number of threads
  seemed to be the sweet spot).

This benchmark has been run on a single i7 box running ubuntu 11.10
with 32G of ram.

Our current time is 53 seconds to index 10M rows and dump them to disk.

For really large data sizes, the signature generation should be done
in hadoop, as this will be pretty easy to do. We're planning to write
the hadoop version fairly soon.

References
----------------------
Charikar, M.  "Similarity Estimation Techniques from
Rounding Algorithms In Proceedings of the 34th Annual
ACM Symposium on Theory of Computing."  2002.

Indyk, P., Motwani, R.  "Approximate nearest neighbors: towards removing the curse of dimensionality."  Proc. of the 30th Symp. on the Theory of Computing, pp. 604–613, 1998.

Ravichandran, D, et al.  "Randomized Algorithms and NLP: Using Locality Sensitive Hash Function for High Speed Noun Clustering."  Proc. of the 43rd Annual Meeting of the ACL, pp. 622-629. 2005.
