# New RISC-V CPU

[TOC]

## Introduction

NRC stands for **N**ew **R**ISC-V **C**PU. It is a RISC-V CPU hardware and software design following the steps on [YSYX (一生一芯) project.](https://ysyx.oscc.cc/docs/) (English version can be found here: https://ysyx.oscc.cc/docs/en/)

The Final goal of this CPU design is to be able to boot Linux.

This repository holds the CPU Core RTL codes and its verification environment. A separate repository holds the SoC design.



## Repository  Structure

```txt
├── core               	# RISC-V CPU core RTL code
├── LICENSE				# License file
├── Makefile			# Main makefile
├── README.md			# Main readme file
├── scripts				# Hold some useful scripts
└── sim					# verilator testbench and run time environment

```



## Architecture

Currently NRC support Single Cycle CPU Core (with limited functionality).

The CPU core support **RV32I** ISA. It also support **M** and **Zicsr** extension.

For more detailed architecture, check the following documents:

Single Cycle CPU Core: [core_s.md](doc/core_s.md)



## Configuration

There are several configuration option in this repo, it is build based on kconfig.

To launch the configuration menu, use the following make command:

```make
make menuconfig
```

It opens up a BIOS like GUI and you can configure various things there.



## Simulation

The simulation environment is coded in C/C++ using ventilator simulator. The tests used to verify the design is mainly from NJU ICS2023 Lab.

To run simulation:

```makefile
make FLOW=sim <TEST>
```

Here is the supported TEST:

```txt
cpu-test am-test alu-test coremark dhrystone microbench demo typing-game bad-apple fceux nanos-lite
```

For more detail about the testbench, read [verilator_tb.md](doc/verilator_tb.md)
