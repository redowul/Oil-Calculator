## Oil Calculator - Measure oil usage over time (Java / Android)

This is a modified version of [AndroidSSH.](https://github.com/jonghough/AndroidSSH) 
that I'm using to enable a SSH connection between the raspberry pi that is acting as my server. 

## How to use -- Oil Calculator
* Plug your server host name, username, password, and port into the strings.xml file. 
* Set storage_path in strings.xml to the location of where the python files on your server are located. 
* For example: on Raspberry Pi, the default directory is /home/pi so if you set storage_path to "oilCalculator", 
you would be accessing python scripts stored in /home/pi/oilCalculator on the end device you're attempting to SSH into.
* Similarly, choosing to leave it blank means you just access files from the home directory instead (/home/pi)

Assuming your end device is configured correctly, you should now be able to automatically SSH into your server once you open this application on your device.
The end device should be able to interface with the Python code provided in the BoilerMonitor folder. 

## BoilerMonitor - Measure oil usage over time (Python / Raspberry Pi)

These are Python scripts written to measure the usage of oil over time. 
They are specifically written for use with a Raspberry Pi in mind (BoilerMonitor.py has references to GPIO pins in its code that are used to detect electrical currents.)

BoilerMonitor.py detects when an electrical current is flowing between an the boiler and the pins of the pi, 
which then calls Database.py to store the DateTime objects (which represent the start and end times of each run of the boiler) 
into a mySQL database stored internally on the Raspberry Pi. 

This data is then retrieved when OilCalculator.py is called, which is the script the Oil Calcululator Android application interfaces with. 
From an Android device running the software, two DateTime formatted strings are generated and sent via SSH to the pi. 
OilCalculator will retrieve all start and end times stored between those two DateTimes and calculate how much oil was burned over that period.

For example: My boiler's nozzle burns about .65 gallons per hour. 
The duration between the start and end times of each retrieved entry are converted to seconds before all entries are added together 
(so if an entry started at 1:00 PM and ended at 1:01 PM, we add 60 seconds to our total runtime.)
 
Basic addition tells us that if this total were to hit 3600 seconds, we could divide the result by 60 and determine that the boiler has run for an hour. 
Since the boiler burns .65 gallons an hour, we now know that we've burned .65 gallons! 
This calculation is performed for any duration in days selected from the Android device and the results are generated on the pi and sent back via SSH to be displayed on the screen.

## How to use -- BoilerMonitor
* Places the BoilerMonitor folder in the directory you're accessing as defined by storage_path in strings.xml of Oil Calculator.
* Make sure you only have a 3.3v connection running between one of the pi's 3.3v pins and applicable GPIO pins. 
You can find images online detailing which pin is which, but I'm using pin 1 and pin 11 on the Raspberry Pi 3 model B+. 
If you don't lower the voltage to 3.3 volts or connect to the right pins, you risk shorting out your pi.
* In Database.py, include the details of the database stored on your pi. The fields in question are user, password, host, and database. 
You can find out how to set up a mySQL database with a little bit of google-fu, but it's important to remember that "database" refers to your database name. 
On a linux system like Raspberry Pi, you can have multiple databases. This will only access one of them.
Additionally, the database is going to want to store two DateTime objects per entry: startTime and endTime.  
* In OilCalculator.py, set boilerBurnRate to the rate of your boiler's nozzle. 
(Most boilers should have a literal nozzle showing how much oil can flow at a time, mine literally says .65 on it.)