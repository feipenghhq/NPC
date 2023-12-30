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
    parameter ALUOP_W = 4,
    parameter BXXOP_W = 3,
    parameter MEMOP_W = 3
) (
    input  logic [ALUOP_W-1:0] alu_opcode,
    input  logic [BXXOP_W-1:0] bxx_opcode,
    input  logic               alu_src1_sel_rs1,
    input  logic               alu_src1_sel_pc,
    input  logic               alu_src1_sel_0,
    input  logic               alu_src2_sel_rs2,
    input  logic               alu_src2_sel_imm,
    input  logic [XLEN-1:0]    pc,
    input  logic [XLEN-1:0]    rs1_rdata,
    input  logic [XLEN-1:0]    rs2_rdata,
    input  logic [XLEN-1:0]    imm,
    input  logic               bxx,
    input  logic               jump,
    output logic [XLEN-1:0]    rd_wdata,
    output logic               pc_branch,
    output logic [XLEN-1:0]    target_pc,
    output logic [XLEN-1:0]    alu_result
);

    // -------------------------------------------
    // Signal definition
    // -------------------------------------------

    logic [XLEN-1:0]    alu_src1;
    logic [XLEN-1:0]    alu_src2;
    logic [XLEN-1:0]    pc_plus4;
    logic               beu_result;

    // -------------------------------------------
    // Branch and Jump logic
    // -------------------------------------------

    BEU #(.BXXOP_W(BXXOP_W), .XLEN(XLEN))
    u_BEU (
        .bxx_opcode(bxx_opcode),
        .src1(rs1_rdata),
        .src2(rs2_rdata),
        .result(beu_result));

    assign pc_branch = jump | bxx & beu_result;
    assign target_pc = alu_result; // target pc is calculated by ALU

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

    // -------------------------------------------
    // Glue logic for rd write data
    // -------------------------------------------
    assign pc_plus4 = pc + 4;
    // For JAL/JALR, pc + 4 is written into rd
    assign rd_wdata = jump ? pc_plus4 : alu_result;

endmodule
