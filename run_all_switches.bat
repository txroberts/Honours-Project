@echo off
REM Run all variable ordering and pre-processing switches on a Minion file
REM Invoke with:
REM {path_to_minion_file} {directory_to_export_dump_tree_to}

SET minion_file_path=%1
SET minion_file=%~nx1
SET output_path=%2
SET dump_tree=%output_path:~0,-1%\%minion_file%

REM No variable ordering or pre-processing

echo Minion...
minion.exe %minion_file_path% -dumptree > %dump_tree%_None_None.txt"
echo Python...
python "C:\Users\Tom\Documents\UoD\4th Year\Honours-Project\Parser\parser.py" %dump_tree%_None_None.txt" %output_path%

SET var_orderings=sdf srf ldf static

FOR %%a IN (%var_orderings%) DO (
	echo %%a
	echo Running Minion...
	minion.exe %minion_file_path% -varorder %%a -dumptree > %dump_tree%_%%a_None.txt"
	echo Python parsing...
	python "C:\Users\Tom\Documents\UoD\4th Year\Honours-Project\Parser\parser.py" %dump_tree%_%%a_None.txt" %output_path%
)