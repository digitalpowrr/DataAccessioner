Updating FITS for Data Accessioner
==================================

*Last update:  Oct. 2, 2023*

Data Accessioner is a Java GUI wrapped around FITS (the Harvard 
*File Information Tool Set*);  FITS itself is a wrapper around a 
number of open source file identification toolkits.

Data Accessioner:  http://www.dataaccessioner.org/

FITS:  http://projects.iq.harvard.edu/fits/home

Data Accessioner (as of version 1.2) contains FITS 1.6.0, with the
following tools enabled:

* ADL Tool
* WebVTT Tool
* Droid
* JHove
* Jpylyzer
* File Utility
* ExifTool
* MediaInfo
* NLNZ Metadata Extractor
* OIS File Information
* OIS XML Information
* OIS File Information
* FFIdent
* Apache Tika

Note that Jpylyzer and MediaInfo may not be enabled on all platforms;
see the FITS documentation for more information on workarounds. If
a tool cannot be loaded by FITS upon startup, it will not be included
in the DataAccessioner list of FITS tools.

FITS Tools page:  http://projects.iq.harvard.edu/fits/fits/tools

## FITS background:  Technical Information

FITS separates its toolkits into two directories:

  * `lib/`:  jar files for the included tools.  
     Many of the tools are segregated into their own subdirectories, to
     avoid conflicts and allow for easier upgrade of a single tool.  
     Shared libraries and scripts are at the top level of this directory.
  * `tools/`:  configuration files for individual tools, and tools
     that are not Java-based (perl, compiled binaries, etc.)  Each tool
     will have its own subdirectory.
    
In addition, FITS has an `xml/` directory, which contains per-tool stylesheets
to convert tool output to FITS XML, as well as some other tool configuration 
files.

The file `xml/fits.xml` is the master configuration file for FITS.  It 
lists the tools that FITS has at its disposal;  the tools higher in 
the list have priority over ones lower in the list, and will be run
first.

See http://projects.iq.harvard.edu/fits/fits/fits-configuration-files 
for more information on configuring FITS.

## Data Accessioner and FITS

DataAccessioner contains a complete binary distribution of FITS.  You may
use another instance of FITS installed on your machine, by setting the
FITS_HOME environment variable to the path to your local installation of FITS.

Note that if you run DataAccessioner using a different version of FITS at a 
different location, you may run into version incompatibility issues.  See
the section on *Dependency Management* to update DataAccessioner to use a 
different version of FITS.

### FITS Logging

The only file that is modified in the FITS instsall distributed with
DataAccessioner is the file `fits/log4j2.xml`; it has been changed to log 
all FITS messages to the console. DataAccessioner also captures and logs 
output from FITS in the file `logs/da.log`. You may use the default 
`log4j2.xml` file distributed with FITS, at the expense of some cryptic 
warning messages and duplicate output in a separate `fits.log` file.

### Dependency Management with Maven

Building DataAccessioner requires Maven version 3.9 or above.

The file `pom.xml` in the source code root contains all the Maven
dependencies for both Data Accessioner and FITS.

Versions for the tools and their jar files are specified near the top
of the file, in the `<properties>` element.  

An effort has been made to retrieve as many dependencies as possible
from the Maven Central Repository.  For many tools and jars,
simply updating the dependency's version in the `pom.xml` file and 
rebuilding DataAccessioner will suffice.

However, some of the jars used in FITS (including the FITS jar itself)
are not available from any public repository.  They are distributed with
FITS itself.  The DataAccessioner POM has an `install` section to install
these local jars to your local maven repository;  the command

    mvn validate

installs these jars to your repo from the FITS distribution.

**Rebuilding DataAccessioner**:

Run this command at the source code directory root:

    mvn clean package

The jar and distribution zip file will be placed in the `target/` subdirectory.

## Updating a Tool

To update a specific tool distributed with FITS:

First, update FITS alone and test:

*  Download FITS to a working directory
*  Update the tool's jars in the `lib/` directory
*  Update, if needed, any tool configuration files in the `tools/`
   and `xml/` directory
*  Update, if needed, the FITS xslt files for the tool in the `xml/`
   directory
*  Run FITS against some sample files, make sure the tool works as 
   expected.
   
Then incorporate the new tool into Data Accessioner:

*  Fork the DataAccessioner git repo, then check your fork out locally
*  Copy the updated jars to `fits/lib`
*  Update any needed config files in `fits/tools/` and
   `fits/xml/`
*  Update any changed XSLT files in `fits/xml/`
*  Look at the new jar files installed for the tool in the FITS `lib/`
   directory, then:
   *  Update the versions of any dependencies already declared in `pom.xml`
   *  Add new dependencies available in Maven Central Repository to the 
      section for the tool's dependencies in `pom.xml`
   *  OR:  Install any new dependencies to your local repository by adding the
      local jars to install to the `maven-install-plugin` section in `pom.xml`, 
      then re-run `mvn validate`.
*  Build dataaccessioner and test
*  Commit and push your changes to your forked git repository
*  Submit a pull request to the `DataAccessioner/DataAccessioner` repository

## Updating the DROID signature file

Updating the DROID signature file does not require any source code modifications.  It can
be updated on any installed instance of DataAccessioner.

However, it does require downloading the source distribution of FITS from GitHub
(https://github.com/harvard-lts/fits). The FITS build process downloads the
Droid signature file and makes some changes to it. 

* Move the current DataAccessioner DROID signature files out of the way
  (but save them in case you need to restore them).  `V###` and `YYYYMMDD`
  should be replaced with the version number and date string in the file names.

        mv fits/tools/DROID_SignatureFile_V###_Alt.xml fits/tools/DROID_SignatureFile_V###_Alt.xml.ORIG
        mv fits/tools/DROID_SignatureFile_V###.xml fits/tools/DROID_SignatureFile_V###.xml.ORIG
        mv fits/tools/container-signature-YYYYMMDD.xml fits/tools/container-signature-YYYYMMDD.xml.ORIG

*  Check out the tagged version of FITS distributed with DataAccessioner from GitHub:
   https://github.com/harvard-lts/fits
   The version of FITS used in DataAccessioner is in the file `fits/version.properties`.

*  Change into the fits repo directory, and run the maven command 
   `mvn -P update-droid-sigs generate-resources`

*  Copy the following generated files in the fits repo to the `fits/tools/droid` directory 
   under your DataAccessioner directory:

        tools/droid/container-signature-*.xml
        tools/droid/DROID_SignatureFile_*.xml

*  Edit the following lines in the DataAccessioner file `fits/xml/fits.xml` to update the 
   Droid version. `V###` and `YYYYMMDD` should be replaced with the version number and 
   date string in the new file names.

        <!-- file name of the droid signature file to use in tools/droid/-->
        <droid_sigfile>DROID_SignatureFile_V###_Alt.xml</droid_sigfile>
        <droid_container_sigfile>container-signature-YYYYMMDD.xml</droid_container_sigfile>

## Updating FITS

* Make a backup copy of the current `fits/` tree somewhere outside the 
  DataAccessioner folder, for reference.
* Delete the folder tree `fits/`.
* Install the new FITS under `fits/`.
* Examine and update FITS core jar dependency versions in `pom.xml`.
* Update the versions of the local jars in `pom.xml`, and re-run
  `mvn validate`.
* Copy the `fits/log4j2.xml` file distributed with DataAccessioner
  over the file distributed with FITS.
* Rebuild DataAccessioner.
* Run DataAccessioner and test that all the tools function normally.