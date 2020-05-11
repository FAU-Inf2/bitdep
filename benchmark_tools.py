import configparser

config = configparser.ConfigParser()
config.read('config')

my_basedir = config['Files']['MyBasedir']
guava_jar = config['Files']['GuavaJar']



def skip_ws(f, lookahead=None):
    if not lookahead:
        lookahead = f.read(1)
    while lookahead and lookahead.isspace():
        lookahead = f.read(1)
    return lookahead


def parse_ident(f, lookahead=None):
    lookahead = skip_ws(f, lookahead)
    if not lookahead.isidentifier() and lookahead != '.':
        raise Exception('Parse error at ' + lookahead)
    buf = list()
    while lookahead and (lookahead.isidentifier() or lookahead == '.'):
        buf.append(lookahead)
        lookahead = f.read(1)
    return ("".join(buf), lookahead)


def parse_bit_width(f, lookahead=None):
    if not lookahead:
        lookahead = f.read(1)
    result = 0
    while lookahead and lookahead.isdigit():
        result = result * 10 + int(lookahead)
        lookahead = f.read(1)
    return (result, lookahead)


def parse_number(f, lookahead=None):
    lookahead = skip_ws(f, lookahead)
    if not lookahead.isdigit() and lookahead != '-':
        raise Exception('Parse error at ' + lookahead)
    accum = 0
    base = 10
    check = lambda x: x.isdigit()
    negative = False
    if lookahead == '-':
        negative = True
        lookahead = f.read(1)
    if lookahead == '0':
        lookahead = f.read(1)
        if lookahead == 'x' or lookahead == 'X':
            base = 16
            lookahead = f.read(1)
            check = lambda x: x.isdigit() or x in {'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F' }
        if lookahead == 'b' or lookahead == 'B':
            base = 2
            lookahead = f.read(1)
            check = lambda x: x in {'0', '1'}
    while lookahead and check(lookahead):
        accum = accum * base + int(lookahead, base=base)
        lookahead = f.read(1)
    if negative:
        accum = -accum
    if lookahead != ':':
        return ((accum, ), lookahead)
    (bit_width, lookahead) = parse_bit_width(f)
    return ((accum, bit_width), lookahead)


def parse_atom_or_expr(f, lookahead):
    assert len(lookahead) == 1
    if lookahead == '(':
        return parse_expr(f, lookahead)
    if lookahead.isidentifier() or lookahead == '.':
        return parse_ident(f, lookahead)
    if lookahead.isdigit() or lookahead == '-':
        return parse_number(f, lookahead)
    raise Exception('Parse error at ' + lookahead)


def parse_expr(f, lookahead):
    assert len(lookahead) == 1
    if lookahead != '(':
        raise Exception('Parse error at ' + lookahead)
    (ident, lookahead) = parse_ident(f)
    lookahead = skip_ws(f, lookahead)
    args = list()
    while lookahead and lookahead != ')':
        (elem, lookahead) = parse_atom_or_expr(f, lookahead)
        args.append(elem)
        lookahead = skip_ws(f, lookahead)
    if lookahead != ')':
        raise Exception('Reached end of file while parsing')
    lookahead = f.read(1)
    return ((ident, args), lookahead)



def parse(f):
    lookahead = f.read(1)
    if not lookahead:
        raise Exception()
    top_level = list()
    while True:
        (result, lookahead) = parse_expr(f, lookahead)
        top_level.append(result)
        lookahead = skip_ws(f, lookahead)
        if not lookahead:
            break
    return top_level



def convert_func_to_my(fs):
    if isinstance(fs, str):
        return fs
    if not isinstance(fs, tuple):
        raise Exception('Invalid function ' + str(fs))
    if isinstance(fs[0], int):
        return ":".join(map(str, fs))
    if fs[0] == 'extract':
        return '(extract ' + str(fs[1][0][0]) + ' ' + str(fs[1][1][0]) + ' ' + convert_func_to_my(fs[1][2]) + ')'
    return '(' + fs[0] + ' ' + ' '.join(map(convert_func_to_my, fs[1])) + ')'



def convert_spec_to_my(spec):
    num_inputs = spec[0][0]
    bit_width = spec[1][0]
    inputs = [ spec[2 + i] for i in range(num_inputs) ]
    func = convert_func_to_my(spec[2 + num_inputs])
    return "bitwidth " + str(bit_width) + "\nspec " + str(num_inputs) + " " + " ".join(inputs) + " -> " + func



def convert_lib_to_my(lib, bit_width):
    result = list()
    for f in lib:
        if isinstance(f, tuple):
            if f[0] == "const":
                if len(f[1]) == 1:
                    result.append(f[0] + " " + str(f[1][0][0]))
                else:
                    result.append(f[0] + " " + str(f[1][0][0]) + " " + str(f[1][1][0]))
            elif f[0] == "fun":
                args = f[1][1:-1]
                result.append('(' + f[1][0] + ', ' + str(len(args)) + ', ' + ' '.join(args) + ' -> ' + convert_func_to_my(f[1][len(f[1]) - 1]) + ')')
            else:
                raise Exception("Unknown library function " + str(f))
        elif f in {'eq', 'neq', 'ult', 'ule', 'ugt', 'uge', 'slt', 'sle', 'sgt', 'sge'}:
            result.append('(' + f + ', 2, x y -> (ite (' + f + ' x y) 1:' + str(bit_width) + ' 0:' + str(bit_width) + '))')
        else:
            result.append(f)
    return "lib " + ", ".join(result)



