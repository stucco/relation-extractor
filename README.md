# Relation Extraction
Library to create vertices and edges from a document annotated with cyber-entity labels and a set of SVMs and feature models to predict relationships between these cyber entities.

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
* ExploitTargetRelatedObservable Edge

		Exploit Target (e.g. vulnerability) --> Observable (e.g. software)
	
* Sub-Observable Edge

		Observable (e.g. software) --> Observable (e.g. file)
	
* Software, File, Function, Vulnerability Vertices

		Software/file/function/vulnerability properties are part of the same vertex
		
		Example: "... **MS15-035**, which addresses a **remote code execution** bug ..."
		"MS15-035" is extracted as a vulnerability MS property, and "remote code execution" is extracted as a vulnerability description property. This type of relationship indicates that both properties are describing the same vulnerability object.


## Input
* Output from the [Entity-Extractor](https://github.com/stucco/entity-extractor) as an Annotation object, which represents the sentences, list of words from the text, along with each word's part of speech tag and cyber domain label.
* The String name of the document's source
* The String name of the document's title
	

## Current Process
* Pre-trained Word2Vec model
* Pre-trained SVM models, one for each relationship and entities' order of appearance
* Pre-generated feature maps, one for each relationship and enities' order of appearance
* NVD XML files are used to find examples of the relationships
* For each Annotated document:
	1. Use NVD files to find known examples of relationships in document
	2. Use Word2Vec model to encode each token of the document
	3. Use feature maps to generate feature vectors for each token of the document
	4. Use pre-trained SVM models with the document's feature vectors to predict relationships between cyber entities

Note: Refer to [relation-bootstrap repo](https://github.com/stucco/relation-bootstrap) for more information on the process.
	
## Output
* A JSON-formatted subgraph of the vertices and edges is created, which loosely resembles the STIX data model
	
	```
	{
		"vertices": {
			"1235": {
				"name": "1235",
				"vertexType": "software",
				"product": "Windows XP",
				"vendor": "Microsoft",
				"source": "CNN"
			},
			...
			"1240": {
				"name": "file.php",
				"vertexType": "file",
				"source": "CNN"
			}
		},
		"edges": [
			{
				"inVertID": "1237",
				"outVertID": "1238",
				"relation": "ExploitTargetRelatedObservable"
			},
			{
				"inVertID": "1240",
				"outVertID": "1239",
				"relation": "Sub-Observable"
			}
		]
	}
	```

## Usage
	EntityLabeler labeler = new EntityLabeler();
	Annotation doc = labeler.getAnnotatedDoc("My Doc", exampleText);
	RelationExtractor rx = new RelationExtractor();
	String graph = rx.createSubgraph(doc, "My source","My Doc");
	
## Test
1) Install the dependency as described [above](https://github.com/stucco/relation-extractor#dependency)

2) Run the command:

	mvn test -Dmaven.test.skip=false
	
## License
This software is freely distributable under the terms of the MIT License.

Copyright (c) UT-Battelle, LLC (the "Original Author")

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS, THE U.S. GOVERNMENT, OR UT-BATTELLE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
