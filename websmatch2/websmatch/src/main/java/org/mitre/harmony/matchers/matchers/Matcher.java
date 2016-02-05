// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.matchers.matchers;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.mitre.harmony.matchers.MatchTypeMappings;
import org.mitre.harmony.matchers.MatcherOption;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;

/** Matcher Interface - A matcher scores source-target linkages based on a specific algorithm */	
public abstract class Matcher
{
	// Constants for the major option names
	protected static final String NAME = "UseName";
	protected static final String DESCRIPTION = "UseDescription";
	protected static final String THESAURUS = "UseThesaurus";
	protected static final String HIERARCHY = "UseHierarchy";
	
	// matchers for factory
	protected static final String LEVEN = "Levenshtein";
	protected static final String SMITH = "SmithWaterman";
	protected static final String SMITHGT = "SmithWatermanGotoh";
	protected static final String SMITHGTW = "SmithWatermanGotohWindowedAffine";
	protected static final String JARO = "Jaro";
	protected static final String JAROW = "JaroWinkler";
	protected static final String STOILOS = "Stoilos_JW";
	protected static final String QGRAMS = "QGramsDistance";
	protected static final String MONGE = "MongeElkan";
	protected static final String WUPALMER = "WuPalmer";
	protected static final String LIN = "Lin";
	protected static final String MULTI = "MultiLevelMatcher";
	protected static final String SOFTTFIDF = "SoftTFIDF";
	protected static final String SOFTTFIDFWN = "SoftTFIDFWordNet";
	
	// Stores the match merger schema information
	protected FilteredSchemaInfo schema1, schema2;

	/** Stores the match merger type mapping information */
	private MatchTypeMappings types;

	/** Stores the options specified for this matcher */
	protected LinkedHashMap<String, MatcherOption> options = new LinkedHashMap<String,MatcherOption>();

	/** Stores if this is a default matcher */
	private boolean isDefault = false;

	/** Stores if this is a hidden matcher */
	private boolean isHidden = false;

	// Stores the completed and total number of comparisons that need to be performed
	protected int completedComparisons = 0, totalComparisons = 1;

	/** Constructs the matcher */
	public Matcher()
	{
		for(MatcherOption option : getMatcherOptions())
			options.put(option.getName(), option);
	}
	
	/** Return the name of the matcher */
	abstract public String getName();

	/** Indicates if the matcher needs a repository client */
	public boolean needsClient() { return false; }
	
	/** Returns the list of options associated with the matcher */
	protected ArrayList<MatcherOption> getMatcherOptions() { return new ArrayList<MatcherOption>(); }
	
	// Matcher getters
	final public boolean isDefault() { return isDefault; }
	final public boolean isHidden() { return isHidden; }

	// Matcher setters
	final public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
	final public void setHidden(boolean isHidden) { this.isHidden = isHidden; }

	/** Initializes the matcher */
	final public void initialize(FilteredSchemaInfo schema1, FilteredSchemaInfo schema2)
		{ this.schema1 = schema1; this.schema2 = schema2; this.types = null; }

	/** Initializes the matcher */
	final public void initialize(FilteredSchemaInfo schema1, FilteredSchemaInfo schema2, MatchTypeMappings types)
		{ this.schema1 = schema1; this.schema2 = schema2; this.types = types; }

	/** Gets the list of options */
	final public ArrayList<MatcherOption> getOptions()
		{ return new ArrayList<MatcherOption>(options.values()); }
	
	/**Gets the specified matcher option */
	final public String getOption(String name)
	{
		MatcherOption option = options.get(name);
		return option!=null ? option.getValue() : null;
	}
	
	/** Sets the specified option */
	final public void setOption(String name, String value)
	{
		MatcherOption option = options.get(name);
		if(option!=null) option.setValue(value);
	}
	
	/** Generates scores for the specified graphs */
	abstract public MatcherScores match();

	/** Indicates if the specified elements can validly be mapped together */
	final protected boolean isAllowableMatch(SchemaElement element1, SchemaElement element2)
		{ return types == null || types.isMapped(element1, element2); }

	/** Indicates the completion percentage of the matcher */
	final public double getPercentComplete()
		{ return 1.0 * completedComparisons / totalComparisons; }
}