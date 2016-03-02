README

The database files are found in ~/EIT060-Project2/src/Database
The auditlog files are found in ~/EIT060-Project2/src/AuditLogs

Example database-data exists in ~/EIT060-Project2/src/Database/DatabaseBackup.txt

**********************************************************************
Before first startup
----------------------------------------------------------------------
The Database will look for a file to load. If none was found, it will
create a new empty one. If you want to use the example-data from 
DatabaseBackup.txt, make a copy of it and rename it Database99999999999999 
or with todays date on the format yyyyMMddHHmmss.

Example name: 	If it's the 3rd of March 2016 at 15:05:20, the name would be
				Database20160303150520

**********************************************************************
To run server from console:
----------------------------------------------------------------------
Start cmd.exe
Change directory into ~/EIT060-Project2/src
Command: Server.server <port number>

Example: Server.server 9876

**********************************************************************
To run client from console
----------------------------------------------------------------------
Make sure server is started before trying to connect with client!

Start cmd.exe
Change directory into ~/EIT060-Project2/src
Command: Client.Client <port number>

Example: Client.Client 9876

Make sure the <port number> is the same as the server started!

**********************************************************************
Login in credentials
----------------------------------------------------------------------
Government users have code 		0---
Doctor users have code			1---
Nurse users have code 			2---
Patiens have their social security number

|	UserID				|		Password		|
|---------------------------------------|---------------------------------------|
|	0001				|		password		|
|	1001				|		password		|
|	1002				|		password		|
|	2001				|		password		|
|	2002				|		password		|
|	2003				|		password		|
|	2004				|		password		|
|	2005				|		password		|
|	2006				|		password		|
|	192810111314			|		password		|
|	195008042722			|		password		|
|	196506111871			|		password		|
|	198408171144			|		password		|
|	198812143325			|		password		|
|---------------------------------------|---------------------------------------|


**********************************************************************
To create a certificate
----------------------------------------------------------------------
For Linux: Run script ~/EIT060-Project2/certificates/createUser.sh

For Windows: 
Start cmd.exe
Change directory to ~/EIT060-Project2/certificates/
Type the commands in the textfile ~/EIT060-Project2/certificates/commands.txt into the command prompt, line by line
	Change the <USERID> in commands.txt to the userID for the new login, for example 1002 or 199401024411
	Change the paths in commands.txt to the CA-certificates if needed

When entering the certificate attributes:
CN: 	CommonName			- Firstname Surname
OU: 	OrganizationalUnit		- UserID
O: 	Organization			- Division
L: 	Locality			- <Leave blank>
S: 	StateOrProvinceName		- <Leave blank>
C:	CountryName			- <Leave blank>
