#ReshapeRDF - Guide

<table>
    <tr>
        <td>Author:</td>
        <td>Felix Bensmann</td>
    </tr>
    <tr>
        <td>Date:</td>
        <td>07. Dec. 2015</td>
    </tr>
    <tr>
        <td>Please note:</td>
        <td>This document may be incomplete.</td>
    </tr>
</table>


##TOC

* [Introduction](#introduction)
* [Sorted N-Triples](#sortedntriples)
* [Terms](#Terms)
* [Setup](#Setup)
* [Commands](#commands)
    * [analyzetype] (#cmd_analyzetype)
    * [block](#cmd_block)
    * [checksorting](#cmd_checksorting)
    * [correct](#cmd_correct)
    * [extractduplicatelinks] (#cmd_extractduplicatelinks)
    * [extractreferenced] (#cmd_extractreferenced)
    * [extractresources](#cmd_extractresources)
    * [filter] (#cmd_filter)
    * [getenrichment](#cmd_getenrichment)
    * [merge](#cmd_merge)
    * [mergedir](#cmd_mergedir)
    * [ntriplify](#cmd_ntriplify)
    * [outline] (#cmd_outline)
    * [pigeonhole] (#cmd_pigeonhole)
    * [pumpup](#cmd_pumpup)
    * [removeduplicates](#cmd_removeduplicates)
    * [renameproperty](#cmd_renameproperty)
    * [securelooseends] (#cmd_securelooseends)
    * [sort](#cmd_sort)
    * [split](#cmd_split)
    * [subtract](#cmd_subtract)
    * [version](#cmd_version)
    * [help](#cmd_help)
* [Getting Started](#gettingstarted)


##<a name="introduction"></a>Introduction
Processing RDF mass data can be a prone job. Common triplestores offer certain 
functionality for querying and manipulating RDF data but only few can handle 
mass data (let's say more than 200 Mio. statements) at the same time. 
Typical operations like data import and SPARQL queries tend to be time consuming 
and inconvenient to be used in comprehensive reshaping operations.

So, when working with simple structured graph data, a solution can be to 
refrain from using a triplestore and to work with dump files instead. 
Recurring tasks are *extracting entities* of a certain class from a large 
dataset, or *subdivide* a dataset into blocks according to a certain property 
(Blocking), *filtering* the data, *removing* resources
and single statements, *renaming* properties and similar reshaping operations.

Unfortunately organizing ones RDF mass data in the desired manner cannot be done 
easily with available out-of-the-box tools.

The tool at hand was developed to enable users of large RDF datasets to 
efficiently organize and reshape their data without the need of a triplestore. 



##<a name="sortedntriples"></a>Sorted N-Triples
When there is an RDF dump file to process, users cannot take for granted that
stored resources are held together. This is especially true for the N-Triple
file format but also applies for the RDF/XML file format that even provides a way to 
cluster statement by syntax.
At the same time resources within such files cannot be found efficiently without
having to read-in the whole file and examine the stream from the start to the end to find all
occurences. Complex searches cannot be handled at all.

To overcome these limitations this tool applies an intermediate file format to 
be used by a given set of operations to organize data in a more flexible way.
This format is "Sorted N-Triples" (SNT). These are, as the name already indicates, 
alphabetically sorted [N-Triples](http://www.w3.org/2001/sw/RDFCore/ntriples/).

The following example depicts how SNTs can be used for an interlinking and 
enrichment process.

1. Convert a non-SNT file to N-Triples
2. Sort it
3. Extract relevant resources (one iteration)
4. Split the extracted resources into smaller datasets (one iteration)
5. Interlink - however
6. If necessary convert the links to SNT
7. Merge the links into the data (one iteration)

The flexible nature of this tool is especially helpful with heterogeneous 
datasets.

##<a name="setup"></a>Setup

Copy the JAR-Archive reshaperdf-1.0-SNAPSHOT.jar and the lib folder to a directory of your choice.
The software requires at least JRE 1.7 .

##<a name="terms"></a>Terms

* **triple and statement** In this application a triple and a statement as known from the RDF context are the same thing. They always fit in one line.
* **line based** An operation is called that if it understands triples as a string line.
* **statement based** An operation is called that if it understands triples as triples/statements.
* **resource based** An operation is called that if it sees the data as individual resources respectively a set of statements.

##<a name="commands"></a>Commands

This chapter outlines the operations and their usage. A command can be called using the following syntax:

`java -jar reshaperdf-1.0-SNAPSHOT.jar <command> [<command parameter> ...]`

###<a name="cmd_analyzetype"></a>analyzetype

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>analyzetype</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>Usage: analyzetype &lt;infile&gt; &lt;type&gt; &lt;predicate1&gt; [&lt;predicate2&gt; ...]</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Counts the occurences of literal objects for a given rdf:type in combination with one or more properties. When more 
            properties are used, the combinations of properties are counted as well. Output is written to a CSV file. Use case example:
            a ranking of most common first name and last name combinations for persons could be created. 
            See also: <a href="#cmd_pigeonhole">pigeonhole</a></td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The input file, requires SNT.</td>
    </tr>
    <tr>
        <td>Argument: type</td>
        <td>The type of resource to be analyze e.g. foaf:Person</td>
    </tr>
    <tr>
        <td>Argument: predicate</td>
        <td>The property to examine. Requires long namespace version.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>One or more CSV files, automatically named.</td>
    </tr>
</table> 


###<a name="cmd_block"></a>block

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>block</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>block &lt;infile&gt; &lt;outputdir&gt; &lt;predicate&gt; </td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Separates the input file into blocks with resources that share the first letter of the properties value. Files that exceed a statement count of 100 000 are further splitted into parts of 100 000, except for the last of course.</td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The input file, requires SNT.</td>
    </tr>
    <tr>
        <td>Argument: outputdir</td>
        <td>Directory to store the output in.</td>
    </tr>
    <tr>
        <td>Argument: predicate</td>
        <td>The property to block by. Requires long namespace version.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>A set of SNT files in the given output directory.</td>
    </tr>
</table> 


###<a name="cmd_checksorting"></a>checksorting

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>checksorting</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>checksorting &lt;infile&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Checks the input file for proper sorting. This sorting differs from line sorting in that it ignores the control signs.</td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The input file, requires N-Triples.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>Prints "Sorted" to stdout if sorted correctly, "Not sorted" otherwise.</td>
    </tr>
</table> 




###<a name="cmd_correct"></a>correct

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>correct</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>correct &lt;infile&gt; &lt;outfile&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Line based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Removes invalid ntriples from a given file respectively Replaces invalid character with the ?-character. </td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The input file, requires N-Triples.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>Name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An N-Triples file with corrected content.</td>
    </tr>
</table> 


###<a name="cmd_extractduplicatelinks"></a>extractduplicatelinks

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>extractduplicatelinks</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>extractduplicatelinks &lt;infile&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Extracts statements with duplicate subjects and objects. Useful in combination with the <a href="#cmd_subtract">subtract</a> command.</td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The input file, requires SNT.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>Two N-Triples files: subjects.nt contains all links that do not address their subject exclusively; 
            objects.nt contains all links that do not address their objects exclusively.</td>
    </tr>
</table> 


###<a name="cmd_extractreferenced"></a>extractreferenced

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>extract referenced</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td> extractreferenced &lt;file A&gt; &lt;file B&gt; &lt;outfile&gt; &lt;predicate1&gt; [&lt;predicate2&gt; ...]</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Extracts resources from file B that are referenced in file A.</td>
    </tr>
    <tr>
        <td>Argument: file A</td>
        <td>The input file containing the references. SNT required.</td>
    </tr>
    <tr>
        <td>Argument: file B</td>
        <td>A second input file containing the referenced resources. SNT required.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>The name of the output file. This file will contain the extractes resources.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file containing the extracted resources.</td>
    </tr>
</table> 






###<a name="cmd_extractresources"></a>extractresources

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>extractresources</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>extractresources &lt;infile&gt; &lt;outfile&gt; &lt;predicate&gt; &lt;object&gt; &lt;offset&gt; &lt;length&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Extracts resources with a given predicate-object combination.</td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The input file, requires N-Triples.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>Name of the output file, the file with the extracted resources.</td>
    </tr>
    <tr>
        <td>Argument: predicate</td>
        <td>The predicate to look for, namespace has to be in long form. Use a "?" to indicate a wildcard.</td>
    </tr>
    <tr>
        <td>Argument: object</td>
        <td>The object to look for. Can be a literal or a URL. Use a "?" to indicate a wildcard.</td>
    </tr>
    <tr>
        <td>Argument: offset</td>
        <td>Number of the matching resource to start from.</td>
    </tr>
    <tr>
        <td>Argument: length</td>
        <td>Number of resources to extract. -1 indicates to use all available resources.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with the extracted resources.</td>
    </tr>
</table>


###<a name="cmd_filter"></a>filter

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>filter</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>filter &lt;whitelist|blacklist&gt; &lt;source file&gt; &lt;filter file&gt; &lt;outfile&gt; </td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Removes statments from an N-Triple file accoringly to a white or black list.</td>
    </tr>
    <tr>
        <td>Argument: whitelist|blacklist</td>
        <td>Keyword either whitelist to indicate that a whitelist is to be used or blacklist to indicate that a blacklist is to be used. Blacklist is not yet implemented.</td>
    </tr>
    <tr>
        <td>Argument: source file</td>
        <td>File to filter</td>
    </tr>
    <tr>
        <td>Argument: filter file</td>
        <td>A text file containing the properties to be subject to the filter. Is a simple line-based text file.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>Name of the file to store the output in.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with the remaining resources.</td>
    </tr>
</table>


###<a name="cmd_getenrichment"></a>getenrichment

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>getenrichment</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>getenrichment &lt;linkfile&gt; &lt;resource file&gt; &lt;outfile&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>statement based/Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Extracts resources from an SNT file, that are adressed by the object of an SNT link file. Missing resources in the resources file are ignored. The subjects of the extracted statements are altered to the subject of the link.</td>
    </tr>
    <tr>
        <td>Argument: linkfile</td>
        <td>The link file, requires SNT.</td>
    </tr>
    <tr>
        <td>Argument: resource file</td>
        <td>An SNT file containing the resources to be extracted.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>Name of the output file. The file with the extracted resources.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with the extracted resources.</td>
    </tr>
</table> 





###<a name="cmd_merge"></a>merge

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>merge</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>merge &lt;outfile&gt; &lt;infile1&gt; &lt;infile2&gt; [&lt;infile3&gt;...]</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Merges a couple of sorted NTriple files.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Argument: infile1</td>
        <td>An SNT file containing statements to be merged.</td>
    </tr>
    <tr>
        <td>Argument: infile2</td>
        <td>Another SNT file containing statements to be merged.</td>
    </tr>
    <tr>
        <td>Argument: infileN</td>
        <td>Further optional SNT files containing statements to be merged.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with the merged results.</td>
    </tr>
</table> 



###<a name="cmd_mergedir"></a>mergedir

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>mergedir</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>mergedir &lt;indir&gt; &lt;outfile&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Merges NTriple files that are in the same directory. Extends namespaces to its long form.</td>
    </tr>
    <tr>
        <td>Argument: indir</td>
        <td>The name of the directory containing the SNT files to be merged. Subdirectories are also searched.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>An SNT file containing the merged statements.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with the merged results.</td>
    </tr>
</table> 



###<a name="cmd_ntriplify"></a>ntriplify

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>ntriplify</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>ntriplify &lt;input dir&gt; &lt;outfile&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Takes an input directory and merges all RDF files into one N-Triple file.</td>
    </tr>
    <tr>
        <td>Argument: indir</td>
        <td>The name of the directory containing the RDF files. Subdirectories are also searched.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An N-Triple file containing the merged statements.</td>
    </tr>
</table> 


###<a name="cmd_outline"></a>outline

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>outline</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>outline &lt;infile&gt; &lt;outfile> &lt;target property></td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Creates literal representations for each resource in a file. 
            The representation is mapped to a given property. 
            See also: <a href="#cmd_securelooseends">securelooseends</a>  </td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The input file with the resource to be outlined. SNT required.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>The name of the file to store the output in.</td>
    </tr>
    <tr>
        <td>Argument: target property</td>
        <td>The property to assign the outline to.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>Name of the file to store the output in.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with one statement for each resource. &lt;original subject&gt; &lt;target property&gt; "literal representation"</td>
    </tr>
</table>



###<a name="cmd_pigeonhole"></a>pigeonhole

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>pigeonhole</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>pigeonhole &lt;infile&gt; &lt;outfile A&gt; &lt;outfile B&gt; &lt;outfile C&gt; &lt;CSV&gt; &lt;total threshold&gt;></td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Pigeonholes the resources within an SNT file according to the frequency of their attributes. 
            A CSV file contains information about a resource of a certain type and their respective properties, 
            combinations of these properties and their occurences. 
            The CSV file is the output of the analyzetype command. The command reads through the CSV input, 
            a threshold on the total column can be used to close out entries that are below this threshold. This reduces memory load.
            While reading through the SNT input file the resources are examined. Their property-combinations
            are looked up in the CSV table. If the resource does not contain all of the properties stated in the CSV then it is written to file C. 
            If a properties combination has an entry in the table then the resource is written to file A. 
            If a certain combination is not present in the table, then the resource is written to file B. 
            See also: <a href="#cmd_analyzetype">analyzetype</a>.  </td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The input file with the resources to be pigeonholed. SNT required.</td>
    </tr>
    <tr>
        <td>Argument: outfile A</td>
        <td>The name of the file to store the output in.</td>
    </tr>
     <tr>
        <td>Argument: outfile B</td>
        <td>The name of the file to store the output in.</td>
    </tr>
     <tr>
        <td>Argument: outfile C</td>
        <td>The name of the file to store the output in.</td>
    </tr>
    <tr>
        <td>Argument: CSV</td>
        <td>A CSV file containing the frequencies of the properties values. Same as the output of the analyzetype command.</td>
    </tr>
    <tr>
        <td>Argument: total threshold</td>
        <td>A positive integer to be used as lower threshold on the total frequencies column in the CSV. Can be used to close out uncommon values.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>Three SNT files with the  resources of their category. (See above)</td>
    </tr>
</table>



###<a name="cmd_pumpup"></a>pumpup

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>pumpup</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>pumpup &lt;infile&gt; &lt;outfile&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Extends the namespaces in an NTriple file to thier long forms. Uses the namespaces as stated below.</td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The name of the input file, requires N-Triples.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An N-Triple file containing the merged statements.</td>
    </tr>
</table> 

List of namespaces with their respective short forms.

* bf http://bibframe.org/vocab/
* bibo http://purl.org/ontology/bibo/
* dbp http://dbpedia.org/ontology/
* dc http://purl.org/dc/elements/1.1/
* dct http://purl.org/dc/terms/
* foaf http://xmlns.com/foaf/0.1/
* gnd http://d-nb.info/standards/elementset/gnd#
* owl http://www.w3.org/2002/07/owl#
* rdac http://rdaregistry.info/Elements/c/
* rdai http://rdaregistry.info/Elements/i/
* rdam http://rdaregistry.info/Elements/m/
* rdau http://rdaregistry.info/Elements/u/
* rdaw http://rdaregistry.info/Elements/w/
* rdf http://www.w3.org/1999/02/22-rdf-syntax-ns#
* rdfs http://www.w3.org/2000/01/rdf-schema#
* schema https://schema.org/
* skos http://www.w3.org/2004/02/skos/core#
* void http://rdfs.org/ns/void#
* sch http://schema.org/


###<a name="cmd_removeduplicates"></a>removeduplicates

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>removeduplicates</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>removeduplicates &lt;infile&gt; &lt;outfile&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Line based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Removes duplicate lines from an SNT file. Keeps one line of each kind.</td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The name of the input file, requires SNT.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file containing the remaining statements.</td>
    </tr>
</table> 

###<a name="cmd_renameproperty"></a>renameproperty

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>renamepropterty</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>renamepropterty &lt;infile&gt; &lt;outfile&gt; &lt;property&gt; &lt;substitute&gt; [&lt;property&gt; &lt;substitute&gt;...]</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Replaces properties with other properties. Requires long namespaces.</td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The name of the input file, requires SNT with long namespaces.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Argument: property</td>
        <td>The property to be replaced. Long namespace required.</td>
    </tr>
    <tr>
        <td>Argument: substitutes</td>
        <td>The substitute property. Long namespace required.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT a copy of the input file with replaced properties.</td>
    </tr>
</table> 


###<a name="cmd_securelooseends"></a>securelooseends

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>securelooseends</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>securelooseends &lt;file A&gt; &lt;file B&gt; &lt;outfile&gt; &lt;predicate1&gt; &lt;substitue1&gt;[&lt;predicate2&gt; ...]</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Extracts resources from file B that are referenced in file A. 
            Then reduces this resource to a meaningful string and adds it to the original resource.</td>
    </tr>
    <tr>
        <td>Argument: file A</td>
        <td>An SNT input file containing the references.</td>
    </tr>
    <tr>
        <td>Argument: file B</td>
        <td>An SNT input file containing the resources that are referenced in file A.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Argument: predicate1</td>
        <td>A property from file A whose reference is to be looked up in file B.</td>
    </tr>
    <tr>
        <td>Argument: substitute1</td>
        <td>A property to map the meaningful string to.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file containing the resulting statements. e.g. &lt;s&gt; &lt;substitute1&gt; "meaningful string"</td>
    </tr>
</table> 



###<a name="cmd_sort"></a>sort

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>sort</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>sort &lt;infile&gt; &lt;outfile&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Line based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Sorts an N-Triple file in ascending order of codepoints.</td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The name of the input file, requires N-Triples, requires long namspace forms.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file containing all the statements from the input file.</td>
    </tr>
</table> 



###<a name="cmd_split"></a>split

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>split</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>split &lt;infile&gt; &lt;outfile prefix&gt; &lt;resources per file&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Splits an SNT file into several smaller, with a given number of resources.</td>
    </tr>
    <tr>
        <td>Argument: infile</td>
        <td>The name of the input file, requires N-Triples</td>
    </tr>
    <tr>
        <td>Argument: outfile prefix</td>
        <td>Prefix for the output files, e.g. /home/data/part_  </td>
    </tr>
    <tr>
        <td>Argument: resources per file</td>
        <td>Number of resources per file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>Multiple SNT files, e.g. /home/data/part_1.nt etc.</td>
    </tr>
</table> 


###<a name="cmd_subtract"></a>subtract

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>subtract</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>subtract &lt;file A&gt; &lt;file B&gt; &lt;out file&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Line based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Removes all statements from file A that are also in file B.</td>
    </tr>
    <tr>
        <td>Argument: file A</td>
        <td>The name of the first file, requires SNT.</td>
    </tr>
    <tr>
        <td>Argument: file B</td>
        <td>The name of the second file, requires SNT.</td>
    </tr>
    <tr>
        <td>Argument: outfile</td>
        <td>Name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>The resulting file with SNT.</td>
    </tr>
</table> 


###<a name="cmd_version"></a>version

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>version</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>version</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>-</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Prints the version to screen, e.g. v0.1</td>
    </tr>
</table> 




###<a name="cmd_help"></a>help

<table border="1" style="width:100%">
    <tr>
        <td>Name</td>
        <td>help</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>help &lt;cmd&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>-</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Displays the help text, for the specified command.</td>
    </tr>
    <tr>
        <td>Argument: cmd</td>
        <td>Name of the command.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>Helptext for the specified command.</td>
    </tr>
</table> 

##<a name="gettingstarted"></a>Getting Started

Some steps to get started:

1. Prepare your data in a single directory, have it in one of these formats: .nt, .rdf, .xml, .jsonld.
2. Convert your data to N-Triples if not already in use.  
`java -jar reshaperdf-1.0-SNAPSHOT.jar ntriplify ./myrdf ./nt/mydata.nt`
3. Sort your data.  
`java -jar reshaperdf-1.0-SNAPSHOT.jar sort ./nt/mydata ./nt/mydata_sorted.nt`
4. Extract all persons (foaf:Person) from the file into another file.  
`java -jar reshaperdf-1.0-SNAPSHOT.jar extract ./nt/mydata_sorted.nt ./nt/mypersons.nt http://xmlns.com/foaf/0.1/Person ? 0 -1`

