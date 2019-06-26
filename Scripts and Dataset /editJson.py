import json
import random
import string
import names
from random import randint
from faker import Faker
fake = Faker()
import codecs

# with is like your try .. finally block in this case
with open('datasetJSON.txt', 'r') as file:
    # read a list of lines into data
    testData = file.readlines()


def random_with_N_digits(n):
    range_start = 10**(n-1)
    range_end = (10**n)-1
    return randint(range_start, range_end)

anomalies = 0
total = 0
def addAdditionalData(param):
    randType = random.randint(1,900)
    randFeildMissing = random.randint(1,900)

    global anomalies
    global total
    param['Username'] = names.get_first_name()

    #Type Change Anomaly
    if randType>1:
        param['CreditCard'] = random_with_N_digits(16)
    else:
        param['CreditCard'] = random_with_N_digits(16).__str__()


    #Missing Field Anomaly
    total += 1
    if randFeildMissing>1:
        param['Password'] = ''.join(random.choices(string.ascii_letters + string.digits, k=16))
    else:
        anomalies += 1

def changeData(param):
    rand = random.randint(1,900)
    sqlInjections = [' ‘ and 1=1--','‘ and 1=2-- ', ' UNION SELECT 1,@@version-- ', ' UNION SELECT 1,name FROM master..sysdatabases-- ']
    randSqlInjection = random.randint(1,900)

    param['Username'] = fake.name().replace(" ", "")[:8]
    if bool(random.getrandbits(1)):
        param['Password'] = ''.join(random.choices(string.ascii_letters + string.digits, k=16))

    #Type Change Anomaly
    if rand>1:
        param['CreditCard'] = random_with_N_digits(16)
    else:
        param['CreditCard'] = random_with_N_digits(16).__str__()

    #Type SQL Injection
    if randSqlInjection<2:
        username = fake.name().replace(" ", "")[:8].strip()
        param['Username'] = (username + random.choice(sqlInjections)).replace(u"\u2018", "'")
        print(username+"=====>"+param['Username'])


for count,row in enumerate(testData):
    jsonData = json.loads(row)
    addAdditionalData(jsonData)
    testData[count] = json.dumps(jsonData)+'\n'

count = 1000
row = 0
while count < 5000:
    data = json.loads(testData[row])
    changeData(data)
    testData.append(json.dumps(data)+'\n')
    count += 1
    row += 1
    if row > 999:
        row = 0

# for row in testData:
#     jsonData = json.loads(row)
#     print('JSON Edited', json.dumps(jsonData))

# and write everything back
print("Anomalies Generated:",anomalies)
print("Total Jsons Generated:",count)
with open('/Users/ZarakMac/Documents/testJson.txt', 'w') as file:
    file.writelines(testData)