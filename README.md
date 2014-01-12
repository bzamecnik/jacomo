# JaCoMo - Jabber Contact Monitor

## About

The task of JaCoMo is to monitor presence of contacts on one's Jabber account,
log it and visualize it. Presence means being online, offline, away etc.

Author: [Bohumir Zamecnik](http://zamecnik.me)
License: GNU GPL v2
Created: 2008-2009

# Libraries (dependecies)

Required Java version is Java SE 6.

- [JavaDB](http://developers.sun.com/javadb/) - database
- [Smack](http://www.igniterealtime.org/projects/smack/) - Jabber comunication
- [JFreeChart](http://www.jfree.org/jfreechart/) - interactive charts

# Building

- unpack the source package
- to build: `ant`
- to run JaCoMo: `java -jar dist/jacomo.jar`

# Installing and running

- unpack the binary package
- to run JaCoMo: `java -jar jacomo.jar`

# Usage

## Concepts

At first you have to have a Jabber account with some contacts. JaCoMo acts as
a Jabber bot. It listens on the account and waits for presence changes. So it
knows when someone goes online, away or offline and logs it into a database.
JaCoMo also interprets these records and shows them in a grafical way.

To select which contacts to monitor there is a contact filter which matches
contacts to a set of patterns. Any contact containing at least one pattern is
excluded from further processing.

Contacts in roster (Jabber account contact list) can be added, updated or
deleted outside JaCoMo and JaCoMo will reflect the changes. If a contact is
deleted it is marked as archived and not actually deleted to preserve its
logged presence records.

A lot of useful information is shown on standart output (console).

### Persistent storage

All peristent data is stored in `$HOME/.jacomo` directory, where `$HOME` means
the user's home directory (on Unix it is usually `/home/username`, on Windows
`C:\Documents and Settings\username`). To store logged data JaCoMo uses JavaDB
embedded database. Each configured Jabber account have its own directory with
database stored in a directory of form `$HOME/.jacomo/username_server`. The
Jabber credentials as well as contact filter setting are stored in
`$HOME/.jacomo/config.properties` file.

SECURITY NOTE: the password is stored in plain-text form so it is by no means
encrypted!

## Grafical User Interface

### Main window
		
The main JaCoMo window consists of a toolbar with buttons and a panel with
charts. In between you can see the current Jabber server and username.
		
#### Buttons

- **Configure** - Opens the Configuration dialog.
- **Database** - Opens the database connection and executing all the data
interpreters resulting in drawing the charts. If the database connection is
established successfully you will be enabled to connect to Jabber. The button
is a toggle button so clicking it again will disconnect the database.
- **Jabber** - Connects to the Jabber server specified in the configuration. If
connected successfully it will be possible to start logging. Clicking again
will disconnect from Jabber.
- **Logger** - Start/stop logging.
- **Refresh charts** - Reloads and reinterprets the data and redraws the charts.  
			
#### Charts

When connected to database the results of data interpreters are show in
charts. There are two type of charts: presence intervals and histograms. On
presence intervals you can see when particular contacts where online or
offline. On histograms you can see relative number of contacts present of
particular time. The interval chart is interactive so you can move along both
axes or select an area to zoom inside. On right-click there is a popup menu
with many more functions than just controling zoom. 

### Configuration dialog

In Configuration dialog you can set the Jabber credentials as server, user 
name and password. It is possible to configure contacts filter matching 
patterns here. One line equals one pattern. Any contact in roster containing 
any pattern from here will be excluded. So it is possible to specify the 
whole contact JID or just a part (eg. a domain). All the setting will be 
saved to a configuration upon closing the application.

## Developer docs

Developer documentation of JaCoMo is available in form of javadoc. It is
generated using Ant when building the project (see Build section). Javadoc
can be generated alone (without building the program) using `ant javadoc`
command. To open the javadoc see `dist/javadoc/index.html` file.
