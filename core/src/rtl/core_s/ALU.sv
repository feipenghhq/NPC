/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/15/2023
 *
 * ------------------------------------------------------------------------------------------------
 * ALU
 * ------------------------------------------------------------------------------------------------
 */

`include "riscv_isa.svh"

module ALU #(
    parameter XLEN    = 32,
    parameter ALUOP_W = 4
) (
    input  logic [ALUOP_W-1:0] alu_opcode,
    input  logic [XLEN-1:0]    alu_src1,
    input  logic [XLEN-1:0]    alu_src2,
    output logic [XLEN-1:0]    alu_result
);

    // --------------------------------------
    //  Signal Definition
    // --------------------------------------

    logic alu_op_add;
    logic alu_op_sub;
    logic alu_op_sll;
    logic alu_op_slt;
    logic alu_op_sltu;
    logic alu_op_xor;
    logic alu_op_srl;
    logic alu_op_sra;
    logic alu_op_or;
    logic alu_op_and;
    logic alu_op_add_or_sub;

    logic alu_substract;
    logic alu_adder_cin;
    logic alu_adder_cout;
    logic [XLEN-1:0] alu_adder_src1;
    logic [XLEN-1:0] alu_adder_src2;
    logic [XLEN-1:0] alu_adder_result;

    logic [XLEN-1:0] alu_slt_result;
    logic [XLEN-1:0] alu_sltu_result;
    logic [XLEN-1:0] alu_xor_result;
    logic [XLEN-1:0] alu_or_result;
    logic [XLEN-1:0] alu_and_result;
    logic [XLEN-1:0] alu_srl_result;
    logic [XLEN-1:0] alu_sra_result;
    logic [XLEN-1:0] alu_sll_result;

    // --------------------------------------
    //  Decode the opcode
    // --------------------------------------
    // Opcode is the same as FUNCT3
    assign alu_op_add  = (alu_opcode[2:0] == `RV32I_FUNCT3_ADD ) & ~alu_opcode[3];
    assign alu_op_sub  = (alu_opcode[2:0] == `RV32I_FUNCT3_SUB ) &  alu_opcode[3];
    assign alu_op_sll  = (alu_opcode[2:0] == `RV32I_FUNCT3_SLL );
    assign alu_op_slt  = (alu_opcode[2:0] == `RV32I_FUNCT3_SLT );
    assign alu_op_sltu = (alu_opcode[2:0] == `RV32I_FUNCT3_SLTU);
    assign alu_op_xor  = (alu_opcode[2:0] == `RV32I_FUNCT3_XOR );
    assign alu_op_srl  = (alu_opcode[2:0] == `RV32I_FUNCT3_SRL ) & ~alu_opcode[3];
    assign alu_op_sra  = (alu_opcode[2:0] == `RV32I_FUNCT3_SRA ) &  alu_opcode[3];
    assign alu_op_or   = (alu_opcode[2:0] == `RV32I_FUNCT3_OR  );
    assign alu_op_and  = (alu_opcode[2:0] == `RV32I_FUNCT3_AND );
    assign alu_op_add_or_sub = alu_op_add | alu_op_sub;

    // --------------------------------------
    //  Calculate result
    // --------------------------------------
    assign alu_xor_result = alu_src1 ^ alu_src2;
    assign alu_or_result  = alu_src1 | alu_src2;
    assign alu_and_result = alu_src1 & alu_src2;
    assign alu_srl_result = alu_src1 >> alu_src2[4:0];
    assign alu_sra_result = $signed(alu_src1) >>> alu_src2[4:0];
    assign alu_sll_result = alu_src1 << alu_src2[4:0];

    // instead of using separate subtracter for sub/slt/sltu instruction,
    // use the same adder as add operation and use 2's complement nature to
    // transfer subtraction to addition operation
    assign alu_substract = alu_op_sub | alu_op_slt | alu_op_sltu;
    assign alu_adder_src1 = alu_src1;
    assign alu_adder_src2 = alu_substract ? ~alu_src2 : alu_src2;
    assign alu_adder_cin = alu_substract ? 1'b1 : 1'b0;
    assign {alu_adder_cout, alu_adder_result} = alu_adder_src1 + alu_adder_src2 + {{XLEN{1'b0}}, alu_adder_cin};

    // slt result can be calculated with the following cases
    // 1. src1 < 0 and src2 > 0
    // 2. src1 and src2 are both positive or negative, and adder result is negative
    //    a. both src1/src2 > 0 and src1 - src2 < 0
    //    b. both src1/src2 < 0 and src1 - src2 < 0
    assign alu_slt_result[XLEN-1:1] = '0;
    assign alu_slt_result[0] = (alu_src1[XLEN-1] & ~alu_src2[XLEN-1]) |
                               (~(alu_src1[XLEN-1] ^ alu_src2[XLEN-1])) & alu_adder_result[XLEN-1];

    // for sltu, if there is no carry, then src1 is smaller then src2
    assign alu_sltu_result[XLEN-1:1] = '0;
    assign alu_sltu_result[0] = ~alu_adder_cout;

    assign alu_result = ({XLEN{alu_op_add_or_sub}} & alu_adder_result) |
                        ({XLEN{alu_op_sll}}        & alu_sll_result)   |
                        ({XLEN{alu_op_slt}}        & alu_slt_result)   |
                        ({XLEN{alu_op_sltu}}       & alu_sltu_result)  |
                        ({XLEN{alu_op_xor}}        & alu_xor_result)   |
                        ({XLEN{alu_op_srl}}        & alu_srl_result)   |
                        ({XLEN{alu_op_sra}}        & alu_sra_result)   |
                        ({XLEN{alu_op_or}}         & alu_or_result)    |
                        ({XLEN{alu_op_and}}        & alu_and_result);

endmodule

