# Verilator Testbench

[TOC]

## Introduction

The testbench for the CPU core is written in C/C++ using Verilator. The tests used to verify the design is mainly from NJU ICS2023 Lab.



## Implementation

The testbench environment is similar to the environment in NJU ICS2023 NEMU. It contains the main testbench for verilator and various supporting environments such as memory, devices, and debug infrastructure. The testbench is written in C++ since verilator is using C++, other codes are mainly written in C.

Here is the code for the testbench:

```txt
.
├── devices				# Emulated device for the CPU
├── difftest			# Difftest
├── include				# Header files
├── infra				# Debug infrastructure including various trace
├── Makefile			# Main makefile
├── memory				# Memory model
├── scripts				# Some makefile scripts
├── testbench			# The Main testbench class for verilator
├── utils				# Some utility functions
└── verilator_main.cc	# main function for verilator

```

### devices

The devices folder contains various device written in C to support the function in ICS2023 tests/program.

```txt
.
├── audio.c				# Emulated audio device using SDL.
├── device.c			# Device initialzation and registeration
├── keyboard.c			# Emulated keyboard.
├── serial.c			# Emulated serial port. Used to print text into the script
├── timer.c				# Emulated timer device.
└── vga.c				# Emulated VGA device using SDL.
```

### infra

The infra folder contains some trace functions to help debug.

```txt
.
├── difftest.c			# difftest 
├── disasm.c			# dis-assembly the machine code to show ASM in debug message
├── ftrace.c			# function trace.
├── itrace.c			# instruction trace.
├── mtrace.c			# memory trace.
├── ringbuf.c			# ring buffer to hold trace data
├── strace.c			# system call trace
└── trace.c				# common function for trace
```

### memory

The memory folder contains the memory device

```txt
.
├── mmio.c				# memory mapped I/O access
└── paddr.c				# contains the physical memory and logic to access memory
```

### testbench

This folder contains the main testbench logic for verilator

```txt
.
├── Dut.cc				# base class for a DUT
├── Core_s.cc			# class and DPI function for core_s design. Inherited from Dut class
├── tb-check.cc			# contains function to check test results
└── tb-exec.cc			# instantiate the design class and execute the testbench
```

