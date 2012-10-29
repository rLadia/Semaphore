#creates 26 empty png files named imgA, imgB, imgC...

import string

for i in string.ascii_uppercase:
    fileName = "img" + i + ".gif"
    file = open(fileName, 'w')
    file.close()
    print(fileName + " created.")
    