def convert_to_my(res, of):
    spece = None
    libe = None
    for e in res:
        if e[0] == 'lib':
            libe = e[1]
        elif e[0] == 'spec':
            spece = e[1]
        else:
            raise Exception('Invalid entry ' + str(e))
    print(convert_spec_to_my(spece), file=of)
    print(convert_lib_to_my(libe, spece[1][0]), file=of)
    print("algo list", file=of)
    print("timing", file=of)
    print("synth", file=of)
    print("quit", file=of)



def convert_funcname_to_sygus(fn, bool_ctx=False):
    if fn == "eq":
        return "="
    if fn == "neq":
        return "distinct"
    if fn == "ite":
        return "ite"
    if fn == "concat":
        return "concat"
    if fn.startswith('cust_'):
        return fn
    if bool_ctx and fn in {'and', 'or', 'xor'}:
        return fn
    return "bv" + fn



def get_funcname_arity(fn, nargs):
    if fn in nargs:
        return nargs[fn]
    if fn.startswith('op_'):
        return get_funcname_arity(fn[3:])
    if fn == "ite":
        return 3
    if fn == "not" or fn == "neg":
        return 1
    return 2



def convert_func_to_sygus(fs, bool_ctx=False):
    if isinstance(fs, str):
        return fs
    if not isinstance(fs, tuple):
        raise Exception('Invalid function ' + str(fs))
    if isinstance(fs[0], int):
        m = (1 << fs[1]) - 1
        return '#b' + format(fs[0] & m, '0' + str(fs[1]) + 'b')
    if fs[0] == 'extract':
        return '((_ extract ' + str(fs[1][1][0]) + ' ' + str(fs[1][0][0]) + ') ' + convert_func_to_sygus(fs[1][2]) + ')'
    if fs[0] == 'ite':
        return '(ite ' + convert_func_to_sygus(fs[1][0], True) + ' ' + convert_func_to_sygus(fs[1][1]) + ' ' + convert_func_to_sygus(fs[1][2]) + ')'
    return '(' + convert_funcname_to_sygus(fs[0], bool_ctx) + ' ' + ' '.join(map(convert_func_to_sygus, fs[1])) + ')'



def convert_spec_to_sygus(spec):
    num_inputs = spec[0][0]
    bit_width = spec[1][0]
    inputs = [ spec[2 + i] for i in range(num_inputs) ]
    func = convert_func_to_sygus(spec[2 + num_inputs])
    spec_fun = "(define-fun spec (" + " ".join( "(" + x + " (_ BitVec " + str(bit_width) + "))" for x in inputs ) + ") (_ BitVec " + str(bit_width) + ") " + func + ")"
    decl_vars = "\n".join( "(declare-var " + x + " (_ BitVec " + str(bit_width) + "))" for x in inputs )
    return (inputs, spec_fun + "\n" + decl_vars + "\n")



