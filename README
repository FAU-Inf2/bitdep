# Identification of Synthesis Problems as Infeasible

This repository accompanies the research paper

Kamp, M., Philippsen, M.: Approximate Bit Dependency Analysis to Identify
Program Synthesis Problems as Infeasible. Formal Methods in Computer-Aided
Design (FMCAD 2020), Haifa, Israel. 2020

The repository holds a reference implementation of the described approximations
and algorithms.


## Building

The code relies on the SMT solver [Yices 2.5.4](https://yices.csl.sri.com/),
which is not included in this repository. You need to place the shared library
`libyices.so` into the `libs` directory.

To build the code, simply run `./gradlew build`. This command also runs the
test suite. Make sure that there are no failing tests, which possibly indicates
a missing dependency.


## Running the benchmark

First, CVC4 version 1.7 is required to run the benchmark. Then, you need to
create a configuration file for the evaluation script by running `./mkconfig`.
Now, to run the benchmark simply execute `python3 ./execute_single.py
benchmark/*`. This command outputs the time measurements of running each
benchmark in JSON format.
