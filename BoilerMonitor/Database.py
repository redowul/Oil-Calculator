import mysql.connector

def initializeConnection():
    dbConnection = mysql.connector.connect(user='mysql_user_here',
                              password='',
                              host='localhost',
                              database='database_name_here')

    dbCursor = dbConnection.cursor()
    return dbConnection, dbCursor

def insertData(startTime, endTime): #def insertData(temperature, startTime, endTime):
    dbConnection, dbCursor = initializeConnection()
    if startTime != endTime:   
        dbCursor.execute("INSERT INTO boiler_monitor (starttime, endtime) VALUES (%s, %s)", (startTime, endTime))
        dbConnection.commit()
        
    dbConnection.close()
    
def selectBetweenDateTimes(dateTimeOne, dateTimeTwo):
    dbConnection, dbCursor = initializeConnection()
    
    dbCursor.execute("SELECT * FROM boiler_monitor WHERE starttime >= %s AND endtime <= %s", (dateTimeOne, dateTimeTwo))
    result = dbCursor.fetchall()

    dbConnection.close()
    
    return result
    
def selectAllData():
    dbConnection, dbCursor = initializeConnection()
    
    dbCursor.execute("SELECT * FROM boiler_monitor")
    result = dbCursor.fetchall()

    dbConnection.close()
    
    return result