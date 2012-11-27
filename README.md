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



References
----------------------
Charikar, M.  "Similarity Estimation Techniques from
Rounding Algorithms In Proceedings of the 34th Annual
ACM Symposium on Theory of Computing."  2002.

Indyk, P., Motwani, R.  "Approximate nearest neighbors: towards removing the curse of dimensionality."  Proc. of the 30th Symp. on the Theory of Computing, pp. 604–613, 1998.

Ravichandran, D, et al.  "Randomized Algorithms and NLP: Using Locality Sensitive Hash Function for High Speed Noun Clustering."  Proc. of the 43rd Annual Meeting of the ACL, pp. 622-629. 2005.
