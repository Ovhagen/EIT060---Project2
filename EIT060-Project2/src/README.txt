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
Command Guide
----------------------------------------------------------------------
Write -h in the console for help with syntax for available commands
as the current user.

System Commands:
----------------------------------------------------------------------
Get: -g SocialSecurityNumber

Retrieves the record for the person with entered Social Security Number.
----------------------------------------------------------------------
Edit: -e SocialSecurityNumber

Gives different option for editing a existing record associated to the entered 
Social Security Number.
----------------------------------------------------------------------
Print All: -pa

Prints all available records for the current users.
----------------------------------------------------------------------
Put: -p Firstname Surname NurseIDs SocialSecurityNumber (Doctors only)

Creates a new record for the entered Social Security Number. Every
argument in this command is seperated by a space.

An example would be: -p Sven Eriksson 2002 2005 2004 199502931239
----------------------------------------------------------------------
Print ACL: -pacl SocialSecurityNumber

Prints the ACL for the record tied to the entered Social Security Number.
----------------------------------------------------------------------
Add Nurses: -an SocialSecurityNumber NurseID1 NurseID2 NurseID3.... (Doctors only)

Adds the nurses to an existing record for the entered Social Security Number. Every
argument in this command is separated by a space.

An example would be: -an 199502931239 2002 2005 2004
----------------------------------------------------------------------
Remove Nurse: -rn SocialSecurityNumber NurseID1 NurseID2 NurseID3... (Doctors only)

Removes the nurses to an existing record for the entered Social Security Number. Every
argument in this command is separated by a space.

An example would be: -rn 199502931239 2002 2005 2004
----------------------------------------------------------------------
Edit DoctorID: -ed SocialSecurityNumber DoctorID (Government only)

Edits the current DoctorID associated with the existing record for
the entered Social Security Number.
----------------------------------------------------------------------
-rm SocialSecurityNumber (Government only)

Removes the existing record for the entered Social Security Number.
----------------------------------------------------------------------

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
