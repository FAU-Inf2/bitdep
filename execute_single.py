#!/usr/bin/python3
import random
from subprocess import run, DEVNULL, PIPE, TimeoutExpired
from tempfile import NamedTemporaryFile
import time

from benchmark_tools import *



timeout = 10*60



def run_cvc4(filename):
    try:
        start_time = time.time()
        res = run(["cvc4", "--lang=sygus", filename], timeout=timeout, stdout=PIPE, stderr=PIPE, universal_newlines=True)
        end_time = time.time()
        if res.returncode != 0:
            return '{ "status": "error" }'
        sat = "unsat" in [ ln.strip() for ln in res.stdout.splitlines() ]
        unknown = "unknown" in [ ln.strip() for ln in res.stdout.splitlines() ]
        status = "sat" if sat else ("unknown" if unknown else "unsat")
        return '{ "status": "%s", "time_ms": %.2f }' % (status, (end_time - start_time) * 1000)
    except TimeoutExpired:
        return '{ "status": "timeout" }'



def run_gjtv(f):
    try:
        res = run(["java", "-cp", my_basedir + "/build/libs/bitdep.jar:" + guava_jar, "synth.SynthesizerShell"], timeout=timeout, stdin=f, stdout=PIPE, stderr=DEVNULL, universal_newlines=True, env={ "LD_LIBRARY_PATH": my_basedir + "/libs:" + my_basedir + "/build/libs/yicesjni/shared" })
        if res.returncode != 0:
            return '{ "status": "error" }'
        stdout_lines = res.stdout.splitlines()
        i = len(stdout_lines) - 1
        sat = "sat" in [ ln.strip().lstrip(" >") for ln in stdout_lines ]
        status = "sat" if sat else "unsat"
        while i >= 0:
            if stdout_lines[i].startswith("("):
                return '{ "status": "%s", "time_ms": %.2f }' % (status, float(stdout_lines[i][1:].split()[0]))
            i -= 1
        return '{ "status": "error" }'
    except TimeoutExpired:
        return '{ "status": "timeout" }'



def run_filter(pr):
    spece = None
    libe = None
    for e in pr:
        if e[0] == 'lib':
            libe = e[1]
        elif e[0] == 'spec':
            spece = e[1]
        else:
            raise Exception('Invalid entry ' + str(e))
    libstr = convert_lib_to_my(libe, spece[1][0])[4:]
    lib = list()
    j = 0
    plevel = 0
    for i in range(len(libstr)):
        if libstr[i] == ',' and plevel == 0:
            lib.append(libstr[j:i].strip())
            j = i + 1
        elif libstr[i] == '(':
            plevel += 1
        elif libstr[i] == ')':
            plevel -= 1
    lib.append(libstr[j:].strip())
    num_inputs = spece[0][0]
    bit_width = spece[1][0]
    inputs = [ spece[2 + i] for i in range(num_inputs) ]
    func = convert_func_to_my(spece[2 + num_inputs])
    specf = " ".join(inputs) + " -> " + func
    res = run(["java", "-cp", my_basedir + "/build/libs/bitdep.jar:" + guava_jar, "analysis.essential.ShapeFeasibilityChecker", str(bit_width), str(num_inputs), specf] + lib, stdout=PIPE, stderr=DEVNULL, universal_newlines=True)
    stdout_lines = res.stdout.splitlines()
    return '{ "status": "%s", "time_ms": %s }' % tuple(stdout_lines)


def execute_single(res):
    print('"filter": ' + run_filter(res) + ',')
    with NamedTemporaryFile('w', encoding='utf-8') as of:
        convert_to_sygus(res, of)
        of.flush()
        print('"cvc4": ' + run_cvc4(of.name) + ',')
    with NamedTemporaryFile('w+', encoding='utf-8', delete=False) as of:
        convert_to_my(res, of)
        of.seek(0)
        print('"gjtv": ' + run_gjtv(of))



def enum_random(res, n, replacements=["and", "or", "xor", "not", "neg", "add", "sub", "mul", "shl", "ashr", "sdiv", "srem"]):
    spece = None
    libe = None
    for e in res:
        if e[0] == 'lib':
            libe = e[1]
        elif e[0] == 'spec':
            spece = e[1]
        else:
            raise Exception('Invalid entry ' + str(e))
    nonconsts = [ x for x in libe if not isinstance(x, tuple) or x[0] != "const" ]
    consts = [ x for x in libe if isinstance(x, tuple) and x[0] == "const" ]
    if len(nonconsts) == 0:
        nonconsts = consts
        consts = []
    nlibs = list()
    while len(nlibs) < n:
        nlib = random.choices(replacements, k=len(nonconsts))
        while nlib == nonconsts or (nlib + consts) in nlibs:
            nlib = random.choices(replacements, k=len(nonconsts))
        nlibs.append(nlib + consts)
    return [ (("spec", spece), ("lib", libn)) for libn in nlibs ]



def libf_to_str(lf):
    if isinstance(lf, tuple) or isinstance(lf, list):
        return ' '.join(libf_to_str(x) for x in lf)
    return str(lf)



def main(args, seed=983302, n=10):
    random.seed(seed)
    print('[')
    for i in range(len(args)):
        fn = args[i]
        with open(fn, "r", encoding="utf-8") as f:
            print('{')
            print('"filename": "' + str(fn + '",'))
            res = parse(f)
            print('"original": {')
            execute_single(res)
            print('},')
            print('"mutations": [')
            first = True
            for tc in enum_random(res, n):
                if not first:
                    print(',')
                print('{')
                first = False
                (_, (_, lib)) = tc
                print('"lib": [' + ', '.join( '"' + libf_to_str(f) + '"' for f in lib ) + '],')
                execute_single(tc)
                print('}')
            print(']')
            print('},' if i + 1 < len(args) else '}')
    print(']')


if __name__ == "__main__":
    import sys
    main(sys.argv[1:])
