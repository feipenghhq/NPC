# Core Components

- [Core Components](#core-components)
  - [IFU](#ifu)
    - [Interfaces](#interfaces)
    - [Implementation](#implementation)
  - [IDU](#idu)
    - [Interface](#interface)
    - [Implementation](#implementation-1)
  - [Decoder](#decoder)
    - [Interface](#interface-1)
    - [Implementation](#implementation-2)
  - [Register File](#register-file)
  - [EXU (Single/Multi-cycle CPU)](#exu-singlemulti-cycle-cpu)
  - [ALU](#alu)
  - [BEU](#beu)


## IFU

IFU contains the PC (Program Counter) register and control logic to for instruction memory AXI4Lite Bus.

### Interfaces

| Name       | Width/Type<sup>1</sup> | Direction<sup>2</sup> | Description                                 |
| ---------- | ---------------------- | --------------------- | ------------------------------------------- |
| ifuData    | IfuBundle              | Stream (Host)         | IFU data to next stage                      |
| ibus       | AXI4Lite               | Stream (Host)         | Instruction memory bus - AXI4Lite Interface |
| branchCtrl | xlen bits              | Flow (Device)         | Branch valid and target PC                  |
| trapCtrl   | xlen bits              | Flow (Device)         | Trap valid and target PC                    |

1. SpinalHDL provides a [Bundle](https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Data%20types/bundle.html)
   data type which is a composite type that defines a group of named signals. The details of each bundle will be provided
   in [Appendix-1: Bundle](#appendix-1-bundles)
1. SpinalHDL library provides two interfaces called [Stream](https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Libraries/stream.html)
   and [Flow](https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinaislHDL/Libraries/flow.html). Stream is a simple
   valid-ready handshake protocol to carry payload. Flow is is a simple valid/payload protocol. The Host and Device are
   the direction of the interfaces. For Stream interface, a valid and a ready signal will be added by SpinalHDL and for
   Flow interface, a valid signal will be added by SpinalHDL.

#### IfuBundle

| Name        | Width | Description                     |
| ----------- | ----- | ------------------------------- |
| pc          | xlen  | Program Counter                 |
| instruction | xlen  | Instruction fetched from memory |

### Implementation

#### PC

PC is the address to fetch next address and usually increment by 4 when an instruction is fetched from the memory and
sent to the downstream module (a handshake completes). With a successful branch or jump instruction, or a trap, PC
will alter to a new value instead of pc + 4. If the instruction fetching is delayed or the downstream logic is blocked,
pc will keep its value.

#### Instruction memory bus

The instruction memory bus is an AXI4Lite bus. A state machine controls the AXI bus logic:

##### IFU state machine for SINGLE/MULTI-CYCLE CPU

State machine

![State Machine](./assets/IFU_State.drawio.png)

State

| State | Description                                                                          |
| ----- | ------------------------------------------------------------------------------------ |
| IDLE  | **Idle state.**  This is the state when cpu is first boot and/or under reset.        |
| REQ   | **Request state.** Assert the arvalid signal to send the read request to the memory. |
| DATA  | **Data state.**  Wait for the read data from the memory.                             |
| WAIT  | **Wait state.**  Wait for downstream logic to be ready.                              |

State Transition

| Current State | Next State | Condition           | Description                                                             |
| ------------- | ---------- | ------------------- | ----------------------------------------------------------------------- |
| IDLE          | REQ        | !reset              | Once reset is release, goto **REQ** state                               |
| REQ           | DATA       | arready             | AR channel handshake complete.                                          |
| DATA          | REQ        | rvalid & ifu_ready  | Read data is back and downstream logic is able to receive the data      |
| DATA          | WAIT       | rvalid & ~ifu_ready | Read data is back but downstream logic is not able to received the data |
| WAIT          | REQ        | ifu_ready           | Downstream logic is ready                                               |

Note: This state machine only works for single/multi-cycle CPU and it only start to fetch the next instruction when the
current instruction has retired. But for pipelined CPU, we need to fetch the next instruction once the instruction retire
from the IFU itself.

An instructions buffer is used to store the instruction when the downstream logic is not able to take the instruction
when it comes back.

## IDU

### Interface

| Name     | Width/Type | Direction       | Description                      |
| -------- | ---------- | --------------- | -------------------------------- |
| iduData  | IduBundle  | Stream (Host)   | IDU data going to the next stage |
| ifuData  | IfuBundle  | Stream (Device) | IFU data from IFU                |
| rdWrCtrl | RdWrCtrl   | Flow (Device)   | Rd write address and write data  |

#### IduBundle

| Name    | Width/Type | Description                 |
| ------- | ---------- | --------------------------- |
| cpuCtrl | CpuCtrl    | CPU control signals         |
| csrCtrl | CsrCtrl    | CSR module control signals  |
| rs1Data | xlen bits  | RS1 data from register file |
| rs2Data | xlen bits  | RS2 data from register file |
| pc      | xlen bits  | program counter             |

- CpuCtrl is described in [Cpu Control signal (CpuCtrl)](#cpu-control-signal-cpuctrl)
- CsrCtrl is described in [CSR Control signal (CsrCtrl)](#csr-control-signal-csrctrl)

### Implementation

IDU has two major components: Decoder and Register File.

- [Decoder](#decoder) is responsible for decoding the instruction and generate various control signals for the EXU.
- [Register File](#register-file) contains the registers.

## Decoder

Decoder read the instruction and generate various control signals. The Interface session listed all the control signals
generated by the decoder.

### Interface

| Name    | Width/Type | Direction | Description                |
| ------- | ---------- | --------- | -------------------------- |
| ifuData | IfuBundle  | Input     | IFU data from IFU          |
| cpuCtrl | CpuCtrl    | Output    | CPU control signals        |
| csrCtrl | CsrCtrl    | Output    | CSR module control signals |

#### CPU Control signal (CpuCtrl)

When the control signals are assert, it indicate that the CPU will perform a certain operation.

| Name      | Width | Description              |
| --------- | :---: | ------------------------ |
| rdWrite   |   1   | Write to the register    |
| rdAddr    |   5   | Write register address   |
| branch    |   1   | Branch instruction       |
| jump      |   1   | Jump instruction         |
| memRead   |   1   | Memory read              |
| memWrite  |   1   | Memory write             |
| ebreak    |   1   | ebreak instruction       |
| ecall     |   1   | ecall instruction        |
| mret      |   1   | mret instruction         |
| aluSelPc  |   1   | ALU select PC as operand |
| selImm    |   1   | Select Immediate value   |
| aluOpcode |   5   | ALU opcode<sup>1</sup>   |
| opcode    |   3   | opcode<sup>2</sup>       |
| rs1Addr   |   5   | rs1 register id          |
| rs2Addr   |   5   | rs2 register id          |
| immediate | XLEN  | Immediate value          |
| muldiv    |   1   | Mul or div instruction   |

<sup>1</sup>The ALU opcode is used by ALU to determines the operation in ALU.
   - aluOpcode[2:0]: Same encoding as the **funct3** field in the instruction. ALU will perform the operation defined in
     the RISC-V spec that match this encoding in **funct3** field.
   - aluOpcode[3]: This bit is used together with aluOpcode[2:0] to distinguish between add/sub, srl(i)/sra(i) since
     they have the same encoding in **funct3** field. This info is encoded in instruction[30].
   - aluOpcode[4]: Encode the LUI instruction for ALU. If aluOpcode[4] = 1, ALU will perform LUI operation regardless of
     other bits in aluOpcode.

Full **aluOpcode** encoding:

| Encoding | Operation |
| -------- | --------- |
| 5'b1XXXX | LUI       |
| 5'b00000 | ADD       |
| 5'b01000 | SUB       |
| 5'b00001 | SLL       |
| 5'b00010 | SLT       |
| 5'b00011 | SLTU      |
| 5'b00100 | XOR       |
| 5'b00101 | SRL       |
| 5'b01101 | SRA       |
| 5'b00110 | OR        |
| 5'b00111 | AND       |

<sup>2</sup>The opcode is used to encode instruction for branch/load/store/rv32m type of instruction. The value is the same as
   **funct3** field since the spec use funct3 to encode these instruction.

Full **opcode** encoding:

| Encoding | Branch | Load | Store | RV32M  |
| -------- | ------ | ---- | ----- | ------ |
| 3'b000   | BEQ    | LB   | SB    | MUL    |
| 3'b001   | BNE    | LH   | SH    | MULH   |
| 3'b010   |        | LW   | SW    | MULHSU |
| 3'b011   |        |      |       | MULHU  |
| 3'b100   | BLT    | LBU  |       | DIV    |
| 3'b101   | BGE    | LHU  |       | DIVU   |
| 3'b110   | BLTU   |      |       | REM    |
| 3'b111   | BGEU   |      |       | REMU   |

##### CSR Control signal (CsrCtrl)

In RTL, CSR related control signal use a separate interface(class)

| Name  | Width | Description                                          |
| ----- | :---: | ---------------------------------------------------- |
| write |   1   | Write the data<sup>1</sup> to CSR                    |
| set   |   1   | Set the CSR register based on the mask<sup>1</sup>   |
| clear |   1   | Clear the CSR register based on the mask<sup>1</sup> |
| read  |   1   | Read the CSR register                                |
| addr  |  12   | CSR register ID                                      |

1. The data and mask are generated by the cpu logic depending on the csr instruction.

### Implementation

TBD

## Register File

Register File contains 32 32-bit registers (RV32I). R0 always return 0.

Register File has 3 ports: 2 read ports and 1 write ports.
- rs1 and rs2 are read ports that returns register value to the given register id. It takes the register id from decoder
  and returns the register value.
- rd is the write port that write the execution result back to register file.

## EXU (Single/Multi-cycle CPU)

In Single/Multi-cycle cpu EXU is the last stage of the cpu core logic and it contains all the execution units including:
ALU, BEU, LSU, MulDiv, CSR, TrapCtrl. The control signals from IDU determines which function units are used in EXU and
the execution result will be calculated by that unit.

Besides the above module, it also contains the following logic:
1. MUX logic to select different sources into the ALU. ALU operand 1 can be rs1 or pc. ALU operand 2 can be rs2 or immediate value.
2. MUX logic to select different result to write to the register files. The results are: ALU result, MulDiv result,
   PC+4 (for jump instruction), mem read data, and csr read data.

EXU may back-pressure the upstream logic (IDU and IFU) if the instruction can't be completed within the clock cycle.
Currently the mem read and write instruction will need more then 1 clock cycle to complete. Mul/Div also needs more
then 1 clock cycle but we implemented these instructions using verilog operator *, /, and % for now so they only take
1 clock cycle at this point.

## ALU

ALU is responsible for logic and arithmetic operation. It takes 2 inputs and an opcode. Based on the alu opcode, it selects
the corresponding result to send to the output. The alu opcode encoding can be in **Decoder** session.

Most of the operations are implemented using the corresponding HDL operator with a few exceptions:

1. LUI: LUI result is the same as the immediate.
2. SLT/SLTU: Instead of using A < B which will incur some sort of adder chains, we can use some other logic to calculate
   them to save area.
   - SLTU: SLTU can be considered as A - B < 0, and we just need to check the MSb of the subtraction result.
   - SLT: SLT can be divided into the following 2 conditions: A < 0 and B > 0 or A and B are both positive/negative
     and A - B < 0. With this consideration, we only need to check A.MSb, B.MSb and (A-B).MSb

ALU also has a dedicated output for the add result because some instructions may use ALU to calculate some results and
the calculation is mainly add operation. The dedicated output for add result helps improve timing. These instructions
includes: branch/jump - calculate target address (PC) and memory read/write - calculate address.

## BEU

BEU is responsible for checking the branch result and generate the jump target address.

For jump instruction, it will also switch to the new address. For branch instruction, the branch result is checked here
in BEU. If the check success, it will switch to the new address. The target address is calculated in ALU and some minor
update to the address for jump instruction is made in BEU.

Note here we choose to calculate the target address using ALU and check branch result in BEU. Another design choice is
to use ALU to calculate branch result and calculate the target address in BEU. Our choice might have a better timing
result on determines the branch decision.
