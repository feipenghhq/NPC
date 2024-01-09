/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/08/2024
 *
 * ------------------------------------------------------------------------------------------------
 * CSR: Control and Status Register
 * ------------------------------------------------------------------------------------------------
 */

`include "riscv_isa.svh"

module CSR #(
    parameter XLEN = 32
) (
    input  logic                clk,
    input  logic                rst_b,
    // CSR read/write bus
    input  logic                csr_write,
    input  logic                csr_set,
    input  logic                csr_clear,
    input  logic                csr_read,
    input  logic [XLEN-1:0]     csr_wdata,
    input  logic [11:0]         csr_addr,
    output logic [XLEN-1:0]     csr_rdata,
    // CSR field HW write
    input  logic [XLEN-1:0]     csr_wr_mepc_mepc,
    input  logic [XLEN-2:0]     csr_wr_mcause_exception_code,
    input  logic                csr_wr_mcause_interrupt,
    // CSR field HW read
    output logic [XLEN-3:0]     csr_rd_mtvec_base,
    output logic [1:0]          csr_rd_mtvec_mode,
    output logic [XLEN-1:0]     csr_rd_mepc_mepc
);
    // ------------------------------------------
    // Helper Macros
    // ------------------------------------------

    // Defining CSR register and generate CSR instruction write/read logic
    `define csr_reg_define(name, id) \
        logic  ``name``_hit; \
        logic  ``name``_read; \
        logic  ``name``_write; \
        assign ``name``_hit   = (csr_addr == id); \
        assign ``name``_read  = ``name``_hit & csr_read; \
        assign ``name``_write = ``name``_hit & csr_wen;

    // A macro used to generate double shash (//) to comment out some logic
    // in the macro define so we can have a more generic macro
    `define before_slash(x) x/
    `define double_shash()  `before_slash(/)
    `define blank()

    // Defining a CSR field with some common logic this including
    //            - Defining a a field for CSR
    //            - Sequential logic: always block and reset
    //            - CSR write logic
    //            - Assign the field to the CSR struct
    // [optional] - HW write logic (controlled by hww)
    // [optional] - HW read logic (controlled by hwr)
    `define csr_field_define(csr, field, width, rval, range, hwr, hww, wen) \
        logic [width-1:0] ``csr``_``field``; \
        always @(posedge clk) begin \
            if (!rst_b) ``csr``_``field`` <= rval; \
            else begin \
            hww if (wen)           ``csr``_``field`` <= csr_wr_``csr``_``field``; \
                if (``csr``_write) ``csr``_``field`` <= csr_final_wdata``range``; \
            end \
        end \
        assign csr.field = ``csr``_``field``; \
    hwr assign csr_rd_``csr``_``field`` = ``csr``_``field``;

    // Defining a CSR field with csr access only (no HW access)
    `define csr_field_define_no(csr, field, width, rval, range) \
        `csr_field_define(csr, field, width, rval, range, `double_shash(), `double_shash(), 0) \

    // Defining a CSR field with HW read only type field
    `define csr_field_define_ro(csr, field, width, rval, range) \
        `csr_field_define(csr, field, width, rval, range, `blank(), `double_shash(), 0) \

    // Defining a CSR field with HW write only type field
    `define csr_field_define_wo(csr, field, width, rval, wen, range) \
        `csr_field_define(csr, field, width, rval, range, `double_shash(), `blank(), wen) \

    // Defining a CSR field with HW read/write type field
    `define csr_field_define_rw(csr, field, width, rval, wen, range) \
        `csr_field_define(csr, field, width, rval, range, `blank(), `blank(), wen) \

    // ------------------------------------------
    // CSR write logic
    // ------------------------------------------

    logic [XLEN-1:0] csr_write_data;
    logic [XLEN-1:0] csr_set_data;
    logic [XLEN-1:0] csr_clear_data;

    logic            csr_wen;
    logic [XLEN-1:0] csr_final_wdata;

    assign csr_write_data = csr_wdata;
    assign csr_set_data   = csr_wdata  | csr_rdata;
    assign csr_clear_data = ~csr_wdata & csr_rdata;

    assign csr_wen = csr_write | csr_set | csr_clear;
    assign csr_final_wdata = ({XLEN{csr_write}} & csr_write_data) |
                             ({XLEN{csr_set}}   & csr_set_data) |
                             ({XLEN{csr_clear}} & csr_clear_data) ;

    // ------------------------------------------
    // CSR Register
    // ------------------------------------------
    // Only implement necessary CSR register, all other register returns zero on read. write has no effect.

    // ------------------------------------------
    // Machine status register (mstatus)
    // ------------------------------------------
    // FIXME: this is only implemented for difftest
    struct packed {
        logic [XLEN-1:0]    mstatus;
    } mstatus;

    `csr_reg_define(mstatus, `MSTATUS)
    `csr_field_define_no(mstatus, mstatus, XLEN, 0, [XLEN-1:0])

    assign mstatus_mstatus = 32'h1800; // initialize to this value to pass difftest

    // ------------------------------------------
    // Machine trap-handler base address (mtvec)
    // ------------------------------------------

    struct packed {
        logic [XLEN-1:2]    base;
        logic [1:0]         mode;
    } mtvec;

    `csr_reg_define(mtvec, `MTVEC)
    `csr_field_define_ro(mtvec, base, (XLEN-2), 0, [XLEN-1:2])
    `csr_field_define_ro(mtvec, mode, 2,        0, [1:0])

    // ------------------------------------------
    // Machine Cause Register (mcause)
    // ------------------------------------------

    struct packed {
        logic              interrupt;
        logic [XLEN-2:0]   exception_code;
    } mcause;

    `csr_reg_define(mcause, `MCAUSE)
    `csr_field_define_wo(mcause, interrupt,      1,        0, ent_trap, [XLEN-1])
    `csr_field_define_wo(mcause, exception_code, (XLEN-1), 0, ent_trap, [XLEN-2:0])

    // ------------------------------------------
    // Machine exception program counter register (mepc)
    // ------------------------------------------

    struct packed {
        logic [XLEN-1:0] mepc;
    } mepc;

    `csr_reg_define(mepc, `MEPC)
    `csr_field_define_rw(mepc, mepc, (XLEN), 0, ent_trap, [XLEN-1:0])

    // ------------------------------------------
    // Final CSR read decode
    // ------------------------------------------

    assign csr_rdata = ({XLEN{mstatus_read}}  & mstatus)  |
                       ({XLEN{mtvec_read}}    & mtvec)    |
                       ({XLEN{mcause_read}}   & mcause)   |
                       ({XLEN{mepc_read}}     & mepc)     |
                       (0) ;


endmodule
