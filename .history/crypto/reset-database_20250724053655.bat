@echo off
echo Resetting database...

echo Cleaning Flyway...
call mvnw.cmd flyway:clean

echo Migrating database...
call mvnw.cmd flyway:migrate

echo Database reset complete!
pause 