@echo off
REM Run all variable ordering and pre-processing switches on a Minion file
REM Invoke with:
REM {path_to_minion_file} {directory_to_export_dump_tree_to}

SET minion_exe=.\minion.exe
SET minion_file_path=%1
SET minion_file=%~nx1
SET output_path=%2
SET dump_tree=%output_path:~0,-1%\%minion_file%
SET parser_script=.\Parser\parser.py

REM No variable ordering or pre-processing

echo Minion...
%minion_exe% %minion_file_path% -dumptree > %dump_tree%_None_None.txt"
echo Python...
python %parser_script% %dump_tree%_None_None.txt" %output_path%

SET var_orderings=sdf srf ldf static

FOR %%a IN (%var_orderings%) DO (
	echo %%a
	echo Running Minion...
	%minion_exe% %minion_file_path% -varorder %%a -dumptree > %dump_tree%_%%a_None.txt"
	echo Python parsing...
	python %parser_script% %dump_tree%_%%a_None.txt" %output_path%
)