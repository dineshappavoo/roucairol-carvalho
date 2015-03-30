#!/usr/bin/env python
import subprocess, os

def kill_after_terminate():
    os.system('''pkill -f  "cd /home/004/d/dx/dxa132330/advanced-operating-system/projects/roucairol-carvalho;java RoucairolCarvalho"''')



if __name__ == "__main__":
    kill_after_terminate()