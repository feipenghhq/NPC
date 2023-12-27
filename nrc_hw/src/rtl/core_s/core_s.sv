/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/14/2023
 *
 * ------------------------------------------------------------------------------------------------
 * core_s: Single Cycle CPU core
 * ------------------------------------------------------------------------------------------------
 */

module core_s #(
    parameter XLEN       = 32,
    parameter PC_RST_VEC = 32'h80000000,    // PC reset vector
    parameter REGID_W    = 5                // Register ID width
) (

    input  logic                clk,
    input  logic                rst_b,

    output logic [XLEN-1:0]     pc,
    input  logic [XLEN-1:0]     inst        // input instruction
);

    // -------------------------------------------
    // localparam definition
    // -------------------------------------------

    localparam ALUOP_W = 4;
    localparam BXXOP_W = 3;
    localparam MEMOP_W = 3;
    localparam R0_ZERO = 1;

    // -------------------------------------------
    // Signal definition
    // -------------------------------------------
    // From IDU
    logic [ALUOP_W-1:0] dec_alu_opcode;
    logic [BXXOP_W-1:0] dec_bxx_opcode;
    logic [MEMOP_W-1:0] dec_mem_opcode;
    logic               dec_alu_src1_sel_rs1;
    logic               dec_alu_src1_sel_pc;
    logic               dec_alu_src1_sel_0;
    logic               dec_alu_src2_sel_rs2;
    logic               dec_alu_src2_sel_imm;
    logic               dec_bxx;
    logic               dec_jump;
    logic               dec_mem_read;
    logic               dec_mem_write;
    logic               dec_ebreak;
    logic               dec_rd_write;
    logic [REGID_W-1:0] dec_rd_addr;
    logic [REGID_W-1:0] dec_rs1_addr;
    logic [REGID_W-1:0] dec_rs2_addr;
    logic [XLEN-1:0]    dec_imm;
    // From RegFile
    logic [XLEN-1:0]    rs1_rdata;
    logic [XLEN-1:0]    rs2_rdata;
    // From EXU
    logic [XLEN-1:0]    rd_wdata;
    logic               pc_branch;
    logic [XLEN-1:0]    target_pc;

    // -------------------------------------------
    // Module Instantiation
    // -------------------------------------------

    // IFU
    IFU #(
        .XLEN(XLEN),
        .PC_RST_VEC(PC_RST_VEC))
    u_IFU (
        .clk(clk),
        .rst_b(rst_b),
        .pc_branch(pc_branch),
        .target_pc(target_pc),
        .pc(pc));

    // IDU
    IDU #(
        .XLEN(XLEN),
        .ALUOP_W(ALUOP_W),
        .BXXOP_W(BXXOP_W),
        .MEMOP_W(MEMOP_W),
        .REGID_W(REGID_W))
    u_IDU (.*);

    // RegFile
    RegFile #(
        .XLEN(XLEN),
        .REGID_W(REGID_W),
        .R0_ZERO(R0_ZERO))
    u_RegFile (
        .clk(clk),
        .rs1_addr(dec_rs1_addr),
        .rs1_rdata(rs1_rdata),
        .rs2_addr(dec_rs2_addr),
        .rs2_rdata(rs2_rdata),
        .rd_addr(dec_rd_addr),
        .rd_wdata(rd_wdata),
        .rd_write(dec_rd_write));

    // EXU
    EXU #(
        .XLEN(XLEN),
        .ALUOP_W(ALUOP_W),
        .BXXOP_W(BXXOP_W),
        .MEMOP_W(MEMOP_W))
    u_EXU (
        .alu_opcode(dec_alu_opcode),
        .bxx_opcode(dec_bxx_opcode),
        .mem_opcode(dec_mem_opcode),
        .alu_src1_sel_rs1(dec_alu_src1_sel_rs1),
        .alu_src1_sel_pc(dec_alu_src1_sel_pc),
        .alu_src1_sel_0(dec_alu_src1_sel_0),
        .alu_src2_sel_rs2(dec_alu_src2_sel_rs2),
        .alu_src2_sel_imm(dec_alu_src2_sel_imm),
        .pc(pc),
        .rs1_rdata(rs1_rdata),
        .rs2_rdata(rs2_rdata),
        .imm(dec_imm),
        .bxx(dec_bxx),
        .jump(dec_jump),
        .pc_branch(pc_branch),
        .target_pc(target_pc),
        .rd_wdata(rd_wdata));


    // -------------------------------------------
    // _Verilator DPI
    // -------------------------------------------
    `ifdef VERILATOR

        import "DPI-C" function void dpi_set_ebreak();

        always @(posedge clk) begin
            if (dec_ebreak) begin
                dpi_set_ebreak();
            end
        end

    `endif


endmodule

