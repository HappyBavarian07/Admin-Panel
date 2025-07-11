package de.happybavarian07.adminpanel.utils.tfidfsearch;/*
 * @Author HappyBavarian07
 * @Date 28.08.2023 | 14:53
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TFIDFSearch {

    private static String[] fields;
    private final Directory index;
    private IndexWriterConfig indexWriterConfig;
    private final Analyzer analyzer;
    private boolean indexInitialized = false;

    public TFIDFSearch(String[] fields) {
        TFIDFSearch.fields = fields;
        this.analyzer = new StandardAnalyzer();
        this.index = new ByteBuffersDirectory();
    }

    public CompletableFuture<Void> indexItems(List<Item> items) {
        return CompletableFuture.runAsync(() -> {
            this.indexWriterConfig = new IndexWriterConfig(analyzer);
            try (IndexWriter writer = new IndexWriter(index, indexWriterConfig)) {
                items.parallelStream().forEach(item -> {
                    Document doc = new Document();
                    for (String field : fields) {
                        doc.add(new TextField(field, item.getFieldValue(field), Field.Store.YES));
                    }
                    try {
                        writer.addDocument(doc);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                writer.commit();
                indexInitialized = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean isIndexInitialized() {
        try {
            if (indexInitialized) {
                try (IndexReader reader = DirectoryReader.open(index)) {
                    return reader.numDocs() > 0;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public List<Item> search(String queryStr) throws Exception {
        //System.out.println("Searching for " + queryStr);
        if (!isIndexInitialized()) {
            return new ArrayList<>();
        }

        try (IndexReader reader = DirectoryReader.open(index)) {
            if (reader.numDocs() == 0) {
                return new ArrayList<>();
            }

            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new MultiFieldQueryParser(fields, analyzer);
            parser.setAllowLeadingWildcard(true);

            if (queryStr.equals("*") || queryStr.endsWith(":*")) {
                queryStr = queryStr.replace("*", "*:*");
            }

            Query luceneQuery;
            try {
                luceneQuery = parser.parse(queryStr);
            } catch (ParseException e) {
                if (queryStr.contains("*")) {
                    return searchAllDocuments(reader);
                } else {
                    throw e;
                }
            }

            TopDocs topDocs = searcher.search(luceneQuery, Integer.MAX_VALUE);

            List<Item> results = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                //System.out.println("ScoreDoc" + scoreDoc.doc);
                Document doc = searcher.storedFields().document(scoreDoc.doc);
                Item item = new Item(new HashMap<>());
                for (String field : fields) {
                    item.setFieldValue(field, doc.get(field));
                }
                results.add(item);
            }
            return results;
        }
    }

    private List<Item> searchAllDocuments(IndexReader reader) throws IOException {
        IndexSearcher searcher = new IndexSearcher(reader);
        List<Item> results = new ArrayList<>();

        for (int i = 0; i < reader.maxDoc(); i++) {
            Document doc = searcher.storedFields().document(i);
            Item item = new Item(new HashMap<>());
            for (String field : fields) {
                item.setFieldValue(field, doc.get(field));
            }
            results.add(item);
        }

        return results;
    }

    // Replace this with your own data structure and logic
    public static class Item {
        private final String[] fieldValues = new String[fields.length]; // Store field values here

        public Item(Map<String, Object> map) {
            for (String field : fields) {
                fieldValues[getFieldIndex(field)] = (String) map.get(field);
            }
        }

        public String getFieldValue(String field) {
            int index = getFieldIndex(field);
            return index >= 0 ? fieldValues[index] : null;
        }

        public void setFieldValue(String field, String value) {
            int index = getFieldIndex(field);
            if (index >= 0) {
                fieldValues[index] = value;
            }
        }

        private int getFieldIndex(String field) {
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].equals(field)) {
                    return i;
                }
            }
            return -1;
        }
    }
}

