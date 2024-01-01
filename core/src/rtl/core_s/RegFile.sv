/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/15/2023
 *
 * ------------------------------------------------------------------------------------------------
 * RegFile: registe File
 * ------------------------------------------------------------------------------------------------
 */

module RegFile #(
    parameter XLEN    = 32,
    parameter REGID_W = 5,  // register ID width
    parameter R0_ZERO = 1   // Tie $0 to Zero
)(
    input  logic                clk,
    // RS1 read port
    input  logic [REGID_W-1:0]  rs1_addr,
    output logic [XLEN-1:0]     rs1_rdata,
    // RS2 read port
    input  logic [REGID_W-1:0]  rs2_addr,
    output logic [XLEN-1:0]     rs2_rdata,
    // RD write port
    input  logic [REGID_W-1:0]  rd_addr,
    input  logic [XLEN-1:0]     rd_wdata,
    input  logic                rd_write
);
    localparam REG_NUM = 1 << REGID_W;

    reg [XLEN-1:0] regs[REG_NUM] /*verilator public*/;

    // RS1/RS2
    generate
        if (R0_ZERO) begin: gen_r0_zero
            assign rs1_rdata = (rs1_addr == 0) ? {XLEN{1'b0}} : regs[rs1_addr];
            assign rs2_rdata = (rs2_addr == 0) ? {XLEN{1'b0}} : regs[rs2_addr];
        end
        else begin: gen_no_r0_zero
            assign rs1_rdata = regs[rs1_addr];
            assign rs2_rdata = regs[rs2_addr];
        end
    endgenerate

    // RD
    always @(posedge clk) begin
        if (rd_write) begin
            regs[rd_addr] <= rd_wdata;
        end
    end

endmodule
