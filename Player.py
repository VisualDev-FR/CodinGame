import sys
import math

# Auto-generated code below aims at helping you parse
# the standard input according to the problem statement.

magic_phrase = input()

print(magic_phrase, file=sys.stderr, flush=True)

# Write an action using print
# To debug: print("Debug messages...", file=sys.stderr, flush=True)

print("+.>-.")

def increase_letter(letter):
    if letter == 'z':
        return 'a'
    else:
        return chr(ord(letter) + 1)
