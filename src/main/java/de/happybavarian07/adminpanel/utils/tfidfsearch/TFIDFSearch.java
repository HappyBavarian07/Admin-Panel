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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TFIDFSearch {

    private static String[] fields;
    private final Directory index;
    private final Analyzer analyzer;

    public TFIDFSearch(String[] fields) {
        TFIDFSearch.fields = fields;
        this.analyzer = new StandardAnalyzer();
        this.index = new ByteBuffersDirectory();
    }

    public void indexItems(List<Item> items) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(index, config)) {
            for (Item item : items) {
                Document doc = new Document();
                //doc.add(new TextField("typeName", item.getName(), Field.Store.YES));
                //doc.add(new TextField("lore", Utils.listToString(item.getLore()), Field.Store.YES));
                //System.out.println("Fields:" + Arrays.toString(fields));
                //System.out.println("Item Fields:" + Arrays.toString(item.fieldValues));
                for (String field : fields) {
                    doc.add(new TextField(field, item.getFieldValue(field), Field.Store.YES));
                }
                writer.addDocument(doc);
            }
        }
    }

    public List<Item> search(String query) throws Exception {
        try (IndexReader reader = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new MultiFieldQueryParser(fields, analyzer);
            org.apache.lucene.search.Query luceneQuery = parser.parse(query);
            TopDocs topDocs = searcher.search(luceneQuery, Integer.MAX_VALUE); // Adjust the number of results as needed

            List<Item> results = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                Item item = new Item(new HashMap<>());
                for (String field : fields) {
                    item.setFieldValue(field, doc.get(field));
                }
                results.add(item);
            }
            return results;
        }
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

