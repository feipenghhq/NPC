/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/26/2023
 *
 * ------------------------------------------------------------------------------------------------
 * MEU: Extend memory read result
 * ------------------------------------------------------------------------------------------------
 */

`include "riscv_isa.svh"

module MEU #(
    parameter XLEN    = 32,
    parameter MEMOP_W = 3
) (
    input  logic [MEMOP_W-1:0] mem_opcode,
    input  logic               mem_read,
    input  logic               mem_write,
    input  logic [1:0]         byte_addr,
    input  logic [XLEN-1:0]    rs2_rdata,
    output logic [XLEN-1:0]    rd_wdata,
    output logic               data_valid, // data memory request
    output logic               data_wen,   // data memory write enable
    output logic [3:0]         data_wstrb, // data memory write strobe
    output logic [XLEN-1:0]    data_wdata, // data memory write data
    input  logic [XLEN-1:0]    data_rdata  // data memory read data
);

    // --------------------------------------
    //  Signal Definition
    // --------------------------------------

    logic is_lb;
    logic is_lbu;
    logic is_lh;
    logic is_lhu;
    logic is_lw;
    logic [7:0] lb_data;
    logic [15:0] lh_data;
    logic [XLEN-1:0] lb_ext_data;
    logic [XLEN-1:0] lbu_ext_data;
    logic [XLEN-1:0] lh_ext_data;
    logic [XLEN-1:0] lhu_ext_data;

    logic is_sb;
    logic is_sh;
    logic is_sw;
    logic [3:0] wstrb_byte;
    logic [3:0] wstrb_half;
    logic [3:0] wstrb_word;

    // --------------------------------------
    //  Main logic
    // --------------------------------------

    // generate memory request
    assign data_valid = mem_read | mem_write;
    assign data_wen = mem_write;

    // generate write strobe
    assign is_sb  = mem_opcode == `RV32I_FUNCT3_SB;
    assign is_sh  = mem_opcode == `RV32I_FUNCT3_SH;
    assign is_sw  = mem_opcode == `RV32I_FUNCT3_SW;
    assign wstrb_byte = {3'b0, is_sb} << byte_addr[1:0];
    assign wstrb_half = {byte_addr[1], byte_addr[1], ~byte_addr[1], ~byte_addr[1]} & {4{is_sh}};
    assign wstrb_word = {4{is_sw}};
    assign data_wstrb = wstrb_byte | wstrb_half | wstrb_word;

    // generate write data
    assign data_wdata = ({XLEN{is_sb}} & {4{rs2_rdata[7:0]}})  |
                        ({XLEN{is_sh}} & {2{rs2_rdata[15:0]}}) |
                        ({XLEN{is_sw}} & rs2_rdata);

    // process read data
    assign is_lb  = mem_opcode == `RV32I_FUNCT3_LB;
    assign is_lh  = mem_opcode == `RV32I_FUNCT3_LH;
    assign is_lw  = mem_opcode == `RV32I_FUNCT3_LW;
    assign is_lbu = mem_opcode == `RV32I_FUNCT3_LBU;
    assign is_lhu = mem_opcode == `RV32I_FUNCT3_LHU;

    assign lb_ext_data  = {{(XLEN-8){lb_data[7]}},  lb_data};
    assign lbu_ext_data = {{(XLEN-8){1'b0}},        lb_data};
    assign lh_ext_data  = {{(XLEN-16){lh_data[15]}},lh_data};
    assign lhu_ext_data = {{(XLEN-16){1'b0}},       lh_data};

    assign lb_data = ({8{byte_addr[1:0] == 0}} & data_rdata[ 7: 0]) |
                     ({8{byte_addr[1:0] == 1}} & data_rdata[15: 8]) |
                     ({8{byte_addr[1:0] == 2}} & data_rdata[23:16]) |
                     ({8{byte_addr[1:0] == 3}} & data_rdata[31:24]);

    assign lh_data = byte_addr[1] ? data_rdata[31:16] : data_rdata[15:0];

    assign rd_wdata = ({XLEN{is_lb}}  & lb_ext_data)  |
                      ({XLEN{is_lbu}} & lbu_ext_data) |
                      ({XLEN{is_lh}}  & lh_ext_data)  |
                      ({XLEN{is_lhu}} & lhu_ext_data) |
                      ({XLEN{is_lw}}  & data_rdata)   ;


endmodule
