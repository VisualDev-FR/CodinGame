def x():return input().split()
i=x()
y=int
b={}
for j in[1]*y(i[7]):r,s=x();b[r]=y(s)
while 1:d,f,h=x();g=(y(i[4]),b.get(d))[d in b];print(("WAIT","BLOCK")[h=="RIGHT"and g<y(f)or h=="LEFT"and g>y(f)])