package com.synavos.maps.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LatLonDocValuesField;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.synavos.maps.beans.Location;
import com.synavos.maps.google.api.response.Place;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/** The Constant log. */
@Slf4j
public class LuceneCache {

    /** The ram directory. */
    private final Directory directory;

    /** The analyzer. */
    private final Analyzer analyzer;

    /** The index writer config. */
    private final IndexWriterConfig indexWriterConfig;

    private final IndexWriter writer;

    /**
     * Instantiates a new lucene cache.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public LuceneCache() throws IOException {
	directory = FSDirectory.open(GoogleMapProperties.PLACES_DIR);
	analyzer = new StandardAnalyzer();

	indexWriterConfig = new IndexWriterConfig(analyzer);
	indexWriterConfig.setOpenMode(OpenMode.CREATE);

	writer = new IndexWriter(directory, indexWriterConfig);

	writer.commit();
    }

    /**
     * Adds the place.
     *
     * @param place
     *            the place
     */
    public void addPlace(final Place place) {
	if (CommonUtils.isNotNull(place)) {
	    try {
		final Document document = getPlaceDocument(place);
		writer.addDocument(document);
	    }
	    catch (final Exception e) {
		log.error("##Exception## occurred while adding document to Lucene.", e);
	    }
	    finally {
		if (CommonUtils.isNotNull(writer)) {
		    try {
			writer.commit();
		    }
		    catch (final IOException e) {
			log.error("##IOException## occurred while commiting and closing Lucene IndexWriter.", e);
		    }
		}
	    }
	}
    }

    public void updatePlace(final Place place) {
	if (CommonUtils.isNotNull(place)) {
	    try {
		final Document document = getPlaceDocument(place);
		writer.updateDocument(new Term("placeId", place.getPlaceId()), document);
	    }
	    catch (final Exception e) {
		log.error("##Exception## occurred while updating document in Lucene.", e);
	    }
	    finally {
		if (CommonUtils.isNotNull(writer)) {
		    try {
			writer.commit();
		    }
		    catch (final IOException e) {
			log.error("##IOException## occurred while commiting and closing Lucene IndexWriter.", e);
		    }
		}
	    }
	}
    }

    public void deletePlace(final Place place) {
	if (CommonUtils.isNotNull(place)) {
	    try {
		writer.deleteDocuments(new Term("placeId", place.getPlaceId()));
	    }
	    catch (final Exception e) {
		log.error("##Exception## occurred while deleting document from Lucene.", e);
	    }
	    finally {
		if (CommonUtils.isNotNull(writer)) {
		    try {
			writer.commit();
		    }
		    catch (final IOException e) {
			log.error("##IOException## occurred while commiting and closing Lucene IndexWriter.", e);
		    }
		}
	    }
	}
    }

    /**
     * Gets the place document.
     *
     * @param place
     *            the place
     * @return the place document
     */
    private Document getPlaceDocument(final Place place) {
	final Document document = new Document();

	final IndexableField id = new StringField("placeId", place.getPlaceId(), Field.Store.YES);
	final IndexableField name = new TextField("name", place.getName().toLowerCase(), Field.Store.YES);
	final IndexableField location = new LatLonPoint("location", place.getLatitude(), place.getLongitude());
	final IndexableField sortedLatLon = new LatLonDocValuesField("location", place.getLatitude(),
		place.getLongitude());

	document.add(id);
	document.add(name);
	document.add(location);
	document.add(sortedLatLon);

	if (!StringUtils.isNullOrEmptyStr(place.getCity())) {
	    final IndexableField city = new StringField("city", place.getCity(), Field.Store.YES);
	    document.add(city);
	}

	indexPlaceTypes(place, document);

	return document;
    }

    private void indexPlaceTypes(final Place place, final Document document) {
	for (final String type : place.getTypes()) {
	    final IndexableField typeField = new StringField("type", type, Field.Store.YES);
	    document.add(typeField);
	}
    }

    /**
     * Search places id by name and city.
     *
     * @param nameTokens
     *            the name tokens
     * @param city
     *            the city
     * @param count
     *            the count
     * @return the list
     */
    public List<String> searchPlacesIdByNameAndCity(final List<String> nameTokens, final String city, final int count,
	    final Location location) {
	List<String> placeIds = null;
	if (!CommonUtils.isNullOrEmptyCollection(nameTokens)) {
	    IndexReader reader = null;
	    try {
		// Create Reader
		reader = DirectoryReader.open(this.directory);

		// Create index searcher
		final IndexSearcher searcher = new IndexSearcher(reader);

		// Build query
		final Query query;

		if (StringUtils.isNullOrEmptyStr(city)) {
		    // query = new PrefixQuery(new Term("name", nameTokens.get(0)));
		    query = prepareWildCardQuery(nameTokens);
		}
		else {
		    final BooleanQuery.Builder builder = new BooleanQuery.Builder();
		    builder.add(prepareWildCardQuery(nameTokens), Occur.MUST);

		    final Query cityQuery = prepareCityQuery(city);
		    if (CommonUtils.isNotNull(cityQuery)) {
			builder.add(cityQuery, Occur.MUST);
		    }

		    query = builder.build();
		}

		// Search the index
		final TopDocs foundDocs = CommonUtils.isNotNull(location)
			? searcher.search(query, count, sort(location.getLatitude(), location.getLongitude()))
			: searcher.search(query, count);

		final ScoreDoc[] scoreDocs = foundDocs.scoreDocs;

		if (!CommonUtils.isNullOrEmptyArray(scoreDocs)) {
		    placeIds = new ArrayList<>(scoreDocs.length);

		    for (final ScoreDoc sd : scoreDocs) {
			final Document d = searcher.doc(sd.doc);
			placeIds.add(d.get("placeId"));
		    }
		}
	    }
	    catch (final IOException e) {
		log.error("##Exception## occurred while searching for places in Lucene.", e);
	    }
	    finally {
		if (CommonUtils.isNotNull(reader)) {
		    try {
			reader.close();
		    }
		    catch (final IOException e) {
			log.error("##IOException## occurred while closing Lucene IndexReader.", e);
		    }
		}
	    }
	}

	return placeIds;
    }

