import sys
import math

# Auto-generated code below aims at helping you parse
# the standard input according to the problem statement.

n = int(input())  # Number of elements which make up the association table.
q = int(input())  # Number Q of file names to be analyzed.

dicoExt = {}

for i in range(n):
    # ext: file extension
    # mt: MIME type.
    ext, mt = input().split()

    dicoExt[ext.upper()]=mt
    
for i in range(q):    
    fname = input().split(".")  # One file name per line.

    dicKey = fname[-1].upper()

    if (dicKey in dicoExt and len(fname)>=2):
        print(dicoExt[dicKey])
    else:
        print("UNKNOWN")

# Write an answer using print
# To debug: print("Debug messages...", file=sys.stderr, flush=True)


# For each of the Q filenames, display on a line the corresponding MIME type. If there is no corresponding type, then display UNKNOWN.

