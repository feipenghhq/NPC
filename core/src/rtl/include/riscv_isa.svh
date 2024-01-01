/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: RVCoreF
 * Author: Heqing Huang
 * Date Created: 12/14/2023
 *
 * ------------------------------------------------------------------------------------------------
 * RISCV ISA Related Macros
 * ------------------------------------------------------------------------------------------------
 */

`ifndef __RISCV_ISA__
`define __RISCV_ISA__

// RV32I Instruction Set Opcode
`define RV32I_OPCODE_LUI       5'b01101
`define RV32I_OPCODE_AUIPC     5'b00101
`define RV32I_OPCODE_JAL       5'b11011
`define RV32I_OPCODE_JALR      5'b11001
`define RV32I_OPCODE_ITYPE     5'b00100
`define RV32I_OPCODE_RTYPE     5'b01100
`define RV32I_OPCODE_BRANCH    5'b11000
`define RV32I_OPCODE_LOAD      5'b00000
`define RV32I_OPCODE_STORE     5'b01000
`define RV32I_OPCODE_FENCE     5'b00011
`define RV32I_OPCODE_SYSTEM    5'b11100

// FUNCT3 field for different instruction
`define RV32I_FUNCT3_ADD       3'b000
`define RV32I_FUNCT3_SUB       3'b000
`define RV32I_FUNCT3_SLL       3'b001
`define RV32I_FUNCT3_SLT       3'b010
`define RV32I_FUNCT3_SLTU      3'b011
`define RV32I_FUNCT3_XOR       3'b100
`define RV32I_FUNCT3_SRL       3'b101
`define RV32I_FUNCT3_SRA       3'b101
`define RV32I_FUNCT3_OR        3'b110
`define RV32I_FUNCT3_AND       3'b111

`define RV32I_FUNCT3_BEQ       3'b000
`define RV32I_FUNCT3_BNE       3'b001
`define RV32I_FUNCT3_BLT       3'b100
`define RV32I_FUNCT3_BGE       3'b101
`define RV32I_FUNCT3_BLTU      3'b110
`define RV32I_FUNCT3_BGEU      3'b111

`define RV32I_FUNCT3_LB        3'b000
`define RV32I_FUNCT3_LH        3'b001
`define RV32I_FUNCT3_LW        3'b010
`define RV32I_FUNCT3_LBU       3'b100
`define RV32I_FUNCT3_LHU       3'b101

`define RV32I_FUNCT3_SB        3'b000
`define RV32I_FUNCT3_SH        3'b001
`define RV32I_FUNCT3_SW        3'b010

// Instruction Field Range
`define R_PHASE  1:0
`define R_OPCODE 6:2
`define R_FUNCT3 14:12
`define R_FUNCT7 31:25
`define R_RS1    19:15
`define R_RS2    24:20
`define R_RD     11:7


`endif
