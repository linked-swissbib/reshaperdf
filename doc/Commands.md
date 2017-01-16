# ReshapeRDF - Guide
A toolset to work with N-Triples

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
        <td>Last change:</td>
        <td>16. Jan. 2016</td>
    </tr>
    <tr>
        <td>Please note:</td>
        <td>This document is intended to provide help to get started with ReshapeRDF, nothing more.<br>
	Its content is subject to change.
    </td>
    </tr>
</table>


## TOC

* [Introduction](#sec:introduction)
* [Sorted N-Triples](#sec:sortedntriples)
* [Terms](#sec:terms)
* [Setup](#sec:setup)
* [Commands](#sec:commands)
    * [Commands for everyday use](#sec:everydayuse)
        * [block](#cmd:block)
        * [checksorting](#cmd:checksorting)
        * [extractresources](#cmd:extractresources)
        * [filter](#cmd:filter)
        * [getenrichment](#cmd:getenrichment)
        * [help](#cmd:help)
        * [merge](#cmd:merge)
        * [mergedir](#cmd:mergedir)
        * [ntriplify](#cmd:ntriplify)
        * [pick](#cmd:pick)
        * [removeduplicates](#cmd:removeduplicates)
        * [renameproperty](#cmd:renameproperty)
        * [securelooseends](#cmd:securelooseends)
        * [sort](#cmd:sort)
        * [split](#cmd:split)
        * [version](#cmd:version)
    * [Special commands](#sec:special)
        * [analyzetype](#cmd:analyzetype)
        * [correct](#cmd:correct)
        * [extractduplicatelinks](#cmd:extractduplicatelinks)
        * [extractreferenced](#cmd:extractreferenced)
        * [outline](#cmd:outline)
        * [pigeonhole](#cmd:pigeonhole)
        * [pumpup](#cmd:pumpup)
        * [subtract](#cmd:subtract)
* [Getting Started](#sec:gettingstarted)


## Introduction <a name="sec:introduction"></a>
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



## Sorted N-Triples <a name="sec:sortedntriples"></a>
When there is an RDF dump file to process, users cannot take for granted that
stored resources are held together. This is especially true for the N-Triples
file format but also applies for the RDF/XML file format that even provides a way to 
cluster statement by syntax.
At the same time resources within such files cannot be found efficiently without
having to read-in the whole file and examine the stream from the start to the end to find all
occurences. Complex searches cannot be handled at all.

To overcome these limitations this tool applies an intermediate file format to 
be used by a given set of operations to organize data in a more flexible way.
This format is "Sorted N-Triples" (SNT). These are, as the name already indicates, 
alphabetically sorted [N-Triples](https://www.w3.org/TR/2014/REC-n-triples-20140225/).

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

## Setup <a name="sec:setup"></a>

Copy the JAR-Archive reshaperdf-1.0-SNAPSHOT.jar and the lib folder to a directory of your choice.
The software requires at least JRE 1.7 .

It is helpful to provide a script "reshaperdf" in /bin that facilitates the calls to the program.


    #!/bin/bash
    # Author:  John Smith
    # Purpose: Facilitates calls to ReshapeRDF.
    
    java -jar reshaperdf-1.0-SNAPSHOT.jar $@

 

## Terms <a name="sec:terms"></a>

* **triple and statement** In this application a triple and a statement as known from the RDF context are the same thing. They always fit in one line.
* **line based** An operation is called that if it understands triples as a string line.
* **statement based** An operation is called that if it understands triples as triples/statements.
* **resource based** An operation is called that if it sees the data as a list of individual resources.



## Commands <a name="sec:commands"></a>

This chapter outlines the operations and their usage. A command can be called using the following syntax:

`java -jar reshaperdf-1.0-SNAPSHOT.jar <command> [<command parameter> ...]`

The chapter is subdivided into a section that is about commands intended for 
everyday use and a section about special commands that do not have a 
purpose in everyday use but become handy in exotic use cases.
The special commands are available in their own branch.

At no point any of the commands will overwrite an input file, 
rather they produce a new file with the desired changes. 
However existing files will be overwritten by output files without notification.

Comments are usually not processed by the commands.
Most commands require the long forms of a URI.


### Commands for everyday use <a name="sec:everydayuse"></a>


#### block <a name="cmd:block"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>block</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>block &lt;input file&gt; &lt;output dir&gt; &lt;predicate&gt; &lt;char offset&gt; &lt;char length&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Assigns the resources of the input file to blocks according to a given character sequence of a given property's value. One block is one file. Files that exceed a statement count of 100 000 are further split into files of 100 000.</td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The input file, requires SNT.</td>
    </tr>
    <tr>
        <td>Argument: output dir</td>
        <td>The Directory to store the output in.</td>
    </tr>
    <tr>
        <td>Argument: predicate</td>
        <td>The property to block by. Requires long namespace version.</td>
    </tr>
    <tr>
        <td>Argument: char offset</td>
        <td>The offset of the character sequence in the property's value. Use 0 for no offset. If the offset is higher than the value's length, then the whole property value will be evaluated.</td>
    </tr>
    <tr>
        <td>Argument: char length</td>
        <td>The lenght of the character sequence in the property's value. If the length is higer than the value's length, then the whole property value will be evaluated.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>A set of SNT files in the given output directory.</td>
    </tr>
</table> 


#### checksorting <a name="cmd:checksorting"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>checksorting</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>checksorting &lt;input file&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Checks the input file for proper sorting. This sorting differs from line sorting in the fact that it ignores the control characters.</td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The input file, requires N-Triples.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>Prints "Sorted" to stdout if sorted correctly, "Not sorted" otherwise.</td>
    </tr>
</table> 



#### extractresources <a name="cmd:extractresources"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>extractresources</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>extractresources &lt;input file&gt; &lt;output file&gt; &lt;predicate&gt; &lt;object&gt; &lt;offset&gt; &lt;length&gt;</td>
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
        <td>Argument: input file</td>
        <td>The input file, requires N-Triples.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
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
See also [pick](#cmd:pick).


#### filter <a name="cmd:filter"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>filter</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>filter &lt;whitelist|blacklist&gt; &lt;input file&gt; &lt;filter file&gt; &lt;output file&gt; </td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Removes statments from an N-Triples file accoringly to a white or black list.</td>
    </tr>
    <tr>
        <td>Argument: whitelist|blacklist</td>
        <td>Either "whitelist" or "blacklist" to indicate what kind of filter is to be used.</td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>File to filter</td>
    </tr>
    <tr>
        <td>Argument: filter file</td>
        <td>A text file containing the properties to be subject to the filter. Is a simple line-based text file.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>Name of the file to store the output in.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with the remaining resources.</td>
    </tr>
</table>


#### getenrichment <a name="cmd:getenrichment"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>getenrichment</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>getenrichment &lt;linkfile&gt; &lt;resource file&gt; &lt;output file&gt;</td>
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
        <td>Argument: output file</td>
        <td>Name of the output file. The file containing the extracted resources.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with the extracted resources.</td>
    </tr>
</table> 

See also [extractreferenced](#cmd:extractreferenced).


#### help <a name="cmd:help"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
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


#### merge <a name="cmd:merge"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>merge</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>merge &lt;output file&gt; &lt;input file1&gt; &lt;input file2&gt; [&lt;input file3&gt;...]</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Merges a couple of sorted N-Triples files.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Argument: input file1</td>
        <td>An SNT file containing statements to be merged.</td>
    </tr>
    <tr>
        <td>Argument: input file2</td>
        <td>Another SNT file containing statements to be merged.</td>
    </tr>
    <tr>
        <td>Argument: input fileN</td>
        <td>Further optional SNT files containing statements to be merged.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with the merged results.</td>
    </tr>
</table> 
For a simple concatenation you may also try "$ cat a.nt b.nt c.nt > mergefile.nt" in a Linux environment.

#### mergedir <a name="cmd:mergedir"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>mergedir</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>mergedir &lt;input dir&gt; &lt;output file&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Merges SNT files that are in the same directory. Extends namespaces to its long form.</td>
    </tr>
    <tr>
        <td>Argument: input dir</td>
        <td>The name of the directory containing the SNT files to be merged. Subdirectories are also searched.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>An SNT file containing the merged statements.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with the merged results.</td>
    </tr>
</table> 
For a simple concatenation you may also try "$ cat *.nt > mergefile.nt" in a Linux environment.



#### ntriplify <a name="cmd:ntriplify"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>ntriplify</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>ntriplify &lt;input dir&gt; &lt;output file&gt; [&lt;JSON-LD context URI&gt; &lt;JSON-LD context file&gt;][...]</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Converts all RDF files from a directory into N-Triples and merges them into a single file.</td>
    </tr>
    <tr>
        <td>Argument: input dir</td>
        <td>The name of the directory containing the RDF files. Subdirectories are also searched.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Argument: JSON-LD context URIs and files</td>
        <td>Optional. It is possible to state a mapping of JSON-LD contexts and local JSON-LD context files. 
            The context-URIs and file paths will have to be inserted in pairs separated by a space. 
            The command will use the local contexts whenever the remote context is not available.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An N-Triples file containing the converted statements.</td>
    </tr>
</table> 


#### pick <a name="cmd:pick"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>pick</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>pick &lt;input file&gt; &lt;output file&gt; &lt;s|p|o|stmt|res&gt; &lt;s|list|?&gt; &lt;p|list|?&gt; &lt;o|list|?&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Dependant on search pattern</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Takes an input file and extracts all subjects, predicates, objects, statements or resources according to the specified pattern and outputs them into a file. 
            A "?"-character can be used to indicate a wildcard.
                Example: infile.nt outfile.nt o subjectlist.txt predicatelist.txt ?
            This returns all objects whose statments match any combination of subjectlist and predicatelist. 
        </td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The name of the input file. Sorted N-Triples are required.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Argument: return type</td>
        <td>The kind of information to be returned; one of subject, predicate, object, statement or resource.</td>
    </tr>
    <tr>
        <td>Argument: subject expression</td>
        <td>The expression for matching the subject: A single URL, a file containing a list of URLs or a wildcard.</td>
    </tr>
    <tr>
        <td>Argument: predicate expression</td>
        <td>The expression for matching the predicate: A single URL, a file containing a list of URLs or a wildcard.</td>
    </tr>
    <tr>
        <td>Argument: object expression</td>
        <td>The expression for matching the subject: A single URL/Literal, a file containing a list of URLs or literals or a wildcard. 
            Datatypes and language tags cannot be processed.
        </td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An N-Triples file containing the output.</td>
    </tr>
</table> 


#### removeduplicates <a name="cmd:removeduplicates"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>removeduplicates</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>removeduplicates &lt;input file&gt; &lt;output file&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Line based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Removes duplicate statements from an SNT file. Keeps one line of each kind.</td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The name of the input file, requires SNT.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file containing the remaining statements.</td>
    </tr>
</table> 


#### renameproperty <a name="cmd:renameproperty"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>renameproperty</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>renameproperty &lt;input file&gt; &lt;output file&gt; &lt;property&gt; &lt;substitute&gt; [&lt;property&gt; &lt;substitute&gt;...]</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Renames a property. Requires long namespaces.</td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The name of the input file, requires SNT with long namespaces.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
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


#### securelooseends <a name="cmd:securelooseends"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>securelooseends</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>securelooseends &lt;file A&gt; &lt;file B&gt; &lt;output file&gt; &lt;predicate1&gt; &lt;substitue1&gt;[&lt;predicate2&gt; ...]</td>
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
        <td>Argument: output file</td>
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



#### sort <a name="cmd:sort"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>sort</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>sort &lt;input file&gt; &lt;output file&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Sorts an N-Triples file in ascending order of codepoints.</td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The name of the input file, requires N-Triples, requires long namspace forms.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file containing all the statements from the input file.</td>
    </tr>
</table> 
See also [checksorting](#cmd:checksorting).


#### split <a name="cmd:split"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>split</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>split &lt;input file&gt; &lt;output file prefix&gt; &lt;resources per file&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Splits an SNT file into several smaller files, with a given number of resources.</td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The name of the input file, requires SNT.</td>
    </tr>
    <tr>
        <td>Argument: output file prefix</td>
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


#### version <a name="cmd:version"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
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
        <td>Prints the version to the screen, e.g. v0.1 .</td>
    </tr>
</table> 





### Special commands <a name="sec:special"></a>




#### analyzetype <a name="cmd:analyzetype"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>analyzetype</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>Usage: analyzetype &lt;input file&gt; &lt;type&gt; &lt;predicate1&gt; [&lt;predicate2&gt; ...]</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Counts the occurences of literal objects for one or more propertiesfor for a given rdf:type. When more than one 
            properties are used, the combinations of properties are counted as well. Output is written to a CSV file. The entries are ranked by their occurences.
			Use case example: A ranking of most common first name and last name combinations for persons could be created. 
            See also: <a href="#cmd:pigeonhole">pigeonhole</a></td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The input file, requires SNT.</td>
    </tr>
    <tr>
        <td>Argument: type</td>
        <td>The type of resource to be analyze e.g. foaf:Person</td>
    </tr>
    <tr>
        <td>Argument: predicate1</td>
        <td>The property to examine. Requires long namespace version.</td>
    </tr>
	<tr>
        <td>Argument: further predicates</td>
        <td>Further predicates, requires long namespace version.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>One CSV file for every property and combination of properties, names are chosen automatically.</td>
    </tr>
</table> 





#### correct <a name="cmd:correct"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>correct</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>correct &lt;input file&gt; &lt;output file&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Line based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Removes invalid triples from a given file, respectively replaces invalid characters with the ?-character. </td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The input file, requires N-Triples.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>Name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An N-Triples file without the problematic triples.</td>
    </tr>
</table> 


#### extractduplicatelinks <a name="cmd:extractduplicatelinks"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>extractduplicatelinks</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>extractduplicatelinks &lt;input file&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Extracts statements that do not address their subject or target exclusively. Use case example: Find owl#sameAs-links in a link set that connect commodity-resources, 
		respectively identify such resources. Useful in combination with the <a href="#cmd:subtract">subtract</a> command.</td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The input file, requires SNT.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>Two N-Triples files: subjects.nt contains all statements that do not address their subject exclusively; 
            objects.nt contains all statements that do not address their objects exclusively.</td>
    </tr>
</table> 


#### extractreferenced <a name="cmd:extractreferenced"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>extract referenced</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>extractreferenced &lt;file A&gt; &lt;file B&gt; &lt;output file&gt; &lt;predicate1&gt; [&lt;predicate2&gt; ...]</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Extracts resources from file B that are referenced in file A. Missing resources in fileB are ignored.</td>
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
        <td>Argument: output file</td>
        <td>The name of the output file. This file will contain the extracted resources.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file containing the extracted resources.</td>
    </tr>
</table> 

See also [getenrichment](#cmd:getenrichment).


#### outline <a name="cmd:outline"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>outline</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>outline &lt;input file&gt; &lt;output file> &lt;target property></td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Creates literal representations for each resource in a file. 
            The representation is mapped to a given property. 
        </td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The input file with the resource to be outlined. SNT required.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>The name of the file to store the output in.</td>
    </tr>
    <tr>
        <td>Argument: target property</td>
        <td>The property to assign the outline to.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>Name of the file to store the output in.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An SNT file with one statement for each resource. &lt;original subject&gt; &lt;target property&gt; "literal representation"</td>
    </tr>
</table>

See also: [securelooseends](#cmd:securelooseends).


#### pigeonhole <a name="cmd:pigeonhole"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>pigeonhole</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>pigeonhole &lt;input file&gt; &lt;output file A&gt; &lt;output file B&gt; &lt;output file C&gt; &lt;CSV&gt; &lt;total threshold&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>Resource based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Extracts the resources from an SNT file according to the frequency of their attributes.
            A CSV file, such as produced by <a href="#cmd:analyzetype">analyzetype</a>, is used to determine the necessary information.
			The CSV file contains combinations of values (a single property is also considered a combination) 
			of the covered properties together with a number "total" that indicates the number occurences of the combination in the input file.
			The entries in this CSV file are sorted by this number. <br>
			The command reads the CSV entries up until the threshold of the "total"-field is undershot. Then it aborts. <br>	
			The command then reads the input file resource-wise and handles the resources: <br>
			Their property-combinations are looked up in the CSV table. <br>
            If a properties combination has an entry in the table then the resource is written to file A. <br>
            If a certain combination is not present in the table, then the resource is written to file B. <br>
			If the resource does not even contain all of the properties stated in the CSV file then it is written to file C.<br><br>
			Thus the command extracts the resources of the top X most frequent properties combinations.
         </td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The input file with the resources to be pigeonholed. SNT required.</td>
    </tr>
    <tr>
        <td>Argument: output file A</td>
        <td>The name of the file to store the output in.</td>
    </tr>
     <tr>
        <td>Argument: output file B</td>
        <td>The name of the file to store the output in.</td>
    </tr>
     <tr>
        <td>Argument: output file C</td>
        <td>The name of the file to store the output in.</td>
    </tr>
    <tr>
        <td>Argument: CSV</td>
        <td>A CSV file containing the frequencies of the properties values. Same as the output of the <a href="#cmd:analyzetype">analyzetype</a> command.</td>
    </tr>
    <tr>
        <td>Argument: total threshold</td>
        <td>A positive integer to be used as lower threshold on the total frequencies column in the CSV. Can be used to close out uncommon values.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>Three SNT files with the resources of their category.<br>
		File A: Contains all resources that containing a property combination that is in the top x (limited by the threshold parameter) most frequent combinations.<br>
		File B: Contains all resources that have values for the requested properties but do not reside in the top x combinations.<br>
		File C: Contains the remaining resources.
		</td>
    </tr>
</table>

Use together with [analyzetype](#cmd:analyzetype).


#### pumpup <a name="cmd:pumpup"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>pumpup</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>pumpup &lt;input file&gt; &lt;output file&gt;</td>
    </tr>
    <tr>
        <td>Type</td>
        <td>statement based</td>
    </tr>
    <tr>
        <td>Description</td>
        <td>Extends the namespaces in an N-Triples file to thier long forms. Uses the namespaces as stated below. 
		The file "namespaces.txt" specifying these namespaces comes along with the binaries and can be adapted to custom needs. 
		Often commands already include this functionality.</td>
    </tr>
    <tr>
        <td>Argument: input file</td>
        <td>The name of the input file, requires N-Triples.</td>
    </tr>
    <tr>
        <td>Argument: output file</td>
        <td>The name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>An N-Triples file containing the merged statements.</td>
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




#### subtract <a name="cmd:subtract"></a>

<table border="1" style="width:100%">
    <col width="20%">
    <col width="80%">
    <tr>
        <td>Name</td>
        <td>subtract</td>
    </tr>
    <tr>
        <td>Usage</td>
        <td>subtract &lt;file A&gt; &lt;file B&gt; &lt;output file&gt;</td>
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
        <td>Argument: output file</td>
        <td>Name of the output file.</td>
    </tr>
    <tr>
        <td>Output</td>
        <td>The resulting file containing SNT.</td>
    </tr>
</table> 




## Getting Started <a name="sec:gettingstarted"></a>

Some steps to get started:

1. Prepare your data in a single directory, have it in one of these formats: .nt, .rdf, .xml, .jsonld.
2. Convert your data to N-Triples if not already in use.  
`java -jar reshaperdf-1.0-SNAPSHOT.jar ntriplify ./myrdf ./nt/mydata.nt`
3. Sort your data.  
`java -jar reshaperdf-1.0-SNAPSHOT.jar sort ./nt/mydata ./nt/mydata_sorted.nt`
4. Extract all persons (foaf:Person) from the file into another file.  
`java -jar reshaperdf-1.0-SNAPSHOT.jar extract ./nt/mydata_sorted.nt ./nt/mypersons.nt http://xmlns.com/foaf/0.1/Person ? 0 -1`