def convert_lib_to_sygus(lib, bit_width, inputs):
    custom_defs = ''
    const_bit_width = None
    op_counts = dict()
    nargs = dict()
    for f in lib:
        if isinstance(f, tuple):
            if f[0] == "const":
                if const_bit_width and const_bit_width != f[1][0][0]:
                    raise Exception('Multiple consts of different width')
                if len(f[1]) == 1:
                    const_bit_width = f[1][0][0]
                else:
                    m = (1 << f[1][0][0]) - 1
                    f = '#b' + format(f[1][1][0] & m, '0' + str(f[1][0][0]) + 'b')
                    if f in op_counts:
                        op_counts[f] += 1
                    else:
                        op_counts[f] = 1
            elif f[0] == "fun":
                args = f[1][1:-1]
                if 'cust_' + f[1][0] not in op_counts:
                    custom_defs += '(define-fun cust_' + f[1][0] + ' ('
                    custom_defs += ' '.join( '(' + n + ' (_ BitVec ' + str(bit_width) + '))' for n in args )
                    custom_defs += ') (_ BitVec ' + str(bit_width) + ') '
                    custom_defs += convert_func_to_sygus(f[1][len(f[1]) - 1]) + ')\n'
                f = 'cust_' + f[1][0]
                nargs[f] = len(args)
                if f in op_counts:
                    op_counts[f] += 1
                else:
                    op_counts[f] = 1
            else:
                raise Exception("Unknown library function " + str(f))
        else:
            if f in op_counts:
                op_counts[f] += 1
            else:
                op_counts[f] = 1
    ops = list(op_counts.keys())
    bitsperop = 4
    nbits = max(bitsperop, len(ops) * bitsperop)
    ops_req = ''
    for i in range(len(ops)):
        f = ops[i]
        if not f.startswith('#'):
            custom_defs += '(define-fun op_' + f + ' ('
            custom_defs += ' '.join( '(i' + str(i) + ' (_ BitVec ' + str(nbits + bit_width) + '))' for i in range(get_funcname_arity(f, nargs)) )
            custom_defs += ') (_ BitVec ' + str(nbits + bit_width) + ') '
            custom_defs += '(concat (bvadd #b' + format(1 << (i * bitsperop), '0' + str(nbits) + 'b') + ' '
            custom_defs += ' '.join( '((_ extract %d %d) i%d)' % (nbits + bit_width - 1, bit_width, j) for j in range(get_funcname_arity(f, nargs)) )
            if f in {'eq', 'neq', 'ult', 'ule', 'ugt', 'uge', 'slt', 'sle', 'sgt', 'sge'}:
                custom_defs += ') (ite (' + convert_funcname_to_sygus(f) + ' '
                custom_defs += ' '.join( '((_ extract ' + str(bit_width - 1) + ' 0) i' + str(j) + ')' for j in range(get_funcname_arity(f, nargs)) )
                custom_defs += ') ' + convert_func_to_sygus((1, bit_width)) + ' ' + convert_func_to_sygus((0, bit_width))
            else:
                custom_defs += ') (' + convert_funcname_to_sygus(f) + ' '
                custom_defs += ' '.join( '((_ extract ' + str(bit_width - 1) + ' 0) i' + str(j) + ')' for j in range(get_funcname_arity(f, nargs)) )
            custom_defs += ')))\n'
            ops_req = format(op_counts[f], '0' + str(bitsperop) + 'b') + ops_req
        else:
            ops_req = format(0, '0' + str(bitsperop) + 'b') + ops_req
    ops_req = '#b' + ops_req
    def mk_name(c):
        return 'Start_' + "".join(map(str, c))
    q = [ [ 0 for x in ops ] ]
    v = { tuple(q[0]) }
    result = "\n(synth-fun res (" + " ".join( "(" + x + " (_ BitVec " + str(bit_width) + "))" for x in inputs ) + ") (_ BitVec " + str(nbits + bit_width) + ") ("
    while len(q) > 0:
        cur = q.pop()
        result += '(' + mk_name(cur) + ' (_ BitVec ' + str(nbits + bit_width) + ') ((concat #b' + ('0' * nbits) + ' Var)'
        if const_bit_width:
            result += ' (concat #b' + ('0' * nbits) + ' Const)'
        for i in range(len(ops)):
            if ops[i].startswith('#'):
                result += ' (concat #b' + ('0' * nbits) + ' ' + ops[i] + ')'
            elif cur[i] < op_counts[ops[i]]:
                new_cur = list(cur)
                new_cur[i] += 1
                #fname = convert_funcname_to_sygus(ops[i])
                args = ' '.join( mk_name(new_cur) for x in range(get_funcname_arity(ops[i], nargs)) )
                #if ops[i] in {'eq', 'neq', 'ult', 'ule', 'ugt', 'uge', 'slt', 'sle', 'sgt', 'sge'}:
                #    result += ' (ite (' + fname + ' ' + args + ') ' + convert_func_to_sygus((1, bit_width)) + ' ' + convert_func_to_sygus((0, bit_width)) + ')'
                #else:
                #    result += ' (' + fname + ' ' + args + ')'
                result += ' (op_' + ops[i] + ' ' + args + ')'
                if tuple(new_cur) not in v:
                    v.add(tuple(new_cur))
                    q.append(new_cur)
        result += '))\n'
    if const_bit_width:
        result += '(Const (_ BitVec ' + str(const_bit_width) + ') ((Constant (_ BitVec ' + str(const_bit_width) + '))))\n'
    result += '(Var (_ BitVec ' + str(bit_width) + ') ((Variable (_ BitVec ' + str(bit_width) + '))))\n'
    if len(ops_req) < 2 + nbits:
        ops_req = '#b' + ('0' * nbits)
    return (custom_defs, ops_req, result)



def convert_to_sygus(res, of):
    spece = None
    libe = None
    for e in res:
        if e[0] == 'lib':
            libe = e[1]
        elif e[0] == 'spec':
            spece = e[1]
        else:
            raise Exception('Invalid entry ' + str(e))
    # TODO
    (inputs, specres) = convert_spec_to_sygus(spece)
    (defs, ops_req, libres) = convert_lib_to_sygus(libe, spece[1][0], inputs)
    print("(set-logic BV)", file=of)
    print(defs, file=of)
    print(specres, file=of)
    print(libres, file=of)
    print('))', file=of)
    print('(constraint (= (concat ' + ops_req + ' (spec ' + ' '.join(inputs) + ')) (res ' + ' '.join(inputs) + ')))', file=of)
    print('(check-synth)', file=of)
