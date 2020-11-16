# Identification of Synthesis Problems as Infeasible

This repository accompanies the research paper

Kamp, M., Philippsen, M.: Approximate Bit Dependency Analysis to Identify
Program Synthesis Problems as Infeasible. International Conference on
Verification, Model Checking, and Abstract Interpretation (VMCAI 2021),
Copenhagen, Denmark. 2021

The repository holds a reference implementation of the described approximations
and algorithms. Furthermore it contains the missing proofs from the paper in
the file `proofs.txt`.


## Building

The code relies on the SMT solver [Yices 2.6.2](https://yices.csl.sri.com/),
which is not included in this repository. You need to place the shared library
`libyices.so` into the `libs` directory.

To build the code, simply run `./gradlew build`. This command also runs the
test suite. Make sure that there are no failing tests, which possibly indicates
a missing dependency.
