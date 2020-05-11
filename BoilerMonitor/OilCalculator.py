import Database
import datetime
import sys
from pprint import pprint

totalDurationInSeconds = 0
boilerBurnRate = .65 # Our boiler burns .65 gallons per hour

def timeDiffInSeconds(startTime, endTime): # returns the amount of minutes that have elapsed between two given datetimes
  timedelta = endTime - startTime # difference is calculated here and returned in datetime format 
  return int(timedelta.seconds) # timedelta is converted into seconds and returned as a whole number

def timeDiffInMinutes(dt2, dt1): # returns the amount of minutes that have elapsed between two given datetimes
  timedelta = dt2 - dt1 # difference is calculated here and returned in datetime format 
  return int(timedelta.seconds / 60) # timedelta is converted into seconds and divided by 60 to get us the elapsed time in minutes, then returns that as a whole number

def calculateOilBurned(totalDurationInSeconds):
    print(totalDurationInSeconds)
    minutes = float(totalDurationInSeconds / 60.0)
    print(minutes)
    hours = float(minutes / 60.0)
    gallonsBurned = float("{:.2f}".format(hours * boilerBurnRate))
    print(hours)
    return gallonsBurned
    
# Only runs when arguments are provided
if(len(sys.argv) > 2):
    dateTimeOne = sys.argv[1]
    dateTimeTwo = sys.argv[2]
    result = Database.selectBetweenDateTimes(dateTimeOne, dateTimeTwo)
    
    for row in result:
        temperature = row[0]
        startTime = row[1]
        endTime = row[2]
        timeDifferenceInSeconds = timeDiffInSeconds(startTime, endTime)
        totalDurationInSeconds = totalDurationInSeconds + timeDifferenceInSeconds
    
    gallonsBurned = calculateOilBurned(totalDurationInSeconds)
    print("OIL BURNED|%s" % (str(gallonsBurned)))