## A test ingest pipeline for language and general data processing
### poller
1. Collectors will gather files from source directory and adds them to a cached directory listing.
2. Based on configured batch size and rate, FileProviders will move/copy a batch of files to their destination

Hoping to support S3, Local file systems, and HDFS