    private Sort sort(double latitude, double longitude) {
	return new Sort(LatLonDocValuesField.newDistanceSort("location", latitude, longitude));
    }

    private Query prepareWildCardQuery(final List<String> nameTokens) {
	final BooleanQuery.Builder builder = new BooleanQuery.Builder();

	nameTokens.forEach(token -> {
	    PrefixQuery prefixQuery = new PrefixQuery(new Term("name", token));

	    // FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("name", token), 2);

	    builder.add(prefixQuery, Occur.MUST);
	});

	return builder.build();
    }

    private Query prepareCityQuery(final String city) {
	QueryParser queryParser = new QueryParser("city", analyzer);
	Query parse;
	try {
	    parse = queryParser.parse(city);
	}
	catch (ParseException e) {
	    log.error("##ParseException## while preparing city query", e);
	    return null;
	}

	return parse;
    }

    /**
     * Find place ids in radius.
     *
     * @param latitude
     *            the latitude
     * @param longitude
     *            the longitude
     * @param radiusMeters
     *            the radius meters
     * @param types
     *            the types
     * @return the list
     */
    public List<String> findPlaceIdsInRadius(final Double latitude, final Double longitude, final Integer radiusMeters,
	    List<String> types, int count) {
	List<String> placeIds = null;
	if (CommonUtils.isNotNull(latitude, longitude, radiusMeters)) {
	    IndexReader reader = null;
	    try {
		// Create Reader
		reader = DirectoryReader.open(this.directory);

		// Create index searcher
		final IndexSearcher searcher = new IndexSearcher(reader);

		// create nearby Query
		final Query latLonQuery = LatLonPoint.newDistanceQuery("location", latitude, longitude, radiusMeters);

		Query query = null;
		if (!CommonUtils.isNullOrEmptyCollection(types)) {
		    final BooleanQuery.Builder builder = new BooleanQuery.Builder();
		    builder.add(latLonQuery, Occur.MUST);

		    final Query typesQuery = getTypesQuery(types);
		    if (CommonUtils.isNotNull(typesQuery)) {
			builder.add(typesQuery, Occur.MUST);
		    }

		    query = builder.build();
		}
		else {
		    query = latLonQuery;
		}

		// Search the index
		final TopDocs foundDocs = searcher.search(query, count, sort(latitude, longitude));

		final ScoreDoc[] scoreDocs = foundDocs.scoreDocs;

		if (!CommonUtils.isNullOrEmptyArray(scoreDocs)) {
		    placeIds = new ArrayList<>(scoreDocs.length);

		    for (final ScoreDoc sd : scoreDocs) {
			final String placeId = searcher.doc(sd.doc).get("placeId");
			if (!StringUtils.isNullOrEmptyStr(placeId)) {
			    placeIds.add(placeId);
			}
		    }
		}
	    }
	    catch (final IOException e) {
		log.error("##Exception## occurred while searching for nearby places in Lucene.", e);
	    }
	    finally {
		if (CommonUtils.isNotNull(reader)) {
		    try {
			reader.close();
		    }
		    catch (final IOException e) {
			log.error("##IOException## occurred while closing Lucene IndexReader.", e);
		    }
		}
	    }
	}

	return placeIds;
    }

    /**
     * Gets the types query.
     *
     * @param types
     *            the types
     * @return the types query
     */
    private Query getTypesQuery(final List<String> types) {
	final BooleanQuery.Builder builder = new BooleanQuery.Builder();
	final AtomicBoolean build = new AtomicBoolean(true);

	types.forEach(type -> {
	    try {
		final QueryParser qp = new QueryParser("type", analyzer);
		final Query query = qp.parse(type);
		builder.add(query, Occur.SHOULD);
	    }
	    catch (final ParseException e) {
		log.error("##ParseException## occurred while building types query", e);
		build.set(false);
	    }
	});

	return build.get() ? builder.build() : null;
    }

    public void destroy() {
	if (CommonUtils.isNotNull(writer)) {
	    try {
		writer.close();
	    }
	    catch (final IOException e) {
		log.error("##IOException## occurred while commiting and closing Lucene IndexWriter.", e);
	    }
	}
    }

}
