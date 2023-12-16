/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/15/2023
 *
 * ------------------------------------------------------------------------------------------------
 * EXU: Execution Unit
 * ------------------------------------------------------------------------------------------------
 */

module EXU #(
    parameter XLEN    = 32,
    parameter ALUOP_W = 4
) (
    input  logic [ALUOP_W-1:0] alu_opcode,
    input  logic               alu_src1_sel_rs1,
    input  logic               alu_src1_sel_pc,
    input  logic               alu_src1_sel_0,
    input  logic               alu_src2_sel_rs2,
    input  logic               alu_src2_sel_imm,
    input  logic [XLEN-1:0]    pc,
    input  logic [XLEN-1:0]    rs1_rdata,
    input  logic [XLEN-1:0]    rs2_rdata,
    input  logic [XLEN-1:0]    imm,
    output logic [XLEN-1:0]    alu_result
);

    // -------------------------------------------
    // Signal definition
    // -------------------------------------------

    logic [XLEN-1:0]    alu_src1;
    logic [XLEN-1:0]    alu_src2;


    // -------------------------------------------
    // ALU and its glue logic
    // -------------------------------------------
    assign alu_src1 = ({XLEN{alu_src1_sel_rs1}} & rs1_rdata) |
                      ({XLEN{alu_src1_sel_pc}} & pc) |
                      ({XLEN{alu_src1_sel_0}} & 0);

    assign alu_src2 = ({XLEN{alu_src2_sel_rs2}} & rs2_rdata) |
                      ({XLEN{alu_src2_sel_imm}} & imm);

    ALU #(
        .XLEN(XLEN),
        .ALUOP_W(ALUOP_W))
    u_ALU (
        .alu_opcode(alu_opcode),
        .alu_src1(alu_src1),
        .alu_src2(alu_src2),
        .alu_result(alu_result));

endmodule
