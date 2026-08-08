from pathlib import Path
import argparse

def root_parser(description:str, self_test:bool=False):
    ap=argparse.ArgumentParser(description=description)
    ap.add_argument('--root',type=Path,default=Path('.'))
    if self_test: ap.add_argument('--self-test',action='store_true')
    return ap
