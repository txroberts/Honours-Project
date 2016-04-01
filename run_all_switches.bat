@echo off
REM Run all variable ordering and pre-processing switches on a Minion file and create a Neo4j database for each combination
REM Invoke with:
REM run_all_switches.bat {minion_file} {output_directory}

SET minion_exe=".\minion.exe"
SET minion_file_path=%1
SET minion_file=%~nx1

SET raw_output_path=%2
SET output_path=%raw_output_path:~0,-1%\%minion_file%
SET parser_script=".\Parser\parser.py"

SET import_tool=".\neo4j-community-2.3.2\bin\Neo4jImport.bat"

REM ==========================================================================================================

SET var_orderings=sdf srf ldf static
SET pre_processings=GAC SAC SACBounds SSAC SSACBounds

REM ==========================================================================================================

REM No variable ordering or pre-processing

echo 1) Minion solving...
%minion_exe% %minion_file_path% -dumptree > %output_path%_None_None.txt"

echo 2) Python parsing...
python %parser_script% %output_path%_None_None.txt" %raw_output_path%

echo 3) Creating Neo4j database...
call %import_tool% --into %output_path%_None_None" --nodes %output_path%_None_None_nodes.csv" --relationships %output_path%_None_None_relationships.csv"

REM echo 4) Measuring tree...
REM MeasureNeo4j.java {directory_of_neo4j_databases} {data_output_csv_file)


REM All variable ordering, no pre-processing

FOR %%v IN (%var_orderings%) DO (
	echo -varorder %%v
	
	echo 1) Minion solving...
	%minion_exe% %minion_file_path% -varorder %%v -dumptree > %output_path%_%%v_None.txt"
	
	echo 2) Python parsing...
	python %parser_script% %output_path%_%%v_None.txt" %raw_output_path%
	
	echo 3) Creating Neo4j database...
	call %import_tool% --into %output_path%_%%v_None" --nodes %output_path%_%%v_None_nodes.csv" --relationships %output_path%_%%v_None_relationships.csv"
	
	REM echo 4) Measuring tree...
	REM MeasureNeo4j.java {directory_of_neo4j_databases} {data_output_csv_file)
)

REM No variable ordering, all pre-processing

FOR %%p IN (%pre_processings%) DO (
	echo -preprocess %%p
	
	echo 1) Minion solving...
	%minion_exe% %minion_file_path% -preprocess %%p -dumptree > %output_path%_None_%%p.txt"
	
	echo 2) Python parsing...
	python %parser_script% %output_path%_None_%%p.txt" %raw_output_path%
	
	echo 3) Creating Neo4j database...
	call %import_tool% --into %output_path%_None_%%p" --nodes %output_path%_None_%%p_nodes.csv" --relationships %output_path%_None_%%p_relationships.csv"
	
	REM echo 4) Measuring tree...
	REM MeasureNeo4j.java {directory_of_neo4j_databases} {data_output_csv_file)
)

REM Every combinations of variable ordering and pre-processing

FOR %%v IN (%var_orderings%) DO (
	FOR %%p IN (%pre_processings%) DO (
		echo -varorder %%v -preprocess %%p
		
		echo 1) Minion solving...
		%minion_exe% %minion_file_path% -varorder %%v -preprocess %%p -dumptree > %output_path%_%%v_%%p.txt"
		
		echo 2) Python parsing...
		python %parser_script% %output_path%_%%v_%%p.txt" %raw_output_path%
		
		echo 3) Creating Neo4j database...
		call %import_tool% --into %output_path%_%%v_%%p" --nodes %output_path%_%%v_%%p_nodes.csv" --relationships %output_path%_%%v_%%p_relationships.csv"
		
		REM echo 4) Measuring tree...
		REM MeasureNeo4j.java {directory_of_neo4j_databases} {data_output_csv_file)
	)
)