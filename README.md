# RuTracker crawler v1.1.0

[![Build Status](https://travis-ci.org/spoluyan/rutracker-crawler.svg?branch=master)](https://travis-ci.org/spoluyan/rutracker-crawler)
[![Coverage Status](https://coveralls.io/repos/github/spoluyan/rutracker-crawler/badge.svg)](https://coveralls.io/github/spoluyan/rutracker-crawler)
[![License](http://img.shields.io/:license-mit-blue.svg)](http://doge.mit-license.org)

## Usage

Add to your `pom.xml`

```
<repositories>
  <repository>
    <id>oss.sonatype.org</id>
    <url>https://oss.sonatype.org/content/repositories/staging</url>
  </repository>
</repositories>
<dependencies>
  <dependency>
    <groupId>pw.spn</groupId>
    <artifactId>rutracker-crawler</artifactId>
    <version>1.1.0</version>
  </dependency>
</dependencies>
```

Main logic is implemented in `pw.spn.crawler.livelib.RutrackerCrawler` class. Just create new instance or inject into your service to use it.

## Current implemented operations

* Search torrents in all topics (only first 50 results)
* Search torrents in specific topics (only first 50 results for each topic)
* Download torrent files