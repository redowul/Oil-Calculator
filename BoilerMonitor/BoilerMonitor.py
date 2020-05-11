import RPi.GPIO as GPIO  
from datetime import datetime
import Database
import Temperature
import OilCalculator
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(11, GPIO.IN, pull_up_down = GPIO.PUD_DOWN) # the pin we use to detect electrical current. For future reference, the current flows between a 3.3v pin (pin 1) and GPIO 11 (pin 23)  
contact      = False                                  # boolean to ensure time measurement only occurs when an electrical current is flowing
startTime    = None                                   # datetime of when the boiler starts
endTime      = None                                   # datetime of when the boiler stops

try:
    while True:
        if GPIO.input(11) == 1 and contact == False: # contact is made; the boiler is running
            startTime = datetime.now()
            contact = True
        elif GPIO.input(11) == 0 and contact == True: # contact broken, the boiler has stopped
            endTime = datetime.now()
            if(OilCalculator.timeDiffInSeconds(startTime, endTime) != 0):
                Database.insertData(startTime, endTime) # send the data to be stored in the database				
            contact = False  
except:
    GPIO.cleanup()