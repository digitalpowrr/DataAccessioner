# Data Accessioner

The DataAccessioner is a simple Java Swing GUI to allow archivists 
and digital preservationists to migrate data off disks and onto a 
file server for basic preservation, further appraisal, arrangement, 
and description.

It provides a wrapper around the FITS toolkit 
(<http://projects.iq.harvard.edu/fits/home>) to integrate common 
metadata tools to analyze and describe found content at the time 
of migration.

DataAccessioner 1.2 requires Java 11 or above. We recommend using Adoptium, an open source Java SE build based upon OpenJDK:
https://adoptium.net/temurin/releases/?version=11

Jpylyzer requires Windows and Python.

### MediaInfo Notes

The version of MediaInfo distributed with FITS for Linux is installed for 
Ubuntu, and the version of MediaInfo distributed with FITS 1.6.0 for Windows will 
not work in Windows 11. If you are running DataAccessioner on a platform that does
not support the supplied version of MediaInfo, try installing MediaInfo directly on 
your system and delete the copy in `fits/tools/mediainfo`. After deleting the 
copy in FITS, FITS will then attempt to use the system copy.  This may not work,
however, if the version of MediaInfo installed on your system is different from
the one distributed with FITS.  You can see the version of MediaInfo distributed
with FITS in the file `fits/tools/mediainfo/version`.

## Download

You can download the latest release from the Github repository
at https://github.com/digitalpowrr/DataAccessioner/releases/

## Install

Unzip the distribution zip file in the location of your choice.
All dependencies are included within the package.

## Run

### Windows

Double-click the `dataaccessioner.jar` file or `start.bat` in the install directory.

### Mac or Linux

Open a terminal application (Terminal in Mac), then run the following commands:

    cd <DATAACCESSIONER_DIRECTORY>
    ./start.sh

Where *<DATAACCESSIONER_DIRECTORY>* is the folder where you installed DataAccessioner.

If you are unfamiliar with using the Terminal to launch programs, please see this [step-by-step guide] (<https://drive.google.com/file/d/1QOkTtuQIeI3nbJq-5NlDbdpfN442UZ-n/view?usp=drive_link>) to walk you through the process.

## For Developers

### Build prerequisites

* A recent version of Java (>= 11)
* Perl (for exiftool)
* Maven (>= 3.9)

### Build 
Data Accessioner (>= 1.1) is built using Maven.  To build, clone the
repository, then execute the following command in the top-level
directory:

    mvn validate

This only needs to be run once, the first time you build the package.
This command copies local FITS jars into your local maven repository.

To build the package:

    mvn clean package

A distributable zip file will be built and placed in the `target/`
subdirectory, with the name `dataaccessioner-<version>-dist.zip`.

For testing purposes, you may run the created jar in the `target/`
folder directly:

    FITS_HOME=fits/ java -jar target/dataaccessioner-1.2-jar-with-dependencies.jar

### Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request

## History

The very first version of the tool was written in under the 
auspices of Duke University Archives in early 2008. In January 
2009 the Data Accessioner was revisited and refactored with a 
revised architecture, and the metadata tool adapters and the 
custom metadata manager were extracted to be used as plugins.

Versions 0.3.1 and 1.0 were made possible by the Digital POWRR 
Project (<http://digitalpowrr.niu.edu/>);  at that point, the tool 
became an open source application, unaffiliated to any institution.  
Version 1.0 is the first version to use FITS as a metadata tool 
wrapper.

Version 1.1 was made possible by The Archives of the Episcopal
Church (http://www.episcopalarchives.org).  It was refactored 
to use Maven to manage dependencies, and contains several minor 
enhancements.

Version 1.2 was made possible by Digital POWRR 
(https://digitalpowrr.niu.edu). It was updated to clean up several
security and deprecation vulnerabilities, and update to the most recent 
version of FITS.

## Credits

[Seth Shaw](https://github.com/seth-shaw)  
[Scott Prater](https://github.com/sprater)

## License

Copyright © 2024 by Digital POWRR.   
Copyright © 2014, 2017 by Seth Shaw.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
