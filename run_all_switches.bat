@echo off
REM Run all variable ordering and pre-processing switches on a Minion file and create a Neo4j database for each combination
REM Invoke with:
REM run_all_switches.bat {minion_file} {output_directory}

SET minion_exe=".\minion.exe"
SET minion_file_path=%1
SET minion_file=%~nx1

SET raw_output_path=%2
SET search_trees_output_path=%raw_output_path:~0,-1%\SearchTrees
SET db_output_path=%raw_output_path:~0,-1%\Databases

if not exist %search_trees_output_path%" mkdir %search_trees_output_path%"
if not exist %db_output_path%" mkdir %db_output_path%"

SET search_tree=%search_trees_output_path%\%minion_file%
SET db=%db_output_path%\%minion_file%

SET parser_script=".\Parser\parser.py"

SET import_tool=".\neo4j-community-2.3.2\bin\Neo4jImport.bat"

REM ==========================================================================================================

SET var_orderings=sdf srf ldf static
SET pre_processings=GAC SAC SACBounds SSAC SSACBounds

REM ==========================================================================================================

REM No variable ordering or pre-processing

echo 1 - Minion solving...
%minion_exe% %minion_file_path% -dumptree > %search_tree%_None_None.txt"

echo 2 - Python parsing...
python %parser_script% %search_tree%_None_None.txt" %search_trees_output_path%

echo 3 - Creating Neo4j database...
call %import_tool% --into %db%_None_None" --nodes %search_tree%_None_None_nodes.csv" --relationships %search_tree%_None_None_relationships.csv"

REM echo 4 - Measuring tree...
REM MeasureNeo4j.java {directory_of_neo4j_databases} {data_output_csv_file)

REM All variable ordering, no pre-processing

FOR %%v IN (%var_orderings%) DO (
	echo -varorder %%v
	
	echo 1 - Minion solving...
	%minion_exe% %minion_file_path% -varorder %%v -dumptree > %search_tree%_%%v_None.txt"
	
	echo 2 - Python parsing...
	python %parser_script% %search_tree%_%%v_None.txt" %search_trees_output_path%
	
	echo 3 - Creating Neo4j database...
	call %import_tool% --into %db%_%%v_None" --nodes %search_tree%_%%v_None_nodes.csv" --relationships %search_tree%_%%v_None_relationships.csv"
	
	REM echo 4 - Measuring tree...
	REM MeasureNeo4j.java {directory_of_neo4j_databases} {data_output_csv_file)
)

REM No variable ordering, all pre-processing

FOR %%p IN (%pre_processings%) DO (
	echo -preprocess %%p
	
	echo 1 - Minion solving...
	%minion_exe% %minion_file_path% -preprocess %%p -dumptree > %search_tree%_None_%%p.txt"
	
	echo 2 - Python parsing...
	python %parser_script% %search_tree%_None_%%p.txt" %search_trees_output_path%
	
	echo 3 - Creating Neo4j database...
	call %import_tool% --into %db%_None_%%p" --nodes %search_tree%_None_%%p_nodes.csv" --relationships %search_tree%_None_%%p_relationships.csv"
	
	REM echo 4 - Measuring tree...
	REM MeasureNeo4j.java {directory_of_neo4j_databases} {data_output_csv_file)
)

REM Every combinations of variable ordering and pre-processing

FOR %%v IN (%var_orderings%) DO (
	FOR %%p IN (%pre_processings%) DO (
		echo -varorder %%v -preprocess %%p
		
		echo 1 - Minion solving...
		%minion_exe% %minion_file_path% -varorder %%v -preprocess %%p -dumptree > %search_tree%_%%v_%%p.txt"
		
		echo 2 - Python parsing...
		python %parser_script% %search_tree%_%%v_%%p.txt" %search_trees_output_path%
		
		echo 3 - Creating Neo4j database...
		call %import_tool% --into %db%_%%v_%%p" --nodes %search_tree%_%%v_%%p_nodes.csv" --relationships %search_tree%_%%v_%%p_relationships.csv"
		
		REM echo 4 - Measuring tree...
		REM MeasureNeo4j.java {directory_of_neo4j_databases} {data_output_csv_file)
	)
)