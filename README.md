# Relation Extraction
Library to create vertices from labeled entities, and edges from interpreted relationships between entities that are within unstructured text.

## Dependency
The Relation Extraction library shares object models with the Entity Extraction library. To install the dependency locally:

	mvn scm:checkout -Dmodule.name=entity-extractor
	cd entity-extractor
	mvn clean install

## Input
* Output from the [Entity-Extractor](https://github.com/stucco/entity-extractor/tree/corenlp) as an Annotation object, which represents the sentences, list of words from the text, along with each word's part of speech tag and cyber domain label.
* The source of the unstructured text

## Current Process
* For each word, or phrase, labeled with a domain label:
	* Search this sentence's set of previously created vertices, starting with the most recent
		* If a vertex is found with the same type and it does not have the property specified by the current domain label, then update this vertex's properties with the current word/phrase
		* Otherwise, create a new vertex of the corresponding type and with the appropriate property, then add it to the end of the set of vertices created for this sentence
	
* For each sentence:
	* For every pair of vertices:
		* If there is an edge defined for the two types of vertices, then create an edge between them in the subgraph
	
## Output
* The GraphSON formatted subgraph of the vertices and edges created

## Usage
	EntityLabeler labeler = new EntityLabeler();
	Annotation doc = labeler.getAnnotatedDoc("My Doc", exampleText);
	RelationExtractor rx = new RelationExtractor();
	String graph = rx.createSubgraph("My source", doc);
	
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