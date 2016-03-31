@echo off
REM Run all variable ordering and pre-processing switches on/create a Neo4j database for every Minion file in the given directory
REM Invoke with:
REM parse_all_minions.bat {directory_of_minion_files} {output_directory}

SET directory_of_minions=%1

for %%F in (%directory_of_minions:~0,-1%\*.minion") do (
	run_all_switches.bat "%%F" %2
)