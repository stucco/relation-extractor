# Relation Extraction
Library to create vertices and edges from a document annotated with cyber-entity labels and a list of relationship patterns between these cyber entities.

## Dependency
The Relation Extraction library shares object models with the Entity Extraction library. To install the dependency locally:

	mvn scm:checkout -Dmodule.name=entity-extractor
	cd entity-extractor
	mvn clean install

## Entity Types
* Software
	* Vendor
	* Product
	* Version
* File
	* Name
* Function
	* Name
* Vulnerability
	* Name
	* Description
	* CVE
	* MS
	
## Relationship Types
* Exploit_Target_Related_Observable

		Exploit Target (e.g. vulnerability) --> Observable (e.g. software)
	
* Sub_Observable 

		Observable (e.g. software) --> Observable (e.g. file)
	
* Software 

		Software properties are part of the same Object
	
* Vulnerability 

		Vulnerability properties are part of the same Object
	

## Relationship Patterns
These patterns are defined in a JSON-formatted file. There are three types of patterns we will need to find:

* The exact-match pattern consists of at least two cyber-entity labels, and words or part-of-speech (pos) tags.  The annotated document must match the pattern exactly with no extra words. 
	
		Example Text: “Tomcat from Apache”
		Pattern: SW.PRODUCT, "from", SW.VENDOR
	
* The fuzzy-match pattern begins and ends with cyber-entity labels, but it allows for extra words to be present in the document that are not reflected in the pattern. The words, or pos tags, in the pattern must appear in the annotated document, but there can be extra words as well. The only restriction is the two entities are no more than 10 words apart in the document.
	
		Example Text: “Apache Tomcat’s latest update 8.0.22”
		Pattern: SW.PRODUCT, “update”, SW.VERSION
	
* The parse-tree-path pattern defines a path through a sentence’s parse tree, where the first and last elements of the path are labeled cyber entities. 
	
		Example Parse Tree: (NP (NP (NNP Apache) (NNP Tomcat) (POS 's)) (JJS latest))
		Pattern: NNP, NP, NNP
	
The majority of the knowledge graph's ontology is defined within this file, so the file can be modified while the extractor's mechanics remain the same. The file format is as follows:

	{"Patterns": [
	{"edgeType": "Exploit_Target_Related_Observable", "patternType": "ExactPattern", "patternSequence": [{"class": "CyberEntity", "value": "sw.product", "vType": "inV"}, {"class": "Token", "value": "update"}, {"class": "Token", "value": "-LRB-"}, {"class": "CyberEntity", "value": "vuln.name", "vType": "outV"}]},
	{"vertexType": "software", "patternType": "ExactPattern", "patternSequence": [{"class": "Token", "value": "versions"}, {"class": "Token", "value": "of"}, {"class": "CyberEntity", "value": "sw.product"}, {"class": "Token", "value": "are"}, {"class": "CyberEntity", "value": "sw.version"}, {"class": "POS", "value": "IN"}, {"class": "CyberEntity", "value": "sw.version"}]},
	{"vertexType": "software", "patternType": "ParseTreePattern", "patternSequence": [{"class": "CyberEntity", "value": "sw.product"}, {"class": "TreeElement", "value": "NNP"}, ...]},
	...
	] }


## Input
* Output from the [Entity-Extractor](https://github.com/stucco/entity-extractor/tree/corenlp) as an Annotation object, which represents the sentences, list of words from the text, along with each word's part of speech tag and cyber domain label.
* The String name of the document's source
* A JSON file containing the set of patterns to find in the annotated document
	

## Current Process
* For each pattern:
	* Search the document for candidate instances and for each instance:
		* If the pattern type is ExactPattern, then all tokens listed in the pattern sequence must match
		* If the pattern type is FuzzyPattern, then all the cyber entities must match those in the pattern sequence with no more than 10 words between them, but there can be other tokens in the document that are not represented in the pattern sequence
		* If the pattern type is ParseTreePattern, then the pattern sequence represents a path, between cyber entities, in the parse tree and all elements must match exactly

	
## Output
* A JSON-formatted subgraph of the vertices and edges is created, which loosely resembles the STIX data model
	
	```
	{ "vertices" : 
		{
			id1 : { "vertexType": "software", "vendor": "Apache", "product": "Tomcat"},
			id2 : { "vertexType": "vulnerability", "description": "buffer overflow"},
			...
		},
	  "edges" : 
	  	[
	  		{"id": "vuln_Tomcat_1234", "inV": "id1", "outV": "id2", "label": "Exploit_Target_Related_Observable"},
			...
	  	]
	}
	```

## Usage
	EntityLabeler labeler = new EntityLabeler();
	Annotation doc = labeler.getAnnotatedDoc("My Doc", exampleText);
	RelationExtractor rx = new RelationExtractor();
	String graph = rx.createSubgraph(doc, "My source");
	
## Test
1) Install the dependency as described [above](https://github.com/stucco/relation-extractor#dependency)

2) Run the command:

	mvn test
	
## License
This software is freely distributable under the terms of the MIT License.

Copyright (c) UT-Battelle, LLC (the "Original Author")

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS, THE U.S. GOVERNMENT, OR UT-BATTELLE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.