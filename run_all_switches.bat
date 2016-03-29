@echo off
REM Run all variable ordering and pre-processing switches on a Minion file
REM Invoke with:
REM run_all_switches.bat {path_to_minion_file} {directory_to_export_dump_tree_to} {neo4j_databases_directory}

SET minion_exe=".\minion.exe"
SET minion_file_path=%1
SET minion_file=%~nx1

SET output_path=%2
SET dump_tree=%output_path:~0,-1%\%minion_file%
SET parser_script=".\Parser\parser.py"

SET import_tool=".\neo4j-community-2.3.2\bin\Neo4jImport.bat"
SET neo4j_output_path=%3
SET neo4j_db=%neo4j_output_path:~0,-1%\%minion_file%

REM ==========================================================================================================

REM No variable ordering or pre-processing

echo Minion solving...
%minion_exe% %minion_file_path% -dumptree > %dump_tree%_None_None.txt"

echo Python parsing...
python %parser_script% %dump_tree%_None_None.txt" %output_path%

echo Creating Neo4j database...
call %import_tool% --into %neo4j_db%_None_None" --nodes %dump_tree%_None_None_nodes.csv" --relationships %dump_tree%_None_None_relationships.csv"

REM All variable orderings, no pre-processing

SET var_orderings=sdf srf ldf static

FOR %%v IN (%var_orderings%) DO (
	echo -varorder %%v
	
	echo Minion solving...
	%minion_exe% %minion_file_path% -varorder %%v -dumptree > %dump_tree%_%%v_None.txt"
	
	echo Python parsing...
	python %parser_script% %dump_tree%_%%v_None.txt" %output_path%
	
	echo Creating Neo4j database...
	call %import_tool% --into %neo4j_db%_%%v_None" --nodes %dump_tree%_%%v_None_nodes.csv" --relationships %dump_tree%_%%v_None_relationships.csv"
)

REM No variable orderings, all pre-processing

SET pre_processings=GAC SAC SACBounds SSAC SSACBounds

FOR %%p IN (%pre_processings%) DO (
	echo -preprocess %%p
	
	echo Minion solving...
	%minion_exe% %minion_file_path% -preprocess %%p -dumptree > %dump_tree%_None_%%p.txt"
	
	echo Python parsing...
	python %parser_script% %dump_tree%_None_%%p.txt" %output_path%
	
	echo Creating Neo4j database...
	call %import_tool% --into %neo4j_db%_None_%%p" --nodes %dump_tree%_None_%%p_nodes.csv" --relationships %dump_tree%_None_%%p_relationships.csv"
)

REM All combinations of variable orderings and pre-processing

FOR %%v IN (%var_orderings%) DO (
	FOR %%p IN (%pre_processings%) DO (
		echo -varorder %%v -preprocess %%p
		
		echo Minion solving...
		%minion_exe% %minion_file_path% -varorder %%v -preprocess %%p -dumptree > %dump_tree%_%%v_%%p.txt"
		
		echo Python parsing...
		python %parser_script% %dump_tree%_%%v_%%p.txt" %output_path%
		
		echo Creating Neo4j database...
		call %import_tool% --into %neo4j_db%_%%v_%%p" --nodes %dump_tree%_%%v_%%p_nodes.csv" --relationships %dump_tree%_%%v_%%p_relationships.csv"
	)
)