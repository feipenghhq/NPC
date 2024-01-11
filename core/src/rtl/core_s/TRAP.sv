/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/10/2024
 *
 * ------------------------------------------------------------------------------------------------
 *  TRAP: Exception/Trap handler
 * ------------------------------------------------------------------------------------------------
 */

`include "riscv_isa.svh"

module TRAP #(
    parameter XLEN = 32
) (
    input  logic                clk,
    input  logic                rst_b,
    input  logic                ecall,
    input  logic                mret,
    input  logic [XLEN-1:0]     pc,
    // Trap control
    output logic                trap,       // trap request to change pc
    output logic [XLEN-1:0]     trap_pc,    // trap pc target
    output logic                ent_trap,   // enter trap
    // CSR write data from trap controller to CSR
    output logic [XLEN-1:0]     csr_wr_mepc_mepc,
    output logic [XLEN-2:0]     csr_wr_mcause_exception_code,
    output logic                csr_wr_mcause_interrupt,
    // CSR read data from CSR to trap controller
    input  logic [XLEN-3:0]     csr_rd_mtvec_base,
    input  logic [1:0]          csr_rd_mtvec_mode,
    input  logic [XLEN-1:0]     csr_rd_mepc_mepc
);

    // -----------------------------------
    // Signals
    // -----------------------------------

    logic [XLEN-1:0] trap_enter_pc;
    logic [XLEN-1:0] trap_exit_pc;

    // -----------------------------------
    // Entering trap
    // -----------------------------------

    // update mepc register
    assign csr_wr_mepc_mepc = pc;

    // update mcause register
    assign csr_wr_mcause_exception_code = ({(XLEN-1){ecall}} & `ECALL_M_MODE_CODE); // only support M mode for now
    assign csr_wr_mcause_interrupt = 1'b0;  // no interrupt supported yet

    assign trap_enter_pc = {csr_rd_mtvec_base, 2'b0}; // not supporting vectored mode

    // -----------------------------------
    // Exit trap
    // -----------------------------------
    assign trap_exit_pc = csr_rd_mepc_mepc;

    // -----------------------------------
    // Final control signal
    // -----------------------------------
    assign trap = mret | ecall;
    assign trap_pc = mret ? trap_exit_pc : trap_enter_pc;

    assign ent_trap = ecall;

    // -----------------------------------
    // Some static checks
    // -----------------------------------
    final assert (csr_rd_mtvec_mode == 0) else $error("Vectored mode not supported");

endmodule
