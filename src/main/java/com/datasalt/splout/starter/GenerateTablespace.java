package com.datasalt.splout.starter;

import static com.datasalt.pangool.tuplemr.mapred.lib.input.TupleTextInputFormat.NO_ESCAPE_CHARACTER;
import static com.datasalt.pangool.tuplemr.mapred.lib.input.TupleTextInputFormat.NO_QUOTE_CHARACTER;

import java.io.File;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.datasalt.pangool.io.Fields;
import com.datasalt.pangool.io.Schema;
import com.splout.db.common.SploutHadoopConfiguration;
import com.splout.db.hadoop.Table;
import com.splout.db.hadoop.TableBuilder;
import com.splout.db.hadoop.TableBuilder.TableBuilderException;
import com.splout.db.hadoop.TablespaceBuilder;
import com.splout.db.hadoop.TablespaceBuilder.TablespaceBuilderException;
import com.splout.db.hadoop.TablespaceGenerator;
import com.splout.db.hadoop.TablespaceSpec;
import com.splout.db.hadoop.TupleSampler.DefaultSamplingOptions;
import com.splout.db.hadoop.TupleSampler.SamplingType;

/**
 * Please read the README.md file in this project before starting!! it contains important information for environment setup.
 * <p>
 * A sample Java class that uses splout-Hadoop API. Usually you will use Hadoop's Tool for receiving the Hadoop
 * Configuration.
 * <p>
 * In this class you can call your Java Hadoop flow, Java MapReduce, Cascading, etc. Or perform some special ETL and
 * then call splout-hadoop to deploy it to Splout, etc.
 * <p>
 * We will deploy an example "hashtags" dataset with two tables. one will contain the counts for every hashtag, per day.
 * The other file will be a "geonames" file containing a mapping of words that are locations. For scaling, we will
 * partition the hashtags file by hashtag. On the other hand, we will replicate the geonames information in each
 * partition.
 * <p>
 * For a more advanced usage of the splout-hadoop API (e.g. custom RecordProcessor) check the wikipedia PageCounts
 * example ({@link https://github.com/datasalt/splout-db/blob/master/splout-hadoop/src/main/java/com/splout/db/examples/
 * PageCountsExample.java})
 */
public class GenerateTablespace implements Tool {

	// the hadoop configuration
	private Configuration hadoopConf;

	@Override
	public Configuration getConf() {
		return hadoopConf;
	}

	@Override
	public void setConf(Configuration hadoopConf) {
		this.hadoopConf = hadoopConf;
	}

	public final static String INDEXED_FILES_OUT_DIR = "splout-starter-indexed-tablespace";

	private TablespaceSpec getTablespaceSpec() throws TableBuilderException, TablespaceBuilderException {
		// All splout-hadoop app. building begins by a TablespaceBuilder!
		TablespaceBuilder tablespaceBuilder = new TablespaceBuilder();

		// Because we will use TEXT files, we need to define a Pangool schema for them
		// This is the schema of the "hashtags" file
		Schema hashtagsSchema = new Schema("hashtags",
		    Fields.parse("ignore:string,date:string,count:long,hashtag:string"));
		// This is the schema of the "geonames" file
		Schema geonamesSchema = new Schema("geonames", Fields.parse("altname:string,canonical:string"));

		// tablespaceBuilder receives tables that need to be built with TableBuilder
		TableBuilder hashTagsTableBuilder = new TableBuilder(hashtagsSchema);
		// special CSV with space as separator
		hashTagsTableBuilder.addCSVTextFile("src/main/resources/hashtags_space.txt", ' ',
		    NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER, false, false, null);
		// create a B-Tree on hashtag+date
		hashTagsTableBuilder.createIndex("hashtag", "date");
		// partitioned table by "hashtag"
		hashTagsTableBuilder.partitionBy("hashtag");

		Table hashTagsTable = hashTagsTableBuilder.build();

		TableBuilder geonamesTableBuilder = new TableBuilder(geonamesSchema);
		// standard text file with tab. as separation and no quotes
		geonamesTableBuilder.addCSVTextFile("src/main/resources/geonames.txt");
		// this table will be replicated in each partition
		// (this is just for illustrative purposes, we could also partition it by "altname".)
		geonamesTableBuilder.replicateToAll();

		Table geonamesTable = geonamesTableBuilder.build();

		tablespaceBuilder.add(hashTagsTable);
		tablespaceBuilder.add(geonamesTable);
		// finally, set the number of partitions we want
		tablespaceBuilder.setNPartitions(4);

		TablespaceSpec spec = tablespaceBuilder.build();

		return spec;
	}

	@Override
	public int run(String[] ignoreArgs) throws Exception {
		TablespaceSpec specToBuild = getTablespaceSpec();

		FileSystem fS = FileSystem.get(hadoopConf);

		// Only for distributed mode: Add sqlite native libs to DistributedCache
		if(!FileSystem.getLocal(hadoopConf).equals(fS)) {
			// When launching the app, the "native" folder must contain the SQLite native libraries
			// This is achieved by the maven assembly task.
			SploutHadoopConfiguration.addSQLite4JavaNativeLibsToDC(hadoopConf);
		}

		// delete indexed files if they already exist
		Path indexedFiles = new Path(INDEXED_FILES_OUT_DIR);
		if(fS.exists(indexedFiles)) {
			fS.delete(indexedFiles, true);
		}

		TablespaceGenerator generator = new TablespaceGenerator(specToBuild, indexedFiles, this.getClass());
		// we do a simple sampling process - but we could also do RESERVOIR sampling
		generator.generateView(hadoopConf, SamplingType.DEFAULT, new DefaultSamplingOptions());

		// files are now generated and ready to be deployed!
		return 1;
	}

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new GenerateTablespace(), args);
	}
}
