# NRC

## Introduction

NRC is a RISC-V CPU hardware and software design following the tutorials from [YSYX (一生一芯) project.](https://ysyx.oscc.cc/docs/) (English version can be found here: https://ysyx.oscc.cc/docs/en/)

The purpose of this project is to learn RISCV-V architecture, CPU core/SoC design, and the corresponding software.

The target of this project is to create a CPU core and SoC that is capable of booting Linux.

This repository contains the CPU core RTL and its verification environment. To make design fast the HDL is written in [SpinalHDL](https://github.com/SpinalHDL/SpinalHDL).

## Repository  Structure

```
├── core               	# RTL code
├── doc                 # Documentation about the design
├── include             # Include file generated by config tool
├── scripts				# Some useful scripts
└── sim					# Testbench and run time environment
```

## Architecture

The targeted ISA is **RV32IMZicsr**.

Currently NRC is a single/multi cycle cpu core. It is used to build the test environment and SoC.
A 5-stage pipelined CPU will be created later to make it a true useful cpu core.
For more detailed architecture, check the following documents:

- Single/Multi cycle cpu core architecture: [CoreN](doc/CoreN.md)

## Simulation

For more detail about the testbench, read [verilator_tb.md](doc/verilator_tb.md)

The simulation environment is coded in C/C++ using ventilator simulator. There are two major tests in the repo:

### ICS PA

  - This is a set of tests and small software created in the NJU ICS PA labs. It is used to test the cpu core itself.
  - The test environment here duplicated the hardware peripherals created in the NJU ICS PA lab so it can run the same
    tests and softwares created in ICS PA.
  - To use this test, a pre-compiled RISC-V binary code for from the lab is required. The plan is to create a separate repo
    to hold these binary files so other people can also run the test.

To run ICS PA simulation:

```makefile
make FLOW=sim_ics_pa <TEST>

# Supported tests are
# cpu-test am-test alu-test coremark dhrystone microbench demo typing-game bad-apple fceux nanos-lite
```

### YSYX SoC

  - This is the test environment created for the YSYX SoC

To run YSYX SoC simulation:

```makefile
make FLOW=sim_ysyx
```

