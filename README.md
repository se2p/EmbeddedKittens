![License GPL v3](https://img.shields.io/github/license/se2p/LitterBox?color=blue&style=flat-square)

LitterBox is developed at the
[Chair of Software Engineering II](https://www.fim.uni-passau.de/lehrstuhl-fuer-software-engineering-ii/)
and the [Didactics of Informatics](https://ddi.fim.uni-passau.de/) of the [University of Passau](https://www.uni-passau.de).


## Building LitterBox-ML

LitterBox-ML is built using [Maven](https://maven.apache.org/). To
produce an executable jar-file, run the following command:

```
mvn package
```

This will produce `target/Litterbox-ML-1.0.jar`


## Using LitterBox

To see an overview of the command line options available in LitterBox type:

```
java -jar Litterbox-ML-1.0.jar --help
```


### Code2Vec output

To be able to use the code2vec model with the programming language Scratch, a scratch parser is needed to generate the required input representation. According to the description on https://github.com/tech-srl/code2vec#extending-to-other-languages,
Litterbox produces for each Scratch program a file with these rules. It's like a Scratch extractor. Litterbox needs a path to a single file or a folder with multiple projects and produces the output to the declared output folder. 

```
java -jar Litterbox-ML-1.0.jar code2vec --output <path/to/folder/for/the/output> --path <path/to/json/project/or/folder/with/projects>
```

There are some differences between Scratch and "normal" programming languages like Java, but the most important is,
that Scratch has no methods and therefore no method names. Because of that, Litterbox uses sprite names like method names 
and creates path contexts from every single sprite in a project.

#### Code2vec output per script

LitterBox can generate the context paths per scripts and procedures. Given a Scratch program as input, it produces for each script and procedure a file containing the needed input representation for the code2vec model. 

```
java -jar Litterbox-1.8.jar code2vec --output <path/to/folder/for/the/output> --path <path/to/json/project/or/folder/with/projects> --scripts
``` 


## Contributors

LitterBox is developed at the
[Chair of Software Engineering II](https://www.fim.uni-passau.de/lehrstuhl-fuer-software-engineering-ii/)
and the [Didactics of Informatics](https://ddi.fim.uni-passau.de/) of
the [University of Passau](https://www.uni-passau.de).

Contributors:

Florian Beck\
Benedikt Fein\
Michael Grüner\
Alaa Khalil\
Sebastian Schweikl
