# Single Cycle CPU Core



## Introduction

Single Cycle CPU Core design is a proof-of-concept RTL design. As the name indicate, it is a single cycle cpu so all the instruction complete at ONE clock cycle, including mul/div and memory access. Because of this nature, it does not support any complex feature such as hardware MMU, and it can't be implemented in FPGA either because the memory access is also single cycle.



## Implementation

The single cycle cpu core implementation is very simple. Based on the function, the design is divided into different modules containing different functions and then instantiated under top level.

The RTL for the single cycle cpu core is in `core/src/rtl/core_s`

Here is the RTL files for the Single Cycle CPU Core

- **ALU.sv**:  ALU module: perform logic and arithematic operation
- **BEU.sv**: Branch Unit: check branch result and calculate branch target address
- **core_s.sv**: Top Level module
- **CSR.sv**: Control and Status register module
- **DIV.sv**: Divider module: perform division operation
- **EXU.sv**: Execution Unit: contains ALU, and BEU
- **IDU.sv**: Instruction Decode Unit: decode the instruction into different fields and generate control signal
- **IFU.sv**: Instruction Fetch Unit: Hold PC register and fetch the next instruction from memory
- **MEU.sv**: Memory Unit: Contains the logic to access memory
- **MUL.sv**: Multiplier Module: perform multiplication operation
- **RegFile.sv**: Contain Register File
- **TRAP.sv**: Logic to handle exception

