import sys
import math

n = int(input())  # Number of elements which make up the association table.
q = int(input())  # Number Q of file names to be analyzed.

dicoExt = {}

for i in range(n):
    ext, mt = input().split()
    dicoExt[ext.upper()]=mt
    
for i in range(q): 

    fname = input().split(".") 
    dicKey = fname[-1].upper()

    if (dicKey in dicoExt and len(fname)>=2):
        print(dicoExt[dicKey])
    else:
        print("UNKNOWN")

