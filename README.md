# ELS Project

For this project, you need to [install Gradle](https://gradle.org/install/)

## Project setup

Copy your source files to the ``src`` folder, and your JUnit test files to the ``test`` folder.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``build/install/els2022-g2/bin``. For convenience, there are two script files, one for Windows (``els2022-g2.bat``) and another for Linux (``els2022-g2``), in the root of the repository, that call these scripts.

After compilation, tests will be automatically executed, if any test fails, the build stops. If you want to ignore the tests and build the program even if some tests fail, execute Gradle with flags "-x test".

When creating a Java executable, it is necessary to specify which class that contains a ``main()`` method should be entry point of the application. This can be configured in the Gradle script with the property ``mainClassName``, which by default has the value ``pt.up.fe.els2022.Main``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``build/reports/tests/test/index.html``.

## Assignment 1


### Reading from an XML file
```
Read files/vitis-report_1.xml as f1 
    Parent Resources
    Col LUT => LUTs
    Col FF => FFs
    Col DSP48E => DSP48Es
    Col BRAM_18K => BRAM_18Ks
End
```
On the first line we specify what file we want to read from and, after keyword ``as``, give the resulting table an **identifier which is mandatory to use when operating on tables**.

If multiple files are read in the same way, our DSL allows for an easy configuration like: 

- ``Read files/vitis-report_1.xml,files/vitis-report_2.xml,files/vitis-report_3.xml as f1,f2,f3``

Inside the Read block we must first specify the parent from where we will read our values, in case there are multiple tags with the same name.
Then we indicate what tag we will extract the value and right after ``=>`` we can change the name of the resulting column on the table.
We decided that renaming columns right at table creation was more intuitive and user-friendly this way, opposed to having to rename them one by one after reading.

### Adding and removing columns
```
AddColumn f1 TESTCOL TESTE
AddColumn f1 TESTCOL TESTE as f4
RemoveColumn f1 LUTs
RemoveColumn f1 LUTs as f4
```

When adding, we first indicate the target table, then the name of the new column and the default value of that column for all entries on that table. 

When removing, we simply indicate the target table and then what column to remove. 

Similar to some other commands, these two **may** have ``as <identifier>`` at the end. When this is present the original table will remain unaltered and the results of the operations will be reflected on a new table will be identified as ``<identifier>``.

### Sorting a table
```
Sort f3 LUTs desc
Sort f3 LUTs desc as f4
```
Here we first indicate the target table, the column from which we take the values to sort the entries and the order (``asc``or ``desc``).
Again, we **may** have ``as <identifier>`` at the end so that the changes are reflected on a new table and leave the original intact.

**Important** - for this assignment, values read from XML's are all strings so sorting is done according to the unicode value of each character. For example, 432 < 54 is true because char '4' has smaller unicode value than '5'. 

### Merging
```
Merge f1,f2
Merge f1,f2,f3 as f4
Merge f1,f2,f3 with Name on File
Merge f1,f2,f3 with Name on File as f4
```
The merge command can be used in several ways. You can merge more than two tables at a time. If ``as <indentifier>`` is specified the combination of all tables will result in a new table, otherwise it will overwrite the first table in the command (not mandatory).

There is also a ``with <atribute> on <column_name>`` part where you can create a new column for all tables in this merge command preserving some attribute of the table on a column.
For example, in this assignment we want to know the file from where a table/entry originated. The filepath is an attribute of the table originated from the Read command and when merging 2 tables together, instead of adding the column manually to each table, we can use this Merge command to more easily add that column to all tables involved.
We chose this approach because in the future more metadata may be needed from the original file and this gives the language a lot of versatility.

### Writing to a file
```
SetOutput f4
    File
    LUTs
    FFs
    DSP48Es
    BRAM_18Ks
End

Write CSV f4 to test/pt/up/fe/els2022/outFiles/outTestFull.csv
```
To create a .csv from a table we use the Write command where we first specific the type of file (only 'CSV' supported for now), then the target table and finally, after ``to``, the output path.

Before using Write, we can use SetOutput to filter and order the columns for the output file. If SetOutput is done, only columns listed in it will be printed following the order they are presented in the command. Otherwise, everything will be printed, following to order in the Read command.


## Overview of the system

![Imgur](https://imgur.com/eXMSaSi.png)

## Design decisions

Most decisions were made due to the limitations we found on the language at the time.  
We wanted the DSL to feel powerful but yet easy to use.  

Our syntax was chosen to allow for a easier parse of the language. The Beginning of each line starts with a Keyword, that allows us to easily identify which command we are parsing.  
Multiline Commands (SetOutput and Read) follow the same principle but we further include the End token so we can easily decide if the commands bellong inside or not those multile Commands.  

Our Implementation works around the Idea of commands. The language parser decides which command to create, depending on the keyword, and delegates the parsing problem to the command itself.  
This modularity allows for an easy way to create and add more commands as we go.  
The app stores a list of commands, by the order they appear on the Config file, and executes them by order.  
This works similar to a Visitor in the way that each command knows which function of the table to call and execute, depending on the said command. 
The Commands also have access to a global symbolTable, where the tables are stored and it is able to assign reassign tables to identifiers(like variables).

One particular part of the language is the ability to store the result of each command on a new variable or on the same one, allowing for more flexibility.

## Assignment 2

In this assignment we mostly refactored our code and introduced features which did not require many new commands. As such, the new commands are almost all for the reading of TXT files, and one for joining tables without creating new lines

### JOIN
```
Join xml,json as xml
```
We stay consistent in or syntax. This command can join two tables, creating another table. **Warning**: Both tables need to have the same number of lines

To do so, the user just needs to list the two tables he whishes to join

The user can use the keywork as to overwrite one of the tables or just create a new one (just like in commands created in the last assignment).

### READ (JSON)
This read is not an addition. It is instead yet another use case. The user can use the same READ command for XML and JSON. TO use for JSON we do:
```
Read files/decision_tree.json as json
    Parent ROOT params
End
```
In this command we list the "parents" on the JSON file from which we want to read the primitives. In this example, we want to read all primitives that are under the attribute params (which is a composite attribute) and under the root of the file (since root doesn't have a name we choose the keyword "ROOT").

We also added the possibility to add the Folder info into a table with "Include folder".

As we can see, it is almost the same as the XML use case. 

### EXTRACT 
```
Extract gprof Lines 1
Extract gprof Columns "% time","name","folder" 
Extract gprof Columns "% time","name","folder" Lines 1
Extract gprof Columns "% time","name","folder" Lines 1,2 as i2
```
With this command we extract lines and/or column from a table and save it in a new table or in the original table.

This is command also represents another way to remove columns from a table including the ones specified in the Column part


### Aditional command on Read 
```
    Table Line 6 Header height 2
    Word Line 1 Word 2 as lw
    Word Line 1 Column 2 as lc
    Word Starts With time Word 2 as sww
    Word Starts With time Column 4 as swc
```
We added more command to the read block.

We can extract a table specifing the line of start and the header height

We can also extract words with the word command.

We have to know the line and word:
    The line can be extracted with "Starts with" or Line
    The word can be specified by the word number (e.g. 3rd on the line) or the column number(e.g. word that has a char in the column 20)

Each Table and Word command creates a new table, these are then joined into the READ output table.

The Table command idrectly maps to an ordinary representation of a table.
The Word command creates a table with one column with the key being the specified with the "as X" sintax an the value, the word extracted with the command. 

## Overview of the system
![Imgur](https://i.imgur.com/zmcq3R2.png)

## Api Example

```
 new    BuilderExecutor()
                                .read()
                                    .setFilesPaths(Collections.singletonList("files/ass2/equivalent.json"))
                                    .setFilesIds(Collections.singletonList("f1"))
                                    .setParentElements(Collections.singletonList("total/results/static"))
                                .close()
                                .write()
                                    .setType("CSV")
                                    .setFileId("f1")
                                    .setFilePath("test/pt/up/fe/els2022/outFiles/outEquivalentAPI.csv")
                                .close()
                            .build()
                .run();
```

The example above shows how one can read a file, set its identifier and the xml/json structure parent, and then write the result into a new file.  

This new api approach lead to an easier and more straightforward connection between the parser and the underlying implementation.

## Design decisions
In our project specifically, because we started out with a preliminary DSL, a lot of the effourt spent in this assignment was refactoring our project to implement the builder pattern and fluent API which both introduced a whole new level of complexy to our code.
We decided to use an hierarchy of Builders. The main builder would return a builder specific for each command that would return the original builder on close.  
This allowed us to retrict the parameters that we could set for each command, making the API more consistent and easy to use.  
This was mainly because different commands had the need for different parameters and, e.g. it would make no sense to allow to add a path into a command like AddColumn.  
The only drawback of such approach was that we now need to call the fucntion close() every time we want to introduce a new command, as those are only accessible in the main builder.

We decided to use the same READ command for XML and JSON because they are so similar in how they work. In the same line of thought, we chose to create a new syntax for the commands inside the READ block specific for TXT files because they lack any structure and so their interpreting is interely different and requires new functions we didn't have before.

Besides that we also added the possibility to read folders of files. The behaviour will be the same for all files. If a type of file is specificied, the program will only read and create tables from that filetype. The identifier used on the READ will map to all tables originated from the command so when an operation is used it will be used on all of them besides JOIN and MERGE which will result in only 1 (aggregate functions).  

This means that now identifiers map to a list of tables instead of just one. When only one file is read at a time, it will be a list conatining only one table but it was the tradeoff of implementing the folder reading feature.  
We faced a decision problem when implementing the way lists of lists were implemented. Some commands like Sort and RemoveColumn operate over a list of tables and the result is a list with the same length with the operation made in each table.  
However the Merge and Join commands are different in purpose, they dont operate over a list of tables only changing each table separately, they operate over a range of tables unifying them.  
This lead to a conundrum, we could join/merge tables according to their index on their respective list (output list would be a list that would contain the merge/join of the 1 element of each list, the second, third, and so on) or we could concatenate every table in a single list and then merge/join them into one table.  
We took this last approach mainly due to a problem that the first solution had, how could we handle lists that had different sizes? It would lead to a command with a strange output, and so the more straightforward approach was chosen.

Now, the tables also have a default output order. Since, internally, Tables are Hashmaps, output order was sort of random before. We changed that so now the order, if not specified will be same as the order they are read from the file.

We decided to add the possibility to extract tables directly from the file in a more automated way, rather than using the manual word command. This means the extraction of tables from an unstructured file is simplified.

## Assignment 3

In this assignment, an xtext grammar and parser was implemented, both improving on the existing functionalities and extending the language with 3 new commands.

### XTEXT Grammar

#### Read

```
Read "files/vitis-report_1.xml","files/vitis-report_2.xml","files/vitis-report_3.xml" XML as f1,f2,f3
    Include folder
    Parent "Resources"
    Col "LUT" => "LUTs"
    Col "FF" => "FFs"
    Col "DSP48E" => "DSPs"
    Col "BRAM_18K" => "BRAMs"
End

Read "files/decision_tree.json" JSON
    Include file
    Parent ROOT "params"
End

Read "files/gprof.txt" TEXT as f4
    Table Line 6 Header height 2
    Word Starts With " Time in seconds" Word 5 as "time_in_seconds"
End
```

As shown above, the `Read` command requires one or more file paths and the file type (XML, JSON or TEXT). Optionally, the user can give the same amount of file identifiers as file paths, and include the folder and/or the file name as a column in the table row. For XML and JSON files, the user can also specify the parent elements to which the values should be extracted from, and which columns to extract (with new names, if the user wants). For TEXT files, the user can specify a table or a word to read.

#### Add Column

```
AddColumn f1 "Col" "Default" as f2
```

With this command, the user can add a column to a table, filling each entry with a default value. The new table can override the old one, or be saved in another one.

#### Remove Column

```
RemoveColumn f1 "LUTs" as f4
```

With this command, the user can remove a column from a table. The new table can override the old one, or be saved in another one.

#### Sort

```
Sort f2 "FF" desc
```

With this command, the user can sort a table on a specific column. The order is, by default, ascending, but can be specified using the keywords ```asc``` and ```desc```. The new table can override the old one, or be saved in another one.

#### Merge

```
Merge f1,f2,f3 with "Name" on "File" as f4
Merge f1,f4
```

With this command, the user can merge tables. Some parameter can be saved on a new column. The new table can override the old one, or be saved in another one.

#### Join

```
Join f1,f2,f3 as out
```

With this command, the user can join tables. The new table can override the old one, or be saved in another one.

#### Extract

```
Extract f1 Columns "% time","name","folder" Lines 1
```

With this command, the user can extract a set of columns and/or lines from an existing table. The new table can override the old one, or be saved in another one.

#### Average

```
Average f3 as f4
```

With this command, the user can calculate the average on every numerical column of a table. These values are added to a new line. The new table can override the old one, or be saved in another one.

#### Sum

```
Sum f3 as f4
```

With this command, the user can calculate the sum on every numerical column of a table. These values are added to a new line. The new table can override the old one, or be saved in another one.

#### Set Output

```
SetOutput f2
    "File"
    "FF"
    "BRAMs"
    "Col"
End
```

With this command, the user can set which of the columns of a table are to be saved and in what order.

#### Write

```
Write CSV f4 to "test/pt/up/fe/els2022/outFiles/outTestAssignment2.csv"
```

With this command, the user can write a table into a CSV file.

#### Read Directory

```
ReadDir "Desktop/files" as f1
    Read "test-results.xml" XML as d1
    Read "test-task2.json" JSON as d2
    Read "testint-task3.txt" TEXT as d3
    Join d1,d2,d3 as d4
    Pile d4
End
```

This command allows for the execution of a set of all the other commands, to all the files in a directory. If a table identifier is specified, a final table must be specified as a "Pile" table, i.e. an accumulated table.

### System Overview

![Imgur](https://i.imgur.com/g0Fw65E.png)

### Design Decisions

Regarding the added commands, some decisions were made, mainly because of usability.

The first command added was the ReadDir command. We though that in order to facilitate the usability of the system we could add a block where we could add any code. Therefore we created the readdir command. This adds flexibility as we can execute multiple kind of code inside this block. As a syntax sugar, in order to improve the experience we added the Pile command. This command merges the specified table between iterations. The result can be accessed as the result table of the readir command. This was not needed as we could add a merge command between each iteration but we decided this would improve usability.
The iteration nature of the solution also resembles the real world problem as the folders are inside the folder and we run the code for each of the subfolders. We also decided that the paths specified inside such block should be relative to the subfolder currently being read. If a read command species a file "test-result.xml" this is relative to the folder being read by the ReadDir command at the time. 
This way there is no need to specify the subfolders but only the files inside the subfolder and the parent folder.

Other commands were introduced (sum and average). Two options were considered, the commands would create a blank table with only the resulting line or they would merge such line to the end of the table specified. We went with the last as it would decrease the needed amount of merges (the first solution would require an extra merge with the needed table). A problem found was that, by making a sum after the average, the sum would take the average into consideration when making the calculations. To solve that, we treat the lines with sum and average differently from the others, as they are ignored by the commands.

Regarding the external DSL, we decided to keep the same syntax as we found it easy to use. Only small adjustments were made in order to solve some problems found when using xtext. mainly paths are now always specified between quotation marks, whilst before the quotation marks were only needed when the paths included blank spaces.
We also decided to keep the internal DSL working as it was before using a fluent api. A plus of our implementation of the DSL is that the external dsl maps directly to the internal dsl and they map almost directly to the underlying architecture of the program, as we work with commands to operate over table, just like the external DSL. This made the change from the previous regex powered parser to the new xtext parser simpler.













